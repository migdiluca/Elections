package Elections;

import Elections.Models.ElectionState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InspectionClient extends Remote {


    /**
     * Notifies vote to political party in its correct desk
     */
    void notifyVote() throws RemoteException;

    void endClient() throws RemoteException;

    void submitError(ElectionState electionState) throws RemoteException;
}
