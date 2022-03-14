import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONObject;

public class ucDriveServer {
    public static void main(String args[]){
        //Start TCP Service
        new TCPServer();
    }
}

class TCPServer extends Thread {
    public TCPServer() {
        this.start();
    }

    public void run(){
        int threadNr = 0;

        try(ServerSocket listenSocket = new ServerSocket(6000)){
            while (true){
                Socket clientSocket = listenSocket.accept();
                new TCPConnection(clientSocket, ++threadNr);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}

class TCPConnection extends Thread{
    Socket clientSocket;
    int threadNr;
    DataInputStream in;
    DataOutputStream out;

    public TCPConnection(Socket clientSocket, int threadNr) {
        this.clientSocket = clientSocket;
        this.threadNr = threadNr;

        try{
            in = new DataInputStream(this.clientSocket.getInputStream());
            out = new DataOutputStream(this.clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try{
            while (true){
                String req = in.readUTF();
                System.out.println("[ " + threadNr + " ] -> " + req);
                switch (req){
                    case "listCurrDir":

                        break;
                }
                out.writeUTF(req);
            }
        } catch (EOFException e) {
            System.out.println("[ " + threadNr + " ]");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[ " + threadNr + " ]");
            e.printStackTrace();
        }
    }
}