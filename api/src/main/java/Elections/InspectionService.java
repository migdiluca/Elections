package Elections;


import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsAlreadyStartedException;
import Elections.Models.PoliticalParty;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InspectionService extends Remote {

    String SERVICE_NAME = "inspection_service";

    /**
     * If already open
     * @throws ElectionsAlreadyStartedException if elections have started
     * @throws AlreadyFinishedElectionException if elections have finished
     */
    void addInspector(InspectionClient inspectionClient, PoliticalParty party, int table) throws RemoteException, ElectionStateException;

}
