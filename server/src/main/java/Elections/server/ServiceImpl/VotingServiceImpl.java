package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.Models.Vote;
import Elections.VotingService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    //private VotingSingleton vt;

    public VotingServiceImpl(int port) throws RemoteException {
        super(port);
    }

    public VotingServiceImpl() throws RemoteException{

    }

    @Override
    public void vote(List<Vote> votes) throws ElectionStateException {
        System.out.println(votes);
    }
}
