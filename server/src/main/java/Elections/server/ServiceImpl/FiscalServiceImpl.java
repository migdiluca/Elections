package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.FiscalCallBack;
import Elections.FiscalService;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.server.Server;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private final Object addClientMutex = "Add client mutex";

    public FiscalServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public void addInspector(FiscalCallBack fiscalCallBack, PoliticalParty party, int desk) throws RemoteException, ElectionStateException {
        if(!electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
            try {
                fiscalCallBack.submitError(electionState.getElectionState());
            } catch (RemoteException e) {
                System.out.println("Cannot add insector when elections are already started or ended");
            }
            return;
        }

        Future<?> future = exService.submit(() -> {
            Pair<PoliticalParty, Integer> votePair = new Pair<>(party, desk);
            synchronized (addClientMutex) {
//                electionState.getFiscalClients().computeIfPresent(votePair, (key, clientsList) -> {
//                        clientsList.add(fiscalCallBack);
//                    return clientsList;
//                });
//                electionState.getFiscalClients().computeIfAbsent(votePair, clientsList -> new ArrayList<>()).add(fiscalCallBack);

                if(electionState.getFiscalClients().containsKey(votePair)){
                    electionState.getFiscalClients().get(votePair).add(fiscalCallBack);
                }
                else {
                    List<FiscalCallBack> list = new ArrayList<>();
                    list.add(fiscalCallBack);
                    electionState.getFiscalClients().put(votePair, list);
                }
            }
        });
        try {
            future.get();
            logger.info("A fiscal has been registered in desk " + desk + " for party " + party.name());
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getCause().getMessage());
        }

    }

}
