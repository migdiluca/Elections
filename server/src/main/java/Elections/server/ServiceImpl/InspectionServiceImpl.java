package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.InspectionClient;
import Elections.InspectionService;
import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InspectionServiceImpl extends UnicastRemoteObject implements InspectionService {

    private ElectionPOJO electionState;

    private Map<Pair<PoliticalParty, Integer>,List<InspectionClient>> clients;

    public InspectionServiceImpl(ElectionPOJO electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void addInspector(InspectionClient inspectionClient, PoliticalParty party, int table) throws RemoteException, ElectionStateException {
        Pair<PoliticalParty, Integer> votePair = new Pair<>(party,table);
        if(clients.containsKey(votePair)){
            clients.get(votePair).add(inspectionClient);
        } else {
            List<InspectionClient> clientsToNotify = new ArrayList<>();
            clientsToNotify.add(inspectionClient);
            clients.put(new Pair<>(party,table),clientsToNotify);
        }
    }

    public void notifyVote(Vote vote){
        for(PoliticalParty politicalParty : vote.getPreferredParties()){
            List<InspectionClient> clientsToNotify = clients.get(new Pair<PoliticalParty,Integer>(politicalParty, vote.getTable()));
            for(InspectionClient inspectionClient : clientsToNotify){
                try{
                    inspectionClient.notifyVote();
                } catch (RemoteException e){
                    System.out.println("Remote exception on server side for InspectionService");
                }
            }
        }
    }
}
