package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.InspectionClient;
import Elections.FiscalService;
import Elections.Models.PoliticalParty;
import javafx.util.Pair;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class FiscalServiceImpl extends UnicastRemoteObject implements FiscalService {

    private Election electionState;

    public FiscalServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void addInspector(InspectionClient inspectionClient, PoliticalParty party, int table) throws RemoteException, ElectionStateException {
        Pair<PoliticalParty, Integer> votePair = new Pair<>(party,table);

        electionState.getFiscalClients().computeIfPresent(votePair, (key, clientsList) -> {
            clientsList.add(inspectionClient);
            return clientsList;
        });
        electionState.getFiscalClients().computeIfAbsent(votePair, clientsList -> Collections.synchronizedList(new ArrayList<>())).add(inspectionClient);

//      VERSION EN JAVA 7
//        if(clients.containsKey(votePair)){
//            clients.get(votePair).add(inspectionClient);
//        } else {
//            List<InspectionClient> clientsToNotify = new ArrayList<>();
//            clientsToNotify.add(inspectionClient);
//            clients.put(new Pair<>(party,table),clientsToNotify);
//        }
    }

    /*public void notifyVoteToClients(Vote vote){
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
    }*/
}
