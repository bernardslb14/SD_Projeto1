package Server.ServerClasses;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {
    int primaryPort = 6000;
    int secundaryPort = 7000;
    SharedMemory sm;

    public TCPServer(SharedMemory sm) {
        this.sm = sm;
        this.start();
    }

    public void run(){
        int threadNr = 0;

        try(ServerSocket listenSocket = new ServerSocket(this.primaryPort)) {
            while (true) {
                sm.setRootPath(".\\ServerDir\\");
                Socket clientSocket = listenSocket.accept();
                new TCPConnection(clientSocket, ++threadNr, sm.getRootPath(), sm);
            }
        } catch (BindException be){
            try(ServerSocket listenSocket = new ServerSocket(this.secundaryPort)) {
                while (true) {
                    sm.setRootPath(".\\ServerSecundaryDir\\");
                    Socket clientSocket = listenSocket.accept();
                    new TCPConnection(clientSocket, ++threadNr, sm.getRootPath(), sm);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}