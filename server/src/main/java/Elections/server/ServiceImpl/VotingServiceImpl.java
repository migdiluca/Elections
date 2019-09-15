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
import java.util.concurrent.*;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    private Election electionState;
    private ExecutorService exService;

    public VotingServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public void vote(List<Vote> votes) throws ElectionStateException, RemoteException {
        Future<?> future = exService.submit(() -> {
            if (electionState.getElectionState().equals(ElectionState.FINISHED)) {
                throw new AlreadyFinishedElectionException();
            } else if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
                throw new ElectionsNotStartedException();
            }
            votes.forEach(vote -> electionState.addToVoteList(vote));
            return null;
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException  e) {
            throw new ElectionStateException(e.getMessage());
        }
    }

}
