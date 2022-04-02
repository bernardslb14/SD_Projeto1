package Server.ServerClasses;

import java.io.IOException;
import java.net.*;

public class UDPService extends Thread{
    int port = 6002;
    int timeBetweenPing = 1000; // ms
    int maxTimeOuts = 10; // totalTime = timeBetweenPing * maxTimeOuts
    SharedMemory sm;
    UDPFilesSynch synchThread;

    public UDPService(SharedMemory sm) {
        this.sm = sm;
        this.start();
    }

    public void run() {
        while (true){
            //SERVER PRIMARIO
            try (DatagramSocket aSocket = new DatagramSocket(this.port)) {
                sm.setPrimaryServer(true);
                synchThread = new UDPFilesSynch(this.port+1, sm);

                while (true) {
                    byte[] buffer = new byte[1000];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);

                    DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
                    aSocket.send(reply);
                }
            } catch (BindException e) {
                //SERVER SECUNDARIO
                int nr_pingsMissed = 0;
                try(DatagramSocket aSocket = new DatagramSocket()){
                    sm.setPrimaryServer(false);
                    synchThread = new UDPFilesSynch(this.port+1, sm);

                    while (true) {
                        byte[] buffer = new byte[1000];
                        String pingText = "ping";
                        InetAddress aHost = InetAddress.getByName("localhost");
                        DatagramPacket request = new DatagramPacket(pingText.getBytes(), pingText.getBytes().length, aHost, this.port);
                        aSocket.send(request);

                        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                        aSocket.setSoTimeout(1000);
                        try {
                            aSocket.receive(reply);
                            nr_pingsMissed = 0;
                            this.sleep(timeBetweenPing);
                        } catch (SocketTimeoutException ste){
                            nr_pingsMissed++;
                            System.out.println("Pings Missed In Row: "+ nr_pingsMissed);

                            if (nr_pingsMissed > maxTimeOuts){
                                throw new IOException();
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }

                    }

                } catch (IOException ioe) {
                    System.out.println("Assuming Primary Server Role");
                    //synchThread.interrupt();
                }
            } catch (IOException e){
            }
        }
    }
}