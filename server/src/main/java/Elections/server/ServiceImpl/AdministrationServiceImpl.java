package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
//        Future<?> future = exService.submit(() -> {
            if (electionState.getElectionState().equals(ElectionState.FINISHED))
                throw new AlreadyFinishedElectionException();
            electionState.setElectionState(ElectionState.RUNNING);
//            return null;
//        });
//        try {
//            future.get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new ElectionStateException(e.getMessage());
//        }
    }

    @Override
    public ElectionState getElectionState() throws RemoteException {
        Future<ElectionState> future = exService.submit(() -> electionState.getElectionState());
        ElectionState state = null;
        try {
            state = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return state;
    }

    @Override
    public void finishElections() throws ElectionStateException, RemoteException {
        if (electionState.getElectionState().equals(ElectionState.NOT_STARTED))
            throw new ElectionsNotStartedException();
        if (electionState.getElectionState().equals(ElectionState.FINISHED)) {
            throw new AlreadyFinishedElectionException();
        }
        electionState.setElectionState(ElectionState.FINISHED);
    }
}
