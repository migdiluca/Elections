package Elections.server.ServiceImpl;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.InspectionClient;
import Elections.Models.ElectionState;
import Elections.Models.Vote;
import Elections.VotingService;
import javafx.util.Pair;

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
            votes.forEach(vote -> {
                electionState.addToVoteList(vote);
                notifyVoteToClients(vote);
            });
            return null;
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException  e) {
            throw new ElectionStateException(e.getMessage());
        }
    }

    private void notifyVoteToClients(Vote vote){
        vote.getPreferredParties().forEach(politicalParty -> {
            List<InspectionClient> clientsToNotify = electionState.getFiscalClients().get(new Pair<>(politicalParty, vote.getTable()));
            clientsToNotify.forEach(inspectionClient -> {
                try{
                    inspectionClient.notifyVote();
                } catch (RemoteException e){
                    System.out.println("Remote exception while notifying votes");
                }
            });
        });
    }
}
