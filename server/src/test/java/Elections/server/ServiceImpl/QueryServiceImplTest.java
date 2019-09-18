package Elections.server.ServiceImpl;


import Elections.Exceptions.ElectionStateException;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import Elections.QueryService;
import Elections.Models.Pair;;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.*;


import static Elections.Models.PoliticalParty.*;
import static Elections.Models.Province.JUNGLE;
import static org.junit.Assert.*;

public class QueryServiceImplTest {

    private QueryService consultingServiceNotStarted;
    private QueryService consultingServiceRunning;
    private QueryService consultingServiceFinished;

    private List<Pair<BigDecimal, PoliticalParty>> nationalList;
    private Map<Province, List<Pair<BigDecimal, PoliticalParty>>> mapProvince;
    private Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> mapDesk;

    @Before
    public void init() {
        try {
            Election electionRunning = new Election();
            Election electionNotStarted = new Election();
            Election electionFinished = new Election();

            electionFinished.setElectionState(ElectionState.FINISHED);
            electionRunning.setElectionState(ElectionState.RUNNING);

            List<Vote> votes = new ArrayList<>();


            //Hard put a value into the final province final results
            mapProvince = new HashMap<>();
            for (Province p : Province.values()) {
                List<Pair<BigDecimal, PoliticalParty>> l = new ArrayList<>();
                l.add(new Pair<>(new BigDecimal(100.0), PoliticalParty.BUFFALO));
                mapProvince.put(p, l);
            }
            electionFinished.setProvinceFinalResults(mapProvince);

            //Hard put desk results
            mapDesk = new HashMap<>();
            for (int i = 0; i < 20; i++) {
                List<Pair<BigDecimal, PoliticalParty>> l = new ArrayList<>();
                l.add(new Pair<>(new BigDecimal(100.0), PoliticalParty.BUFFALO));
                mapDesk.put(i, l);
            }
            electionFinished.setDeskFinalResults(mapDesk);


            //Hard put National final results
            nationalList = new LinkedList<>();
            for (PoliticalParty pp : PoliticalParty.values()) {
                nationalList.add(new Pair<>(new BigDecimal(values().length / 100.0), pp));
            }
            electionFinished.setNationalFinalResults(nationalList);

            //for the mid results
            PoliticalParty[] parties1 = {BUFFALO, GORILLA}; //#3
            PoliticalParty[] parties2 = {BUFFALO, GORILLA, LYNX}; //#1
            PoliticalParty[] parties3 = {BUFFALO, LYNX}; //#2
            PoliticalParty[] parties4 = {MONKEY, LYNX}; //#2
            PoliticalParty[] parties5 = {OWL, LYNX}; //#1
            PoliticalParty[] parties6 = {MONKEY, OWL}; //#1
            Vote v1 = new Vote(1, new ArrayList<>(Arrays.asList(parties1)), JUNGLE);
            Vote v2 = new Vote(1, new ArrayList<>(Arrays.asList(parties6)), JUNGLE);
            Vote v3 = new Vote(1, new ArrayList<>(Arrays.asList(parties3)), JUNGLE);
            Vote v4 = new Vote(1, new ArrayList<>(Arrays.asList(parties4)), JUNGLE);
            Vote v5 = new Vote(1, new ArrayList<>(Arrays.asList(parties5)), JUNGLE);
            Vote v6 = new Vote(1, new ArrayList<>(Arrays.asList(parties3)), JUNGLE);
            Vote v7 = new Vote(1, new ArrayList<>(Arrays.asList(parties1)), JUNGLE);
            Vote v8 = new Vote(1, new ArrayList<>(Arrays.asList(parties2)), JUNGLE);
            Vote v9 = new Vote(1, new ArrayList<>(Arrays.asList(parties4)), JUNGLE);
            Vote v10 = new Vote(1, new ArrayList<>(Arrays.asList(parties1)), JUNGLE);
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

            for (Vote v : votes) {
                electionFinished.addToVoteList(v);
                electionRunning.addToVoteList(v);
            }

            consultingServiceNotStarted = new QueryServiceImpl(electionNotStarted);
            consultingServiceRunning = new QueryServiceImpl(electionRunning);
            consultingServiceFinished = new QueryServiceImpl(electionFinished);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkResultNationalTest() {
        try {
            consultingServiceNotStarted.checkResultNational();
            fail();
        } catch (ElectionStateException | RemoteException ignore) {
        }
        try {
            List<Pair<BigDecimal, PoliticalParty>> result = consultingServiceRunning.checkResultNational();
            assertEquals(result.get(0).getValue(), BUFFALO);
            assertEquals(result.get(0).getKey(), new BigDecimal(60.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            assertEquals(result.get(1).getValue(), MONKEY);
            assertEquals(result.get(1).getKey(), new BigDecimal(30.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            assertEquals(result.get(2).getValue(), OWL);
            assertEquals(result.get(2).getKey(), new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));


            assertTrue(consultingServiceFinished.checkResultNational().containsAll(nationalList));

        } catch (ElectionStateException | RemoteException e) {
            fail();
        }
    }

    @Test
    public void checkResultProvinceTest() {
        try {
            consultingServiceNotStarted.checkResultProvince(JUNGLE);
            fail();
        } catch (ElectionStateException | RemoteException ignore) {
        }

        try {
            List<Pair<BigDecimal, PoliticalParty>> result = consultingServiceRunning.checkResultProvince(JUNGLE);
            assertEquals(result.get(0).getValue(), BUFFALO);
            assertEquals(result.get(0).getKey(), new BigDecimal(60.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            assertEquals(result.get(1).getValue(), MONKEY);
            assertEquals(result.get(1).getKey(), new BigDecimal(30.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            assertEquals(result.get(2).getValue(), OWL);
            assertEquals(result.get(2).getKey(), new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));
        } catch (ElectionStateException | RemoteException ignore) {
        }


        mapProvince.forEach((k, v) -> {
            try {
                assertEquals(v, consultingServiceFinished.checkResultProvince(k));
            } catch (RemoteException | ElectionStateException e) {
                e.printStackTrace();
            }
        });

    }

    @Test
    public void checkResultDeskTest() {
        try {
            consultingServiceNotStarted.checkResultDesk(1);
            fail();
        } catch (ElectionStateException | RemoteException ignore) {
        }

        try {
            List<Pair<BigDecimal, PoliticalParty>> result = consultingServiceRunning.checkResultDesk(1);
            assertEquals(result.get(0).getValue(), BUFFALO);
            assertEquals(result.get(0).getKey(), new BigDecimal(60.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            assertEquals(result.get(1).getValue(), MONKEY);
            assertEquals(result.get(1).getKey(), new BigDecimal(30.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            assertEquals(result.get(2).getValue(), OWL);
            assertEquals(result.get(2).getKey(), new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_DOWN));
        } catch (ElectionStateException | RemoteException ignore) {
        }


        mapDesk.forEach((k, v) -> {
            try {
                assertEquals(v, consultingServiceFinished.checkResultDesk(k));
            } catch (RemoteException | ElectionStateException e) {
                e.printStackTrace();
            }
        });
    }


}
