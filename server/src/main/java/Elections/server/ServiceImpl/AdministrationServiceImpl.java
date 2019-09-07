package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Models.ElectionState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AdministrationServiceImpl extends UnicastRemoteObject implements AdministrationService {

    public AdministrationServiceImpl(int port) throws RemoteException {
        super(port);
    }

    public AdministrationServiceImpl() throws RemoteException{

    }

    @Override
    public void openElections() {
        System.out.println("Opening!");
    }

    @Override
    public ElectionState getElectionState() {
        System.out.println(ElectionState.RUNNING);
        return ElectionState.RUNNING;
    }

    @Override
    public void finishElections() {
        System.out.println("Closing!");
    }
}
