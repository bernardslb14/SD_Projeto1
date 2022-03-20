package Client;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLOutput;
import java.util.Scanner;


public class ucDriveClient {
    public static void main(String[] args){
        String username;
        int portA = 6000;
        int portB = 7000;

        String ipA = "localhost";
        String ipB = "localhost";

        int opcao = -1;

        while(true) {
                Scanner sc = new Scanner(System.in);
                System.out.println("MENU PRIMÁRIO\n[1] Login\n[2] Alterar configurações de conexão");

                switch (sc.nextInt()){
                    case 1 -> {
                        try (Socket s = new Socket(ipA, portA)) {
                            DataInputStream in = new DataInputStream(s.getInputStream());
                            DataOutputStream out = new DataOutputStream(s.getOutputStream());


                            while (true) {
                                if (!checkServer(in, out)) {
                                    System.out.println("Conecta-te ao servidor primário!");
                                    break;
                                }
                                // Func 1 FEITO
                                //TODO: Encriptacao de passwords
                                //      Feedback de autenticacao falhada (dados errados)
                                username = autenticacao(sc, in, out);

                                while (opcao != 1 && opcao != 0) {
                                    System.out.println("\t\t[1] Mudar password\n\t[2] Navegar pelos ficheiros do Server\n\t[3] Navegar pelos ficheiros Locais\n\t[0] Sair");
                                    opcao = sc.nextInt();

                                    switch (opcao) {
                                        case 1 -> {
                                            //Func 2
                                            changePassword(sc, in, out);
                                        }
                                        case 2 -> {
                                            // Func 4, 5, 8 FEITO
                                            navigateServerFiles(username, in, out, sc, portA);
                                        }
                                        case 3 -> {
                                            // Func 6, 7, 9 FEITO
                                            navigateLocalFiles(username, sc, portA);
                                        }
                                    }

                                }
                            }

                        } catch (ConnectException ce) {
                            System.out.println("Server not found.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    case 2 -> {
                        System.out.println("\t[1] Server A -> " + ipA + ":" + portA  + "\n\t[2] Server B -> " + ipB + ":" + portB);
                        switch (sc.nextInt()){
                            case 1 -> {
                                //sc.nextLine();
                                sc = new Scanner(System.in);
                                System.out.println("xxx.xxx.xxx.xxx:xxxx");
                                String input = sc.nextLine();
                                String[] aux = input.split(":");
                                ipA = aux[0];
                                portA = Integer.parseInt(aux[1]);
                            }
                            case 2 -> {
                                //sc.nextLine();
                                sc = new Scanner(System.in);
                                System.out.println("xxx.xxx.xxx.xxx:xxxx");
                                String input = sc.nextLine();
                                String[] aux = input.split(":");

                                ipB = aux[0];
                                portB = Integer.parseInt(aux[1]);
                            }
                        }
                    }
                }
        }
    }

    public static boolean checkServer (DataInputStream in, DataOutputStream out) throws IOException {
        out.writeUTF("check");

        return in.readBoolean();
    }

    public static String autenticacao(Scanner sc, DataInputStream in, DataOutputStream out) throws IOException{
        out.writeUTF("autenticacao");

        sc = new Scanner(System.in);
        System.out.println("Username: ");
        String username = sc.nextLine();

        System.out.println("Password: ");
        String password = sc.nextLine();

        JSONObject aut = new JSONObject();
        aut.put("username", username);
        aut.put("password", password);

        out.writeUTF(aut.toString());

        if (in.readBoolean()){
            System.out.println("Success");
            return username;
        }
        else {
            System.out.println("Fail");
            return null;
        }
    }

    public static void changePassword(Scanner sc, DataInputStream in, DataOutputStream out) throws IOException {
        out.writeUTF("changePassword");

        System.out.println("");
        sc = new Scanner(System.in);

        System.out.println("New Password: ");
        String newPW = sc.nextLine();

        JSONObject aut = new JSONObject();
        aut.put("newPW", newPW);

        out.writeUTF(aut.toString());

        if(in.readBoolean())
            System.out.println("OK");
    }

    public static JSONObject listServerFiles(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeUTF("listServerDir");
        String dir = in.readUTF();
        JSONArray filesList = new JSONArray(in.readUTF());

        System.out.println(dir);
        for (int i = 0; i < filesList.length(); i++) {
            JSONObject elem = filesList.getJSONObject(i);
            if (elem.getBoolean("isDirectory"))
                System.out.print("\uD83D\uDCC1 ");
            if (elem.getBoolean("isFile"))
                System.out.print("\uD83D\uDCC3 ");
            System.out.println(elem.getString("name"));
        }

        //Caso a diretoria esteja vazia
        if (filesList.length() == 0)
            System.out.println("(Empty Dir)");

        JSONObject r = new JSONObject();
        r.put("curDirArr", filesList);
        r.put("curDir", dir.substring(4, dir.length()-3));
        return r;
    }

    public static void navigateServerFiles(String username, DataInputStream in, DataOutputStream out, Scanner sc, int port) throws IOException{
        String buff;
        while (true){
            JSONObject info = listServerFiles(in, out);
            JSONArray curDirList = info.getJSONArray("curDirArr");
            String curServerDir = info.getString("curDir");

            System.out.println(">> Type 'exit' to quit Local Explorer");
            System.out.println(">> Type <dirName> or <fileName> to open folder or download file");
            System.out.println(">> Type '..' to go back");
            System.out.print("> ");

            buff = sc.nextLine();

            if (buff.equals("exit"))
                break;

            for (int i = 0; i < curDirList.length(); i++){
                JSONObject elem = curDirList.getJSONObject(i);
                if (!elem.getString("name").equals(buff))
                    continue;
                if (elem.getBoolean("isFile")){
                    downloadFile(username, curServerDir, buff, port);
                    continue;
                }
                break;
            }

            out.writeUTF("changeServerDir");
            out.writeUTF(buff);
        }
    }

    public static JSONArray listLocalFiles(String username, String dir){
        String filePath = ".\\ClientDir\\" + username;
        JSONArray filesList = new JSONArray();
        if (!dir.isEmpty())
            filePath += dir;

        File path = new File(filePath);

        //Caso nao exista a diretoria
        if (!path.exists())
            path.mkdir();

        File[] fileList = path.listFiles();
        System.out.println(filePath.substring(12) + "\\..");
        for (File file: fileList) {
            if (file.isDirectory())
                System.out.print("\uD83D\uDCC1 ");
            if (file.isFile())
                System.out.print("\uD83D\uDCC3 ");
            String fileName = String.valueOf(file).substring(filePath.length() + 1);
            System.out.println(fileName);

            JSONObject elem = new JSONObject();
            elem.put("isDirectory", file.isDirectory());
            elem.put("isFile", file.isFile());
            elem.put("name", fileName);
            filesList.put(elem);
        }

        //Caso a diretoria esteja vazia
        if (fileList.length == 0)
            System.out.println("(Empty Dir)");

        return filesList;
    }

    public static void navigateLocalFiles(String username, Scanner sc, int port){
        String dir = "";
        String buff;

        while (true){
            JSONArray curDirList = listLocalFiles(username, dir);
            System.out.println(">> Type 'exit' to quit Local Explorer");
            System.out.println(">> Type <dirName> or <fileName> to open folder or upload file");
            System.out.println(">> Type '..' to go back");
            System.out.print("> ");
            buff = sc.nextLine();

            if (buff.equals("exit")){
                break;
            }
            if (buff.equals("..")){
                String[] DirArr = dir.split("\\\\");
                dir = "";
                for (int i = 1; i < DirArr.length - 1; i++){
                    dir += "\\" + DirArr[i];
                }
                continue;
            }

            boolean foundFile = false;
            for (int i = 0; i < curDirList.length(); i++){
                JSONObject elem = curDirList.getJSONObject(i);
                if (!elem.getString("name").equals(buff))
                    continue;
                if (elem.getBoolean("isDirectory")){
                    dir += "\\" + buff;
                }
                if (elem.getBoolean("isFile")){
                    uploadFile(username, dir, buff, port);
                }
                foundFile = true;
                break;
            }
            if (foundFile)
                continue;

            //TODO:Criar pasta nova??
            System.out.println(">> Invalid input.");
        }
    }

    //TODO: Deixar escolher a diretoria destino
    public static void uploadFile(String username, String originFileDir, String originFileName, int port){
        try(Socket FTS = new Socket("localhost", port + 1)){
            DataInputStream in = new DataInputStream(FTS.getInputStream());
            DataOutputStream out = new DataOutputStream(FTS.getOutputStream());

            JSONObject req = new JSONObject();
            req.put("operation", "uploadToServer");
            req.put("username", username);
            req.put("localPath", originFileDir);
            req.put("localName", originFileName);
            req.put("serverPath","");
            req.put("serverName",originFileName);

            out.writeUTF(req.toString());

            int bytes;
            File file = new File(".\\ClientDir\\" + username + "\\" + originFileDir + "\\" + originFileName);
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

            System.out.println("\nUPLOAD COMPLETED");

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //TODO: Deixar escolher a diretoria destino
    public static void downloadFile(String username, String originFileDir, String originFileName, int port){
        try(Socket FTS = new Socket("localhost", port + 1)){
            DataInputStream in = new DataInputStream(FTS.getInputStream());
            DataOutputStream out = new DataOutputStream(FTS.getOutputStream());

            String destinFileDir = "";

            JSONObject req = new JSONObject();
            req.put("operation", "downloadFromServer");
            req.put("username", username);
            req.put("localPath", destinFileDir);
            req.put("localName", originFileName);
            req.put("serverPath",originFileDir);
            req.put("serverName",originFileName);

            out.writeUTF(req.toString());

            int bytes;
            FileOutputStream fileOutputStream = new FileOutputStream(".\\ClientDir\\" + username + "\\" + destinFileDir + "\\" + originFileName);

            long size = in.readLong();     // read file size
            byte[] buffer = new byte[4*1024];
            while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer,0,bytes);
                size -= bytes;      // read upto file size
            }
            fileOutputStream.close();

            System.out.println("\nDOWNLOAD COMPLETED");

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
