package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.InspectionClient;
import Elections.FiscalService;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import javafx.util.Pair;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FiscalServiceImpl extends UnicastRemoteObject implements FiscalService {

    private Election electionState;
    private ExecutorService exService;

    public FiscalServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public void addInspector(InspectionClient inspectionClient, PoliticalParty party, int table) throws RemoteException, ElectionStateException {
        if(!electionState.getElectionState().equals(ElectionState.NOT_STARTED)){
            inspectionClient.submitError(electionState.getElectionState());
            return;
        }

        Future<?> future = exService.submit(() -> {
            Pair<PoliticalParty, Integer> votePair = new Pair<>(party, table);
            electionState.getFiscalClients().computeIfPresent(votePair, (key, clientsList) -> {
                clientsList.add(inspectionClient);
                return clientsList;
            });
            electionState.getFiscalClients().computeIfAbsent(votePair, clientsList -> Collections.synchronizedList(new ArrayList<>())).add(inspectionClient);
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getMessage());
        }
    }

}
