package Server.ServerClasses;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class UDPFilesSynch extends Thread{
    int port;
    SharedMemory sm;

    public UDPFilesSynch(int port, SharedMemory sm) {
        this.port = port;
        this.sm = sm;
        this.start();
    }

    public void run(){
        if (sm.isPrimaryServer()){
            //SERVER PRIMARIO
            try (DatagramSocket aSocket = new DatagramSocket()) {
                InetAddress aHost = InetAddress.getByName("localhost");
                while(true){
                    if (!sm.isOperationsPendingEmpty()){
                        String op = sm.getOperation();
                        JSONObject msg = new JSONObject(op);
                        //filePath

                        File file = new File(sm.getRootPath() + msg.getString("filePath"));
                        FileInputStream fileInputStream = new FileInputStream(file);

                        //ADICIONAR TAMANHO DO FICHEIRO AO JSON
                        msg.put("fileSize", file.length());

                        //ENVIA INFORMACOES DO FICHEIRO ATUAL
                        DatagramPacket req = new DatagramPacket(msg.toString().getBytes(), msg.toString().getBytes().length, aHost, this.port);
                        aSocket.send(req);

                        byte[] buffer = new byte[Math.toIntExact(file.length())];

                        fileInputStream.read(buffer);
                        aSocket.send(new DatagramPacket(buffer ,buffer.length, aHost, this.port));

                        fileInputStream.close();

                        sm.remOperation(op);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //SERVER SECUNDARIO
            try(DatagramSocket aSocket = new DatagramSocket(this.port)){
                while(true){
                    if (sm.isPrimaryServer())
                        break;
                    JSONObject msg;
                    byte[] buffer = new byte[4*1024];
                    aSocket.setSoTimeout(500);
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    try{
                        aSocket.receive(request);

                        msg = new JSONObject(new String(request.getData(), 0, request.getLength()));
                        FileOutputStream fileOutputStream = new FileOutputStream(sm.getRootPath() + msg.getString("filePath"));
                        long size = msg.getLong("fileSize");

                        byte[] fileData = new byte[Math.toIntExact(size)];

                        DatagramPacket fileDatagram = new DatagramPacket(fileData, fileData.length);
                        aSocket.receive(fileDatagram);

                        fileOutputStream.write(fileData);
                        fileOutputStream.close();
                    } catch (SocketTimeoutException ignored){

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
