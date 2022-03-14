import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class ucDriveClient {
    public static void main(String args[]){
        try(Socket s = new Socket("localhost", 6000)){
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String username;


            try(Scanner sc = new Scanner(System.in)){

                // Func 1 FEITO
                //TODO: Encriptacao de passwords
                username = autenticacao(sc, in, out);

                // Func 4 e 5 FEITO
                navigateServerFiles(in, out, sc);

                // Func 6 e 7 FEITO
                //navigateLocalFiles(username, sc);
            }
        } catch (UnknownHostException | EOFException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String autenticacao(Scanner sc, DataInputStream in, DataOutputStream out) throws IOException{
        out.writeUTF("autenticacao");

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

    public static JSONArray listServerFiles(DataInputStream in, DataOutputStream out) throws IOException {
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

        return filesList;
    }

    public static void navigateServerFiles(DataInputStream in, DataOutputStream out, Scanner sc) throws IOException{
        String buff;
        while (true){
            JSONArray curDirList = listServerFiles(in, out);

            System.out.println(">> Type 'exit' to quit Local Explorer");
            System.out.println(">> Type <dirName> or <fileName> to open folder or download file");
            System.out.println(">> Type '..' to go back");
            System.out.print("> ");

            buff = sc.nextLine();

            if (buff.equals("exit"))
                break;

            Boolean foundFile = false;
            for (int i = 0; i < curDirList.length(); i++){
                JSONObject elem = curDirList.getJSONObject(i);
                if (!elem.getString("name").equals(buff))
                    continue;
                if (elem.getBoolean("isFile")){
                    foundFile = true;
                }
                break;
            }

            out.writeUTF("changeServerDir");
            out.writeUTF(buff);

            if (foundFile)
                downloadFile(buff);
        }
    }

    public static JSONArray listLocalFiles(String username, String dir){
        String filePath = ".\\src\\ClientDir\\" + username;
        JSONArray filesList = new JSONArray();
        if (!dir.isEmpty())
            filePath += dir;

        File path = new File(filePath);

        //Caso nao exista a diretoria
        if (!path.exists())
            path.mkdir();

        File[] fileList = path.listFiles();
        System.out.println(filePath.substring(16) + "\\..");
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

    public static void navigateLocalFiles(String username, Scanner sc){
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

            Boolean foundFile = false;
            for (int i = 0; i < curDirList.length(); i++){
                JSONObject elem = curDirList.getJSONObject(i);
                if (!elem.getString("name").equals(buff))
                    continue;
                if (elem.getBoolean("isDirectory")){
                    dir += "\\" + buff;
                }
                if (elem.getBoolean("isFile")){
                    uploadFile();
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

    //TODO:https://www.w3schools.com/java/java_files_create.asp
    public static void uploadFile(){

    }
    //TODO:https://www.w3schools.com/java/java_files_create.asp
    public static void downloadFile(String fileName){

    }
}
