package Elections.server.ServiceImpl;

import Elections.ManagementService;
import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import javafx.util.Pair;


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
    private ExecutorService exService;
    private VotingSystems votingSystems;

    public ManagementServiceImpl(int port) throws RemoteException {
        super(port);
        exService = Executors.newFixedThreadPool(12);
    }

    Election getElection() {
        return election;
    }

    public ManagementServiceImpl(Election electionState) throws RemoteException {
        this.election = electionState;
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public synchronized void openElections() throws ElectionStateException, RemoteException {
        if (election.getElectionState().equals(ElectionState.FINISHED))
            throw new AlreadyFinishedElectionException();
        election.setElectionState(ElectionState.RUNNING);
    }

    @Override
    public ElectionState getElectionState() throws RemoteException, ServiceException {
        Future<ElectionState> future = exService.submit(() -> election.getElectionState());
        ElectionState state;
        try {
            state = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException();
        }
        return state;
    }

    @Override
    public synchronized void finishElections() throws ElectionStateException, RemoteException {
        if (election.getElectionState().equals(ElectionState.NOT_STARTED))
            throw new ElectionsNotStartedException();
        if (election.getElectionState().equals(ElectionState.FINISHED)) {
            throw new AlreadyFinishedElectionException();
        }
        election.setElectionState(ElectionState.FINISHED);

        this.votingSystems = new VotingSystems(election.getVotingList());
        election.setDeskFinalResults(votingSystems.calculateDeskResults());
        election.setNationalFinalResults(votingSystems.alternativeVoteNationalLevel());

        //FIXME: hardcodeado para testeo

        Map<Province, List<Pair<BigDecimal, PoliticalParty>>> map = new HashMap<>();
        for (Province p: Province.values()) {
            List<Pair<BigDecimal, PoliticalParty>> l = new ArrayList<>();
            l.add(new Pair<>(new BigDecimal(50.0), PoliticalParty.BUFFALO));
            map.put(p,l);
        }
        election.setProvinceFinalResults(map);

        notifyEndToClients();
    }

    private void notifyEndToClients() {
        election.getFiscalClients().forEach((pair, clientList) -> {
            clientList.forEach(client -> {
                try {
                    client.endClient();
                } catch (RemoteException e) {
                    System.out.println("Remote exception while ending client");
                }
            });
        });
    }
}
