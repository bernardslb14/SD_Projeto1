import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import org.json.JSONObject;

public class ucDriveClient {
    public static void main(String args[]){
        try(Socket s = new Socket("localhost", 6000)){
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String username = "username2";
            String password = "password";


            try(Scanner sc = new Scanner(System.in)){
                /*
                out.writeUTF(sc.nextLine());
                System.out.println(in.readUTF());
                */

                // Func 4
                listServerFiles(in, out);

                // Func 6
                listLocalFiles("username2", "");
            }
        } catch (UnknownHostException | EOFException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void listServerFiles(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeUTF("listCurrDir");
        JSONObject filesList = new JSONObject(in.readUTF());
    }

    public static void listLocalFiles(String username, String dir){
        String filePath = ".\\src\\ClientDir\\" + username;
        if (!dir.isEmpty())
            filePath += "\\" + dir;

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
            System.out.println(String.valueOf(file).substring(filePath.length() + 1));
        }

        //Caso a diretoria esteja vazia
        if (fileList.length == 0)
            System.out.println("(Empty Dir)");
    }

    //TODO:https://www.w3schools.com/java/java_files_create.asp
    public static void uploadFile(){

    }
}
