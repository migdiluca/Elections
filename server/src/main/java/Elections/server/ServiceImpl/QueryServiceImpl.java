package Elections.server.ServiceImpl;

import Elections.QueryService;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Pair;;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class QueryServiceImpl extends UnicastRemoteObject implements QueryService {

    private Election electionState;

    public QueryServiceImpl(Election electionState) throws RemoteException {
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
        if (p != null) {
            return p;
        } else {
            return electionState.getProvinceFinalResults().get(province) == null ?
                    new ArrayList<>() :
                    electionState.getProvinceFinalResults().get(province);
        }
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultDesk(int desk) throws RemoteException, ElectionStateException {
        List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
        if (p != null) {
            return p;
        } else {
            return electionState.getDeskFinalResults().get(desk) == null ?
                    new ArrayList<>() :
                    electionState.getDeskFinalResults().get(desk);
        }
    }


    private List<Pair<BigDecimal, PoliticalParty>> notCompletedResults() throws RemoteException, ElectionStateException {
        if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
            throw new ElectionsNotStartedException();
        } else if (electionState.getElectionState().equals(ElectionState.RUNNING) ||
                electionState.getElectionState().equals(ElectionState.CALCULATING)) {
            List<Pair<BigDecimal, PoliticalParty>> retList = new ArrayList<>();
            if (electionState.getAmountOfVotes() > 0) {
                for (int i = 0; i < PoliticalParty.values().length; i++) {
                    PoliticalParty p = PoliticalParty.values()[i];
                    retList.add(new Pair<>(new BigDecimal(
                            100 * electionState.getPartialVotes()[i] / (double) electionState.getAmountOfVotes()).setScale(2, BigDecimal.ROUND_DOWN), p));
                }
                retList.sort(VotingSystems.cmp);
            }
            return retList;
        } else {
            return null;
        }
    }
}
