package Server.ServerClasses;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;

public class TCPFileTransferConnection extends Thread{
    Socket clientSocket;
    int threadNr;
    String rootPath;
    SharedMemory sm;
    DataInputStream in;
    DataOutputStream out;

    public TCPFileTransferConnection(Socket clientSocket, int threadNr, String rootPath, SharedMemory sm) {
        this.clientSocket = clientSocket;
        this.threadNr = threadNr;
        this.rootPath = rootPath;
        this.sm = sm;

        try{
            this.in = new DataInputStream(this.clientSocket.getInputStream());
            this.out = new DataOutputStream(this.clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            JSONObject req = new JSONObject(in.readUTF());

            String operation = req.getString("operation");
            switch (operation){
                case "uploadToServer" -> {
                    String username = req.getString("username");
                    String serverFileDir = req.getString("serverPath");
                    String serverFileName = req.getString("serverName");

                    int bytes = 0;
                    FileOutputStream fileOutputStream = new FileOutputStream(rootPath + username + "\\" + serverFileDir + "\\" + serverFileName);

                    long size = in.readLong();     // read file size
                    byte[] buffer = new byte[4*1024];
                    while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                        fileOutputStream.write(buffer,0,bytes);
                        size -= bytes;      // read upto file size
                    }
                    fileOutputStream.close();

                    JSONObject op = new JSONObject();
                    op.put("filePath", username + "\\" + serverFileDir + "\\" + serverFileName);

                    sm.addOperation(op.toString());
                }
                case "downloadFromServer" -> {
                    String username = req.getString("username");
                    String serverFileDir = req.getString("serverPath");
                    String serverFileName = req.getString("serverName");

                    int bytes = 0;
                    File file = new File(rootPath + username + "\\" + serverFileDir + "\\" + serverFileName);
                    FileInputStream fileInputStream = new FileInputStream(file);

                    // send file size
                    out.writeLong(file.length());
                    // break file into chunks
                    byte[] buffer = new byte[4*1024];
                    while ((bytes=fileInputStream.read(buffer))!=-1){
                        out.write(buffer,0,bytes);
                        out.flush();
                    }
                    fileInputStream.close();
                }
            }
        } catch (EOFException e) {
            System.out.println("FTS [ " + threadNr + " ] disconnected.");
        } catch (IOException e) {
            System.out.println("FTS [ " + threadNr + " ] lost connection.");
        }
    }
}