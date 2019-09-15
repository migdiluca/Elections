package Elections.server.ServiceImpl;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;
import Elections.Models.Vote;
import Elections.VotingService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    private Election electionState;

    public VotingServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void vote(List<Vote> votes) throws ElectionStateException, RemoteException {
        if (electionState.getElectionState().equals(ElectionState.FINISHED)){
            throw new AlreadyFinishedElectionException();
        }
        else if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)){
            throw new ElectionsNotStartedException();
        }
        votes.forEach(vote -> electionState.addToVoteList(vote));
    }

}
