package Server.ServerClasses;

import java.util.ArrayList;

public class SharedMemory {
    private boolean isPrimaryServer = false;
    private ArrayList<String> operationsPending = new ArrayList<>();
    private String rootPath = "";

    public SharedMemory() {}

    synchronized boolean isPrimaryServer() {
        return isPrimaryServer;
    }

    synchronized void setPrimaryServer(boolean primaryServer) {
        isPrimaryServer = primaryServer;
    }

    synchronized ArrayList<String> getOperationsPending() {
        return operationsPending;
    }

    synchronized void setOperationsPending(ArrayList<String> operationsPending) {
        this.operationsPending = operationsPending;
    }

    synchronized void addOperation(String s){
        this.operationsPending.add(s);
    }

    synchronized String getOperation(){
        String op = this.operationsPending.get(0);
        return op;
    }

    synchronized boolean remOperation(String s){
        return this.operationsPending.remove(s);
    }

    synchronized boolean isOperationsPendingEmpty(){
        return this.operationsPending.isEmpty();
    }

    synchronized void setRootPath(String r){
        this.rootPath = r;
    }

    synchronized String getRootPath(){
        return this.rootPath;
    }
}
