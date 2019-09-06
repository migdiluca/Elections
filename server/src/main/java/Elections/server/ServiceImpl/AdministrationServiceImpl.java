package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Models.ElectionState;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AdministrationServiceImpl extends UnicastRemoteObject implements AdministrationService {

    public AdministrationServiceImpl() throws RemoteException {

    }

    @Override
    public void openElections() {

    }

    @Override
    public ElectionState getElectionState() {
        return null;
    }

    @Override
    public void finishElections() {

    }
}
