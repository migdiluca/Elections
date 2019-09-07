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
     * @throws ElectionsAlreadyStartedException or
     * @throws AlreadyFinishedElectionException
     *
     * hay que ver como implementar el servicio remoto pavisar que hubo un voto
     */
    void addInspector(PoliticalParty party, int desk) throws RemoteException, ElectionStateException;


}
