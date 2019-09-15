package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdministrationServiceImpl extends UnicastRemoteObject implements AdministrationService {

    private Election electionState;
    private ExecutorService exService;

    public AdministrationServiceImpl(int port) throws RemoteException {
        super(port);
        exService = Executors.newFixedThreadPool(12);
    }

    public AdministrationServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
        exService = Executors.newFixedThreadPool(12);
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
