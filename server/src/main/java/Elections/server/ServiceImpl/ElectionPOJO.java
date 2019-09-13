package Elections.server.ServiceImpl;

import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class ElectionPOJO {

    private ElectionState electionState;
    private List<Vote> votingList;
    private LongAdder[] partialVotes;

    private List<Pair<BigDecimal, PoliticalParty>> nationalFinalResults;
    private Map<Province, List<Pair<BigDecimal, PoliticalParty>>> provinceFinalResults;
    private Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> deskFinalResults;

    private final Object helperA = 0;
    private final Object helperB = 0;
    public ElectionPOJO() {
        electionState = ElectionState.NOT_STARTED;
        votingList = Collections.synchronizedList(new ArrayList<>());
        partialVotes = new LongAdder[13];
        for (int i = 0; i < partialVotes.length; i++) {
            partialVotes[i] = new LongAdder();
        }

        nationalFinalResults = new ArrayList<>();
        provinceFinalResults = new HashMap<>();
        for (Province p : Province.values()) {
            provinceFinalResults.put(p, new ArrayList<>());
        }
        deskFinalResults = new HashMap<>();
    }

    void addToVoteList(Vote vote) {
        synchronized (helperA) {
            votingList.add(vote);
        }
        synchronized (helperB) {
            partialVotes[vote.getPreferredParties().get(0).ordinal()].increment();
        }
    }

    int getAmountOfVotes() {
        return votingList.size();
    }

    ElectionState getElectionState() {
        return electionState;
    }

    void setElectionState(ElectionState electionState) {
        this.electionState = electionState;
    }

    Long[] getPartialVotes() {
        return Arrays.stream(partialVotes).map(LongAdder::longValue).toArray(Long[]::new);
    }

    List<Pair<BigDecimal, PoliticalParty>> getNationalFinalResults() {
        return nationalFinalResults;
    }

    Map<Province, List<Pair<BigDecimal, PoliticalParty>>> getProvinceFinalResults() {
        return provinceFinalResults;
    }

    Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> getDeskFinalResults() {
        return deskFinalResults;
    }

}
