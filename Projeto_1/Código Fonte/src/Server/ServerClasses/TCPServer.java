package Server.ServerClasses;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {
    int primaryPort;
    int secondaryPort;
    SharedMemory sm;

    public TCPServer(SharedMemory sm, int primaryPort, int secondaryPort) {
        this.primaryPort = primaryPort;
        this.secondaryPort = secondaryPort;
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
            try(ServerSocket listenSocket = new ServerSocket(this.secondaryPort)) {
                while (true) {
                    sm.setRootPath(".\\ServerSecondaryDir\\");
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