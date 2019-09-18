package Elections;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ManagementService extends Remote {

    String SERVICE_NAME = "administration_service";

    /**
     * Open elections
     * @throws AlreadyFinishedElectionException if elections already finished
     */
    void openElections() throws RemoteException, ElectionStateException;

    /**
     * Gets the election state
     */
    ElectionState getElectionState() throws RemoteException, ServiceException;

    /**
     * Finish elections, users cannot vote once this function runs
     * @throws ElectionsNotStartedException if elections did not start
     * @throws AlreadyFinishedElectionException if elections already finished
     */
    void finishElections() throws RemoteException, ElectionStateException;

}
