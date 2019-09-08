package Elections;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InspectionClient extends Remote {
    void notifyVote() throws RemoteException;
}
