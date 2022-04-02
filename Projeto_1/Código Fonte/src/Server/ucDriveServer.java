package Server;

import Server.ServerClasses.*;

public class ucDriveServer {
    public static void main(String[] args){
        // Shared Memory
        SharedMemory sm = new SharedMemory();

        //Start UDP Checking Service
        new UDPService(sm);

        //Start TCP Service
        new TCPServer(sm, 6000, 7000);

        //Start TCP File Transfer Service
        new TCPFileTransfer(sm, 6001, 7001);
    }
}