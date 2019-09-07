package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.Vote;
import Elections.VotingService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    private ElectionPOJO electionState;

    public VotingServiceImpl(ElectionPOJO electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void vote(Vote vote) throws ElectionStateException {

    }
}
