package Elections.server.ServiceImpl;

import Elections.Exceptions.*;
import Elections.ManagementService;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Pair;;


import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ManagementServiceImpl extends UnicastRemoteObject implements ManagementService {

    private Election election;

    private final Object mutexElectionState = "Election state mutex";

    public ManagementServiceImpl(int port) throws RemoteException {
        super(port);
    }

    Election getElection() {
        return election;
    }

    public ManagementServiceImpl(Election electionState) throws RemoteException {
        this.election = electionState;
    }

    @Override
    public synchronized void openElections() throws ElectionStateException, RemoteException {
        synchronized (mutexElectionState) {
            if (election.getElectionState().equals(ElectionState.FINISHED) || election.getElectionState().equals(ElectionState.CALCULATING))
                throw new AlreadyFinishedElectionException();
            if(election.getElectionState().equals(ElectionState.RUNNING))
                throw new ElectionsAlreadyStartedException();
            election.setElectionState(ElectionState.RUNNING);
        }
    }

    @Override
    public ElectionState getElectionState() throws RemoteException, ServiceException {
        return election.getElectionState();
    }

    @Override
    public void finishElections() throws ElectionStateException, RemoteException {
        synchronized (mutexElectionState) {
            if (election.getElectionState().equals(ElectionState.NOT_STARTED))
                throw new ElectionsNotStartedException();
            if (election.getElectionState().equals(ElectionState.FINISHED) || election.getElectionState().equals(ElectionState.CALCULATING)) {
                throw new AlreadyFinishedElectionException();
            }
            election.setElectionState(ElectionState.CALCULATING);
        }
        VotingSystems votingSystems = new VotingSystems(election.getVotingList());
        election.setDeskFinalResults(votingSystems.calculateDeskResults());
        election.setNationalFinalResults(votingSystems.alternativeVoteNationalLevel());

        Map<Province, List<Pair<BigDecimal, PoliticalParty>>> map = new HashMap<>();
        for (Province p : Province.values()) {
            map.put(p, votingSystems.stVoteProvicialLevel(p));
        }
        election.setProvinceFinalResults(map);

        election.setElectionState(ElectionState.FINISHED);

        notifyEndToClients();
    }

    private void notifyEndToClients() {
        election.getFiscalClients().forEach((Pair, clientList) -> clientList.forEach(client -> {
            try {
                client.endClient();
            } catch (RemoteException e) {
                System.out.println("Remote exception while ending client");
            }
        }));
    }
}
