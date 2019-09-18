package Elections;

import Elections.Models.ElectionState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FiscalCallBack extends Remote {

    /**
     * Notifies vote to political party in its correct desk
     */
    void notifyVote() throws RemoteException;

    /**
     * Notifies that elections have ended, in consequence client stops execution
     */
    void endClient() throws RemoteException;

    /**
     * Notifies an error
     * @param electionState the state of the election
     */
    void submitError(ElectionState electionState) throws RemoteException;
}
