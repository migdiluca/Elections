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
    public synchronized void openElections() throws ElectionStateException, RemoteException {
        if(electionState.getElectionState().equals(ElectionState.FINISHED))
            throw new AlreadyFinishedElectionException();
        electionState.setElectionState(ElectionState.RUNNING);
    }

    @Override
    public ElectionState getElectionState() throws RemoteException {
        return electionState.getElectionState();
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
