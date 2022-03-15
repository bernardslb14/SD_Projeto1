package Server.ServerClasses;

import java.util.ArrayList;

public class SharedMemory {
    private boolean isPrimaryServer = false;
    private ArrayList<String> operationsPending = new ArrayList<>();

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
}
