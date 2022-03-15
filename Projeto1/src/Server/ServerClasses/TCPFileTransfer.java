package Server.ServerClasses;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPFileTransfer extends Thread{
    int primaryPort = 6001;
    int secundaryPort = 7001;
    SharedMemory sm;
    public TCPFileTransfer(SharedMemory sm) {
        this.sm = sm;
        this.start();
    }

    public void run(){
        int threadNr = 0;

        try(ServerSocket listenSocket = new ServerSocket(this.primaryPort)){
            while (true){
                Socket clientSocket = listenSocket.accept();
                new TCPFileTransferConnection(clientSocket, ++threadNr, ".\\ServerDir\\", sm);
            }
        } catch (BindException be){
            try(ServerSocket listenSocket = new ServerSocket(this.secundaryPort)) {
                while (true) {
                    Socket clientSocket = listenSocket.accept();
                    new TCPFileTransferConnection(clientSocket, ++threadNr, ".\\ServerSecundaryDir\\", sm);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}