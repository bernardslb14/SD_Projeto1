package Server.ServerClasses;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TCPConnection extends Thread{
    Socket clientSocket;
    int threadNr;
    DataInputStream in;
    DataOutputStream out;
    SharedMemory sm;
    String rootPath;
    String username;
    String password;
    String curDirInit;
    String curDir;
    JSONArray lastDirRequestedContent;

    public TCPConnection(Socket clientSocket, int threadNr, String rootPath, SharedMemory sm) {
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
        try{
            while (true){
                if (sm.isPrimaryServer()){
                    String req = in.readUTF();
                    switch (req) {
                        case "check" -> {
                            okServer(in, out, sm);
                        }

                        case "autenticacao" -> {
                            JSONObject resp = checkUser(in, out, rootPath);
                            if (resp != null) {
                                this.username = resp.getString("username");
                                this.curDir = resp.getString("lastDir");
                                this.curDirInit = resp.getString("lastDir");
                                this.password = resp.getString("password");
                            }
                        }
                        case "changePassword" -> {
                            saveLastDir(username, password, newPassword(in, out, password), curDirInit, curDir, rootPath, sm);
                        }
                        case "changeServerDir" -> {
                            curDir = changeDir(username, in, curDir, lastDirRequestedContent);
                        }
                        case "listServerDir" -> {
                            lastDirRequestedContent = listServerFiles(username, out, curDir, rootPath);
                        }
                    }
                } else {
                    out.writeUTF("");
                }
            }
        } catch (EOFException e) {
            System.out.println("TCPS [ " + threadNr + " ] disconnected.");
            saveLastDir(username, password, password, curDirInit, curDir, rootPath, sm);
        } catch (IOException e) {
            System.out.println("TCPS [ " + threadNr + " ] lost connection.");
            saveLastDir(username, password, password, curDirInit, curDir, rootPath, sm);
        }
    }

    public static void okServer(DataInputStream in, DataOutputStream out, SharedMemory sm) throws IOException{
        if(sm.isPrimaryServer())
            out.writeBoolean(true);
        else
            out.writeBoolean(false);
    }

    public static JSONObject checkUser(DataInputStream in, DataOutputStream out, String rootPath) throws IOException{
        JSONObject dados = new JSONObject(in.readUTF());

        String username = dados.getString("username");
        String pw = dados.getString("password");

        String filePath = rootPath + "clients.txt";
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

            String[] lastDirArr = lastDir.split("");
            for (int i = 0; i < lastDirArr.length; i++){
                if (lastDirArr[i].equals("/"))
                    lastDirArr[i] = "\\";
            }
            lastDir = String.join("", lastDirArr);

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

    public static String newPassword(DataInputStream in, DataOutputStream out, String password) throws IOException{
        JSONObject dados = new JSONObject(in.readUTF());
        String pw = dados.getString("newPW");

        out.writeBoolean(true);

        return pw;
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

    public static JSONArray listServerFiles(String username, DataOutputStream out, String dir, String rootPath) throws IOException{
        String filePath = rootPath + username;
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

    public static void saveLastDir(String username, String oldPassword, String newPassword, String oldDir, String newDir, String rootPath, SharedMemory sm){
        String filePath = rootPath + "clients.txt";

        try(Scanner sc = new Scanner(new File(filePath))) {
            StringBuffer buffer = new StringBuffer();
            while (sc.hasNextLine()) {
                buffer.append(sc.nextLine() + System.lineSeparator());
            }
            String fileContents = buffer.toString();

            String oldLine = username + "|" + oldPassword + "|" + oldDir;
            String newLine = username + "|" + newPassword + "|" + newDir;

            String[] oldLineArr = oldLine.split("");
            for (int i = 0; i < oldLineArr.length; i++){
                if (oldLineArr[i].equals("\\"))
                    oldLineArr[i] = "/";
            }
            oldLine = String.join("", oldLineArr);

            String[] newLineArr = newLine.split("");
            for (int i = 0; i < newLineArr.length; i++){
                if (newLineArr[i].equals("\\"))
                    newLineArr[i] = "/";
            }
            newLine = String.join("", newLineArr);

            fileContents = fileContents.replace(oldLine, newLine);

            FileWriter writer = new FileWriter(filePath);
            writer.append(fileContents);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject op = new JSONObject();
        op.put("filePath", "clients.txt");
        sm.addOperation(op.toString());
    }
}