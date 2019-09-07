package Elections.server.ServiceImpl;


import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.Dimension;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConsultingServiceImpl extends UnicastRemoteObject implements ConsultingService {

    private ElectionPOJO electionState;

    public ConsultingServiceImpl(ElectionPOJO electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public List<Pair<PoliticalParty, BigDecimal>> checkResultNational()
            throws RemoteException, ElectionStateException {
        List<Pair<PoliticalParty, BigDecimal>> p = notCompletedResults();
        return p != null?p:electionState.getNationalFinalResults();
    }

    @Override
    public List<Pair<PoliticalParty, BigDecimal>> checkResultProvince(Province province) throws RemoteException, ElectionStateException {
        List<Pair<PoliticalParty, BigDecimal>> p = notCompletedResults();
        return p != null?p:electionState.getProvinceFinalResults().get(province);
    }

    @Override
    public List<Pair<PoliticalParty, BigDecimal>> checkResultDesk(int desk) throws RemoteException, ElectionStateException {
        List<Pair<PoliticalParty, BigDecimal>> p = notCompletedResults();
        return p != null?p:electionState.getDeskFinalResults().get(desk);
    }


    private List<Pair<PoliticalParty, BigDecimal>> notCompletedResults() throws RemoteException, ElectionStateException {
        if(electionState.getElectionState().equals(ElectionState.NOT_STARTED)){
            throw new ElectionsNotStartedException();
        }
        else if(electionState.getElectionState().equals(ElectionState.RUNNING)){
            List<Pair<PoliticalParty, BigDecimal>> retList = new ArrayList<>();
            for (int i = 0; i < PoliticalParty.values().length; i++) {
                PoliticalParty p = PoliticalParty.values()[i];
                retList.add(new Pair<>(p, new BigDecimal(
                        100* electionState.getPartialVotes()[i] / electionState.getAmountOfVotes())));
            }
            return retList;
        }
        else return null;
    }
}
