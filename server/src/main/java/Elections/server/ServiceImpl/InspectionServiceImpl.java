package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ServiceException;
import Elections.InspectionClient;
import Elections.InspectionService;
import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
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

public class InspectionServiceImpl extends UnicastRemoteObject implements InspectionService {

    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private Election electionState;
    private ExecutorService exService;

    private Map<Pair<PoliticalParty, Integer>, List<InspectionClient>> clients;

    public InspectionServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
        clients = Collections.synchronizedMap(new HashMap<>());
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public void addInspector(InspectionClient inspectionClient, PoliticalParty party, int table) throws RemoteException, ElectionStateException {
        Future<?> future = exService.submit(() -> {
            Pair<PoliticalParty, Integer> votePair = new Pair<>(party, table);
            clients.computeIfPresent(votePair, (key, clientsList) -> {
                clientsList.add(inspectionClient);
                return clientsList;
            });
            clients.computeIfAbsent(votePair, clientsList -> Collections.synchronizedList(new ArrayList<>())).add(inspectionClient);
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getMessage());
        }

//      VERSION EN JAVA 7
//        if(clients.containsKey(votePair)){
//            clients.get(votePair).add(inspectionClient);
//        } else {
//            List<InspectionClient> clientsToNotify = new ArrayList<>();
//            clientsToNotify.add(inspectionClient);
//            clients.put(new Pair<>(party,table),clientsToNotify);
//        }
    }

    public void notifyVoteToClients(Vote vote) throws ServiceException {
        Future<?> future = exService.submit(() -> {
            vote.getPreferredParties().forEach(politicalParty -> {
                List<InspectionClient> clientsToNotify = clients.get(new Pair<>(politicalParty, vote.getTable()));
                clientsToNotify.forEach(inspectionClient -> {
                    try {
                        inspectionClient.notifyVote();
                    } catch (RemoteException e) {
                        logger.error("Remote exception on server side for InspectionService");
                    }
                });
            });
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException  e) {
            throw new ServiceException();
        }

//        VERSION EN JAVA 7
//        for(PoliticalParty politicalParty : vote.getPreferredParties()){
//            List<InspectionClient> clientsToNotify = clients.get(new Pair<>(politicalParty, vote.getTable()));
//            for(InspectionClient inspectionClient : clientsToNotify){
//                try{
//                    inspectionClient.notifyVote();
//                } catch (RemoteException e){
//                    System.out.println("Remote exception on server side for InspectionService");
//                }
//            }
//        }
    }
}
