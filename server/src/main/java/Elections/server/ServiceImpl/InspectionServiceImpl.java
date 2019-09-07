package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.InspectionService;
import Elections.Models.PoliticalParty;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class InspectionServiceImpl extends UnicastRemoteObject implements InspectionService {

    private ElectionPOJO electionState;

    public InspectionServiceImpl(ElectionPOJO electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void addInspector(PoliticalParty party, int desk) throws RemoteException, ElectionStateException {

    }
}
