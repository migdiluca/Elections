package Elections.server.ServiceImpl;


import Elections.Exceptions.ElectionStateException;
import Elections.FiscalCallBack;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class FiscalServiceImplTest {

    private FiscalServiceImpl fiscalService;
    private Election election;

    @Before
    public void init() {
        try {

            election = new Election();
            election.setElectionState(ElectionState.RUNNING);
            fiscalService = new FiscalServiceImpl(election);

        } catch (RemoteException e) {
            System.out.println("remote exception error");
        }
    }

    @Test
    public void addInspectorSingleTest() {

        FiscalCallBack fiscalCallBack = new FiscalCallBack() {
            @Override
            public void notifyVote() {

            }

            @Override
            public void endClient() {

            }

            @Override
            public void submitError(ElectionState electionState) {

            }
        };
        try {
            fiscalService.addInspector(fiscalCallBack, PoliticalParty.BUFFALO, 1);
        } catch (RemoteException | ElectionStateException e) {
            e.printStackTrace();
        }

        Pair<PoliticalParty, Integer> pair1 = new Pair<>(PoliticalParty.BUFFALO, 1);
        Pair<PoliticalParty, Integer> pair2 = new Pair<>(PoliticalParty.BUFFALO, 2);

        if (election.getFiscalClients().containsKey(pair1)) {
            assertEquals(fiscalCallBack, election.getFiscalClients().get(pair1).get(0));
        }
        if (election.getFiscalClients().containsKey(pair2)) {
            fail();
        }
    }

    @Test
    public void addInspectorMultipleTest() {
        FiscalCallBack fiscalCallBack = new FiscalCallBack() {
            @Override
            public void notifyVote() {

            }

            @Override
            public void endClient() {

            }

            @Override
            public void submitError(ElectionState electionState) {

            }
        };
        try {
            fiscalService.addInspector(fiscalCallBack, PoliticalParty.BUFFALO, 1);
        } catch (RemoteException | ElectionStateException e) {
            e.printStackTrace();
        }

        for (PoliticalParty pp : PoliticalParty.values()) {
            for (int i = 0; i < 20; i++) {
                Pair<PoliticalParty, Integer> pair = new Pair<>(pp, i);
                if (election.getFiscalClients().containsKey(pair)) {
                    assertEquals(fiscalCallBack, election.getFiscalClients().get(pair).get(0));
                }
            }
        }
    }

}
