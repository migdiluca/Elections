package Elections;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InspectionClient extends Remote {


    /**
     * Notifies vote to political party in its correct desk
     */
    void notifyVote() throws RemoteException;

    void endClient() throws RemoteException;
}
