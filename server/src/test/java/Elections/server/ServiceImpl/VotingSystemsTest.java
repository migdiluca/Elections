package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static Elections.Models.PoliticalParty.GORILLA;
import static Elections.Models.PoliticalParty.BUFFALO;
import static Elections.Models.PoliticalParty.*;
import static Elections.Models.Province.*;
import static org.junit.Assert.*;

public class VotingSystemsTest {

    List<Vote> votes;
    VotingSystems votingSystems;

    @Before
    public void setUp() throws Exception {
        votes = new LinkedList<>();
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
        Vote[] votesArr = {v1, v2, v3, v4, v5, v6, v7, v8, v9, v10};
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
    public void alternativeVoteNationalLevel() throws Exception {
        Pair<BigDecimal, PoliticalParty> result = votingSystems.alternativeVoteNationalLevel();

        /*
         * Los resultados esperados para este sistema fueron obtenidos con el siguiente motor: http://condorcet.ericgorr.net
         */

        assertEquals(result.getValue(), BUFFALO);
        // ni idea por que lo agarra como un 0.58999999 al hacer round down se me va a 0.58
        assertEquals(result.getKey(), new BigDecimal(0.59).setScale(2, BigDecimal.ROUND_HALF_DOWN));
    }

    @Test
    public void calculateDeskResults() throws Exception {
        Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> results = votingSystems.calculateDeskResults();

        List<Pair<BigDecimal, PoliticalParty>> table1 = new LinkedList<>();
        List<Pair<BigDecimal, PoliticalParty>> table2 = new LinkedList<>();
        List<Pair<BigDecimal, PoliticalParty>> table3 = new LinkedList<>();
        List<Pair<BigDecimal, PoliticalParty>> table4 = new LinkedList<>();
        /*
         * Estos resultados fueron calculados a mano
         */
        table1.add(new Pair<>(new BigDecimal(0.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), MONKEY));table1.add(new Pair<>(new BigDecimal(0.66).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        table2.add(new Pair<>(new BigDecimal(0.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), OWL));table2.add(new Pair<>(new BigDecimal(0.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), MONKEY));table2.add(new Pair<>(new BigDecimal(0.33).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        table3.add(new Pair<>(new BigDecimal(1.00).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        table4.add(new Pair<>(new BigDecimal(0.50).setScale(2, BigDecimal.ROUND_HALF_DOWN), MONKEY));table4.add(new Pair<>(new BigDecimal(0.50).setScale(2, BigDecimal.ROUND_HALF_DOWN), BUFFALO));
        Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> expectedResults = new HashMap<>();
        expectedResults.put(1, table1);
        expectedResults.put(2, table2);
        expectedResults.put(3, table3);
        expectedResults.put(4, table4);

        assertTrue(results.entrySet().stream().allMatch((entry) ->
                // iguales por ambos lados
                expectedResults.get(entry.getKey()).containsAll(entry.getValue()) && entry.getValue().containsAll(expectedResults.get(entry.getKey()))
        ));
    }

}