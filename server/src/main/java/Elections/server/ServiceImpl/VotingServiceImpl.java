package Elections.server.ServiceImpl;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.FiscalCallBack;
import Elections.Models.ElectionState;
import Elections.Models.Vote;
import Elections.VotingService;
import Elections.server.Server;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.*;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    private static Logger logger = LoggerFactory.getLogger(Server.class);
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
            logger.info("Total votes uploaded by now: " + electionState.getVotingList().size());
        } catch (InterruptedException | ExecutionException  e) {
            throw new ElectionStateException(e.getCause().getMessage());
        }
    }

    @Override
    public void vote(Vote vote) throws ElectionStateException, RemoteException {
        if (electionState.getElectionState().equals(ElectionState.FINISHED)){
            throw new AlreadyFinishedElectionException();
        }
        else if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)){
            throw new ElectionsNotStartedException();
        }
        electionState.addToVoteList(vote);
    }

    private void notifyVoteToClients(Vote vote) {
        exService.submit(() -> {
            vote.getPreferredParties().forEach(politicalParty -> {
                List<FiscalCallBack> clientsToNotify = electionState.getFiscalClients().get(new Pair<>(politicalParty, vote.getDesk()));
                clientsToNotify.forEach(inspectionClient -> {
                    try {
                        inspectionClient.notifyVote();
                    } catch (RemoteException e) {
                        logger.error("Remote exception while notifying client");
                    }
                });
            });
        });
    }
}
