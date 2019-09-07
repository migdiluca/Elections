package Elections;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdministrationService extends Remote {
    String SERVICE_NAME = "administration_service";

    /**
     * If already finished
     * @throws AlreadyFinishedElectionException (o un nombre mas piolin)
     */
    void openElections() throws RemoteException;

    ElectionState getElectionState() throws RemoteException, ElectionStateException;

    /**
     * If elections did not start
     * @throws ElectionsNotStartedException (o un nombre mas piolin)
     */
    void finishElections() throws RemoteException, ElectionStateException;

}
