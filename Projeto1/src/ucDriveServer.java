import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Scanner;


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
    String username;
    String password;
    String curDirInit;
    String curDir;
    JSONArray lastDirRequestedContent;

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
                switch (req) {
                    case "autenticacao" -> {
                        JSONObject resp = checkUser(in, out);
                        if (resp != null) {
                            this.username = resp.getString("username");
                            this.curDir = resp.getString("lastDir");
                            this.curDirInit = resp.getString("lastDir");
                            this.password = resp.getString("password");
                        }
                    }
                    case "changeServerDir" -> {
                        curDir = changeDir(username, in, curDir, lastDirRequestedContent);
                    }
                    case "listServerDir" -> {
                        lastDirRequestedContent = listServerFiles(username, out, curDir);
                    }
                }
            }
        } catch (EOFException e) {
            System.out.println("[ " + threadNr + " ] disconnected.");
            saveLastDir(username, password, curDirInit, curDir);
        } catch (IOException e) {
            System.out.println("[ " + threadNr + " ] lost connection.");
            saveLastDir(username, password, curDirInit, curDir);
        }
    }

    public static JSONObject checkUser(DataInputStream in, DataOutputStream out) throws IOException{
        JSONObject dados = new JSONObject(in.readUTF());

        String username = dados.getString("username");
        String pw = dados.getString("password");

        String filePath = ".\\src\\ServerDir\\clients.txt";
        Boolean flag = false;
        String lastDir = "";
        try{
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);
            while(myReader.hasNextLine()){
                String data = myReader.nextLine();
                String[] arr = data.split("\\|");

                if(arr[0].equals(username) && arr[1].equals(pw)){
                    if (arr.length == 3)
                        lastDir = arr[2];
                    flag=true;
                    break;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e){
            System.out.println("Error");
            e.printStackTrace();
        }

        if (flag){
            out.writeBoolean(true);
            JSONObject returnVal = new JSONObject();
            returnVal.put("username", username);
            returnVal.put("lastDir", lastDir);
            returnVal.put("password", pw);
            return returnVal;
        }
        else {
            return null;
        }
    }

    public static String changeDir(String username, DataInputStream in, String dir, JSONArray curDirList) throws IOException{
        String action = in.readUTF();

        if (action.equals("..")){
            String[] DirArr = dir.split("\\\\");
            dir = "";
            for (int i = 1; i < DirArr.length - 1; i++){
                dir += "\\" + DirArr[i];
            }
            return dir;
        }

        for (int i = 0; i < curDirList.length(); i++){
            JSONObject elem = curDirList.getJSONObject(i);
            if (!elem.getString("name").equals(action))
                continue;
            if (elem.getBoolean("isDirectory")){
                dir += "\\" + action;
                return dir;
            }
        }
        return dir;
    }

    public static JSONArray listServerFiles(String username, DataOutputStream out, String dir) throws IOException{
        String filePath = ".\\src\\ServerDir\\" + username;
        if (!dir.isEmpty())
            filePath += dir;

        File path = new File(filePath);
        //Caso nao exista a diretoria
        if (!path.exists())
            path.mkdir();

        JSONArray finalList = new JSONArray();
        File[] fileList = path.listFiles();
        for (File file: fileList) {
            JSONObject curFile = new JSONObject();
            curFile.put("isDirectory", file.isDirectory());
            curFile.put("isFile", file.isFile());
            curFile.put("name", String.valueOf(file).substring(filePath.length() + 1));
            finalList.put(curFile);
        }

        if (dir.isEmpty())
            out.writeUTF("Home\\..");
        else
            out.writeUTF("Home" + dir + "\\..");
        out.writeUTF(finalList.toString());

        return finalList;
    }

    public static void saveLastDir(String username, String password, String oldDir, String newDir){
        String filePath = ".\\src\\ServerDir\\clients.txt";

        try(Scanner sc = new Scanner(new File(filePath))) {
            StringBuffer buffer = new StringBuffer();
            while (sc.hasNextLine()) {
                buffer.append(sc.nextLine() + System.lineSeparator());
            }
            String fileContents = buffer.toString();

            String oldLine = username + "\\|" + password + "\\|" + oldDir;
            String newLine = username + "\\|" + password + "\\|" + newDir;

            fileContents = fileContents.replaceAll(oldLine, newLine);

            FileWriter writer = new FileWriter(filePath);
            writer.append(fileContents);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}