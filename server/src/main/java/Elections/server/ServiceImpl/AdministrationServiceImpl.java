package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AdministrationServiceImpl extends UnicastRemoteObject implements AdministrationService {

    private Election election;
    private ExecutorService exService;

    public AdministrationServiceImpl(int port) throws RemoteException {
        super(port);
        exService = Executors.newFixedThreadPool(12);
    }

    Election getElection() {
        return election;
    }

    public AdministrationServiceImpl(Election electionState) throws RemoteException {
        this.election = electionState;
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public synchronized void openElections() throws ElectionStateException, RemoteException {
        if (election.getElectionState().equals(ElectionState.FINISHED))
            throw new AlreadyFinishedElectionException();
        election.setElectionState(ElectionState.RUNNING);
    }

    @Override
    public ElectionState getElectionState() throws RemoteException, ServiceException {
        Future<ElectionState> future = exService.submit(() -> election.getElectionState());
        ElectionState state;
        try {
            state = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException();
        }
        return state;
    }

    @Override
    public synchronized void finishElections() throws ElectionStateException, RemoteException {
        if (election.getElectionState().equals(ElectionState.NOT_STARTED))
            throw new ElectionsNotStartedException();
        if (election.getElectionState().equals(ElectionState.FINISHED)) {
            throw new AlreadyFinishedElectionException();
        }
        election.setElectionState(ElectionState.FINISHED);
        notifyEndToClients();
    }

    private void notifyEndToClients() {
        election.getFiscalClients().forEach((pair, clientList) -> {
            clientList.forEach(client -> {
                try {
                    client.endClient();
                } catch (RemoteException e) {
                    System.out.println("Remote exception while ending client");
                }
            });
        });
    }
}
