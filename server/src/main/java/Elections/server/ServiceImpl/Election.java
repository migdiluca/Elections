package Elections.server.ServiceImpl;

import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.*;

public class Election {

    private ElectionState electionState;
    private List<Vote> votingList;
    private long[] partialVotes;

    private List<Pair<BigDecimal, PoliticalParty>> nationalFinalResults;
    private Map<Province, List<Pair<BigDecimal, PoliticalParty>>> provinceFinalResults;
    private Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> deskFinalResults;


    public Election() {
        electionState = ElectionState.NOT_STARTED;
        votingList = Collections.synchronizedList(new ArrayList<>());
        partialVotes = new long[13];

        nationalFinalResults = new ArrayList<>();
        provinceFinalResults = new HashMap<>();
        for (Province p : Province.values()) {
            provinceFinalResults.put(p, new ArrayList<>());
        }
        deskFinalResults = new HashMap<>();
    }

    public void addToVoteList(Vote vote) {
        votingList.add(vote);
        partialVotes[vote.getPreferredParties().get(0).ordinal()]++;
    }

    public int getAmountOfVotes() {
        return votingList.size();
    }

    public ElectionState getElectionState() {
        return electionState;
    }

    public void setElectionState(ElectionState electionState) {
        this.electionState = electionState;
    }

    public List<Vote> getVotingList() {
        return votingList;
    }

    public void setVotingList(List<Vote> votingList) {
        this.votingList = votingList;
    }

    public long[] getPartialVotes() {
        return partialVotes;
    }

    public void setPartialVotes(long[] partialVotes) {
        this.partialVotes = partialVotes;
    }

    public List<Pair<BigDecimal, PoliticalParty>> getNationalFinalResults() {
        return nationalFinalResults;
    }

    public void setNationalFinalResults(List<Pair<BigDecimal, PoliticalParty>> nationalFinalResults) {
        this.nationalFinalResults = nationalFinalResults;
    }

    public Map<Province, List<Pair<BigDecimal, PoliticalParty>>> getProvinceFinalResults() {
        return provinceFinalResults;
    }

    public void setProvinceFinalResults(Map<Province, List<Pair<BigDecimal, PoliticalParty>>> provinceFinalResults) {
        this.provinceFinalResults = provinceFinalResults;
    }

    public Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> getDeskFinalResults() {
        return deskFinalResults;
    }

    public void setDeskFinalResults(Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> deskFinalResults) {
        this.deskFinalResults = deskFinalResults;
    }
}
