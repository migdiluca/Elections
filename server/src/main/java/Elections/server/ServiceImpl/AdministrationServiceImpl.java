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
    public synchronized void openElections() throws ElectionStateException, RemoteException {
        if(electionState.getElectionState().equals(ElectionState.FINISHED))
            throw new AlreadyFinishedElectionException();
        electionState.setElectionState(ElectionState.RUNNING);
    }

    @Override
    public ElectionState getElectionState() throws RemoteException, ServiceException {
        Future<ElectionState> future = exService.submit(() -> electionState.getElectionState());
        ElectionState state = null;
        try {
            state = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException();
        }
        return state;
    }

    @Override
    public synchronized void finishElections() throws ElectionStateException, RemoteException {
        if(electionState.getElectionState().equals(ElectionState.NOT_STARTED))
            throw new ElectionsNotStartedException();
        if(electionState.getElectionState().equals(ElectionState.FINISHED)){
            throw new AlreadyFinishedElectionException();
        }
        electionState.setElectionState(ElectionState.FINISHED);
        notifyEndToClients();
    }

    private void notifyEndToClients(){
        electionState.getFiscalClients().forEach((pair,clientList) -> {
            clientList.forEach(client -> {
                try{
                    client.endClient();
                } catch (RemoteException e){
                    System.out.println("Remote exception while ending client");
                }
            });
        });
    }
}
