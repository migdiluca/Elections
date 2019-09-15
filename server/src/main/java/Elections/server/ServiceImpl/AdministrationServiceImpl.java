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
    public void openElections() throws ElectionStateException, RemoteException {
        if(electionState.getElectionState().equals(ElectionState.FINISHED))
            throw new AlreadyFinishedElectionException();
        electionState.setElectionState(ElectionState.RUNNING);
    }

    @Override
    public ElectionState getElectionState() throws RemoteException {
        return electionState.getElectionState();
    }

    @Override
    public void finishElections() throws ElectionStateException, RemoteException {
        if(electionState.getElectionState().equals(ElectionState.NOT_STARTED))
            throw new ElectionsNotStartedException();
        if(electionState.getElectionState().equals(ElectionState.FINISHED)){
            throw new AlreadyFinishedElectionException();
        }
        electionState.setElectionState(ElectionState.FINISHED);
    }
}
