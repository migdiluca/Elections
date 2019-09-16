package Elections.server.ServiceImpl;


import Elections.Exceptions.ElectionStateException;
import Elections.InspectionClient;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import javafx.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class FiscalServiceImplTest {

    private FiscalServiceImpl fiscalService;
    private static ExecutorService service;
    private Election election;

    @Before
    public void init() {
        try {

            election = new Election();
            election.setElectionState(ElectionState.RUNNING);
            fiscalService = new FiscalServiceImpl(election);

            service = Executors.newFixedThreadPool(200);
        } catch (RemoteException e) {
            System.out.println("remote exception error");
        }
    }

    @After
    public final void after() {
        service.shutdownNow();
    }

    @Test
    public void addInspectorSingleTest() throws RemoteException, ElectionStateException {

        service.execute(() -> {
            InspectionClient inspectionClient = new InspectionClient() {
                @Override
                public void notifyVote() throws RemoteException {

                }

                @Override
                public void endClient() throws RemoteException {

                }

                @Override
                public void submitError(ElectionState electionState) throws RemoteException {

                }
            };
            try {
                fiscalService.addInspector(inspectionClient, PoliticalParty.BUFFALO, 1);
            } catch (RemoteException | ElectionStateException e) {
                e.printStackTrace();
            }

            Pair<PoliticalParty, Integer> pair1 = new Pair<>(PoliticalParty.BUFFALO, 1);
            Pair<PoliticalParty, Integer> pair2 = new Pair<>(PoliticalParty.BUFFALO, 2);

            if (election.getFiscalClients().containsKey(pair1)) {
                assertEquals(inspectionClient, election.getFiscalClients().get(pair1).get(0));
            }
            if (election.getFiscalClients().containsKey(pair2)) {
                fail();
            }
        });
    }

    @Test
    public void addInspectorMultipleTest() throws RemoteException, ElectionStateException {

        service.execute(() -> {
            InspectionClient inspectionClient = new InspectionClient() {
                @Override
                public void notifyVote() throws RemoteException {

                }

                @Override
                public void endClient() throws RemoteException {

                }

                @Override
                public void submitError(ElectionState electionState) throws RemoteException {

                }
            };
            try {
                fiscalService.addInspector(inspectionClient, PoliticalParty.BUFFALO, 1);
            } catch (RemoteException | ElectionStateException e) {
                e.printStackTrace();
            }

            for (PoliticalParty pp:PoliticalParty.values()) {
                for (int i = 0; i < 20; i++) {
                    Pair<PoliticalParty, Integer> pair = new Pair<>(pp, i);
                    if (election.getFiscalClients().containsKey(pair)) {
                        assertEquals(inspectionClient, election.getFiscalClients().get(pair).get(0));
                    }
                }
            }


        });
    }

}
