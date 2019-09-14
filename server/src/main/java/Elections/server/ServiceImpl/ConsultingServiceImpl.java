package Elections.server.ServiceImpl;


import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ConsultingServiceImpl extends UnicastRemoteObject implements ConsultingService {

    private Election electionState;

    public ConsultingServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultNational() throws RemoteException, ElectionStateException {
        List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
        return p != null ? p : electionState.getNationalFinalResults();
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultProvince(Province province) throws RemoteException, ElectionStateException {
        List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
        return p != null ? p : electionState.getProvinceFinalResults().get(province);
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultDesk(int desk) throws RemoteException, ElectionStateException {
        List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
        return p != null ? p : electionState.getDeskFinalResults().get(desk);
    }


    private List<Pair<BigDecimal, PoliticalParty>> notCompletedResults() throws RemoteException, ElectionStateException {
        if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
            throw new ElectionsNotStartedException();
        } else if (electionState.getElectionState().equals(ElectionState.RUNNING)) {
            List<Pair<BigDecimal, PoliticalParty>> retList = new ArrayList<>();
            for (int i = 0; i < PoliticalParty.values().length; i++) {
                PoliticalParty p = PoliticalParty.values()[i];
                retList.add(new Pair<>(new BigDecimal(
                        100 * electionState.getPartialVotes()[i] / electionState.getAmountOfVotes()).setScale(2, BigDecimal.ROUND_DOWN), p));
            }
            return retList;
        } else return null;
    }
}
