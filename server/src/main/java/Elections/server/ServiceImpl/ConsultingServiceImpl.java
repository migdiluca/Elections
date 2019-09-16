package Elections.server.ServiceImpl;

import Elections.ConsultingService;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConsultingServiceImpl extends UnicastRemoteObject implements ConsultingService {

    private Election electionState;
    private ExecutorService exService;

    public ConsultingServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
        exService = Executors.newFixedThreadPool(12);
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultNational() throws RemoteException, ElectionStateException {
        try {
            Future<List<Pair<BigDecimal, PoliticalParty>>> future = exService.submit(() -> {
                List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
                return p != null ? p : electionState.getNationalFinalResults();
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getMessage());
        }
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultProvince(Province province) throws RemoteException, ElectionStateException {
        try {
            Future<List<Pair<BigDecimal, PoliticalParty>>> future = exService.submit(() -> {
                List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
                return p != null ? p : electionState.getProvinceFinalResults().get(province);
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getMessage());
        }
    }

    @Override
    public List<Pair<BigDecimal, PoliticalParty>> checkResultDesk(int desk) throws RemoteException, ElectionStateException {
        try {
            Future<List<Pair<BigDecimal, PoliticalParty>>> future = exService.submit(() -> {
                List<Pair<BigDecimal, PoliticalParty>> p = notCompletedResults();
                return p != null ? p : electionState.getDeskFinalResults().get(desk);
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getMessage());
        }

    }


    private List<Pair<BigDecimal, PoliticalParty>> notCompletedResults() throws RemoteException, ElectionStateException {
        try {
            Future<List<Pair<BigDecimal, PoliticalParty>>> future = exService.submit(() -> {
                if (electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
                    throw new ElectionsNotStartedException();
                } else if (electionState.getElectionState().equals(ElectionState.RUNNING)) {
                    List<Pair<BigDecimal, PoliticalParty>> retList = new ArrayList<>();
                    for (int i = 0; i < PoliticalParty.values().length; i++) {
                        PoliticalParty p = PoliticalParty.values()[i];
                        retList.add(new Pair<>(new BigDecimal(
                                100 * electionState.getPartialVotes()[i] / (double) electionState.getAmountOfVotes()).setScale(2, BigDecimal.ROUND_DOWN), p));
                    }
                    retList.sort(Comparator.comparing(Pair::getKey));
                    return retList;
                } else {
                    return null;
                }
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ElectionStateException(e.getMessage());
        }
    }
}
