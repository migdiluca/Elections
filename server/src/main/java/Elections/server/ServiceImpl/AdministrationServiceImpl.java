package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AdministrationServiceImpl extends UnicastRemoteObject implements AdministrationService {

    private Election electionState;

    public AdministrationServiceImpl(int port) throws RemoteException {
        super(port);
    }

    public AdministrationServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void openElections() throws ElectionStateException {
        if(electionState.getElectionState().equals(ElectionState.FINISHED))
            throw new AlreadyFinishedElectionException();
        electionState.setElectionState(ElectionState.RUNNING);
    }

    @Override
    public ElectionState getElectionState() {
        return electionState.getElectionState();
    }

    @Override
    public void finishElections() throws ElectionStateException {
        if(electionState.getElectionState().equals(ElectionState.NOT_STARTED))
            throw new ElectionsNotStartedException();
        electionState.setElectionState(ElectionState.FINISHED);
    }
}
