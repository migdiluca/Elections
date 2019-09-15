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
        votes.forEach(vote -> {
            electionState.addToVoteList(vote);
            notifyVoteToClients(vote);
        });
    }

    private void notifyVoteToClients(Vote vote){
        vote.getPreferredParties().forEach(politicalParty -> {
            List<InspectionClient> clientsToNotify = electionState.getFiscalClients().get(new Pair<>(politicalParty, vote.getTable()));
            clientsToNotify.forEach(inspectionClient -> {
                try{
                    inspectionClient.notifyVote();
                } catch (RemoteException e){
                    System.out.println("Remote exception on server side for InspectionService");
                }
            });
        });
    }
}
