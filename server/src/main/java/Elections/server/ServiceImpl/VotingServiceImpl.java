package Elections.server.ServiceImpl;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.FiscalCallBack;
import Elections.Models.ElectionState;
import Elections.Models.Vote;
import Elections.VotingService;
import Elections.server.Server;
import Elections.Models.Pair;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.*;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private Election electionState;

    public VotingServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void vote(List<Vote> votes) throws ElectionStateException, RemoteException {
        if (electionState.getElectionState().equals(ElectionState.FINISHED) ||
                electionState.getElectionState().equals(ElectionState.CALCULATING)) {
            throw new AlreadyFinishedElectionException();
        } else if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
            throw new ElectionsNotStartedException();
        }
        votes.forEach(vote -> {
            electionState.addToVoteList(vote);
        });

        votes.forEach(this::notifyVoteToClients);
    }

    @Override
    public void vote(Vote vote) throws ElectionStateException, RemoteException {
        if (electionState.getElectionState().equals(ElectionState.FINISHED) ||
                electionState.getElectionState().equals(ElectionState.CALCULATING)) {
            throw new AlreadyFinishedElectionException();
        } else if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
            throw new ElectionsNotStartedException();
        }
        electionState.addToVoteList(vote);
        logger.info("hasta ahora" + electionState.getVotingList().size());
        notifyVoteToClients(vote);
    }

    private void notifyVoteToClients(Vote vote) {
        vote.getPreferredParties().forEach(politicalParty -> {
            List<FiscalCallBack> clientsToNotify = electionState.getFiscalClients().get(new Pair<>(politicalParty, vote.getDesk()));
            if(clientsToNotify != null) {
                clientsToNotify.forEach(inspectionClient -> {
                    try {
                        inspectionClient.notifyVote();
                    } catch (RemoteException e) {
                        logger.error("Remote exception while notifying client");
                    }
                });
            }
        });
    }
}
