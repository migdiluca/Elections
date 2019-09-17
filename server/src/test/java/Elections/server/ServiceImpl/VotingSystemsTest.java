package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static Elections.Models.PoliticalParty.GORILLA;
import static Elections.Models.PoliticalParty.BUFFALO;
import static Elections.Models.PoliticalParty.*;
import static Elections.Models.Province.*;
import static org.junit.Assert.*;

public class VotingSystemsTest {

    private VotingSystems votingSystems;

    @Before
    public void setUp() {
        List<Vote> votes = new LinkedList<>();
        PoliticalParty[] parties1 = {BUFFALO, GORILLA}; //#3
        PoliticalParty[] parties2 = {BUFFALO, GORILLA, LYNX}; //#1
        PoliticalParty[] parties3 = {BUFFALO, LYNX}; //#2
        PoliticalParty[] parties4 = {MONKEY, LYNX}; //#2
        PoliticalParty[] parties5 = {OWL, LYNX}; //#1
        PoliticalParty[] parties6 = {MONKEY, OWL}; //#1
        Vote v1 = new Vote(1, new ArrayList<>(Arrays.asList(parties1)), JUNGLE);
        Vote v2 = new Vote(1, new ArrayList<>(Arrays.asList(parties6)), JUNGLE);
        Vote v3 = new Vote(1, new ArrayList<>(Arrays.asList(parties3)), JUNGLE);
        Vote v4 = new Vote(2, new ArrayList<>(Arrays.asList(parties4)), JUNGLE);
        Vote v5 = new Vote(2, new ArrayList<>(Arrays.asList(parties5)), JUNGLE);
        Vote v6 = new Vote(2, new ArrayList<>(Arrays.asList(parties3)), JUNGLE);
        Vote v7 = new Vote(3, new ArrayList<>(Arrays.asList(parties1)), SAVANNAH);
        Vote v8 = new Vote(3, new ArrayList<>(Arrays.asList(parties2)), SAVANNAH);
        Vote v9 = new Vote(4, new ArrayList<>(Arrays.asList(parties4)), SAVANNAH);
        Vote v10 = new Vote(4, new ArrayList<>(Arrays.asList(parties1)), SAVANNAH);
        votes.add(v1);
        votes.add(v2);
        votes.add(v3);
        votes.add(v4);
        votes.add(v5);
        votes.add(v6);
        votes.add(v7);
        votes.add(v8);
        votes.add(v9);
        votes.add(v10);
        votingSystems = new VotingSystems(votes);
    }

    @Test
    public void alternativeVoteNationalLevel() {
        List<Pair<BigDecimal, PoliticalParty>> result = votingSystems.alternativeVoteNationalLevel();

        /*
         * The expected results to this system were obtained using this page: http://condorcet.ericgorr.net
         */

        assertEquals(result.get(0).getValue(), BUFFALO);
        assertEquals(result.get(0).getKey(), new BigDecimal(60.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

        assertEquals(result.get(1).getValue(), MONKEY);
        assertEquals(result.get(1).getKey(), new BigDecimal(30.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

        assertEquals(result.get(2).getValue(), OWL);
        assertEquals(result.get(2).getKey(), new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));
    }

    @Test
    public void calculateDeskResults() {
        Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> results = votingSystems.calculateDeskResults();

        List<Pair<BigDecimal, PoliticalParty>> desk1 = new LinkedList<>();
        List<Pair<BigDecimal, PoliticalParty>> desk2 = new LinkedList<>();
        List<Pair<BigDecimal, PoliticalParty>> desk3 = new LinkedList<>();
        List<Pair<BigDecimal, PoliticalParty>> desk4 = new LinkedList<>();

        /*
         * This results were calculated prior to the test
         */

        desk1.add(new Pair<>(new BigDecimal(33.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), MONKEY));desk1.add(new Pair<>(new BigDecimal(66.66).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        desk2.add(new Pair<>(new BigDecimal(33.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), OWL));desk2.add(new Pair<>(new BigDecimal(33.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), MONKEY));desk2.add(new Pair<>(new BigDecimal(33.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        desk3.add(new Pair<>(new BigDecimal(100.00).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        desk4.add(new Pair<>(new BigDecimal(50.00).setScale(2, BigDecimal.ROUND_HALF_DOWN), MONKEY));desk4.add(new Pair<>(new BigDecimal(50.00).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> expectedResults = new HashMap<>();
        expectedResults.put(1, desk1);
        expectedResults.put(2, desk2);
        expectedResults.put(3, desk3);
        expectedResults.put(4, desk4);

        assertTrue(results.entrySet().stream().allMatch((entry) ->
                // equal on both sides
                expectedResults.get(entry.getKey()).containsAll(entry.getValue()) && entry.getValue().containsAll(expectedResults.get(entry.getKey()))
        ));
    }

}