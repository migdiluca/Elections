package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AdministrationServiceImplTest {

    private AdministrationServiceImpl administrationService;
    private AdministrationServiceImpl administrationServiceRunning;
    private AdministrationServiceImpl administrationServiceFinished;

    private static ExecutorService service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        try {
            Election election = new Election();
            administrationService = new AdministrationServiceImpl(election);
            Election election1 = new Election();
            election1.setElectionState(ElectionState.FINISHED);
            administrationServiceFinished = new AdministrationServiceImpl(election1);
            Election election2 = new Election();
            election2.setElectionState(ElectionState.RUNNING);
            administrationServiceRunning = new AdministrationServiceImpl(election2);

            service = Executors.newFixedThreadPool(2);
        } catch (RemoteException e) {
            System.out.println("remote exception error");
        }
    }

    @After
    public final void after() {
        service.shutdownNow();
    }

    @Test
    public void openElectionsTest() {
        service.execute(() -> {
            try {
                administrationService.openElections();
            } catch (ElectionStateException | RemoteException exception) {
                fail();
            }
            try {
                administrationServiceFinished.openElections();
                fail();
            } catch (ElectionStateException | RemoteException exception) {
                assertEquals(administrationService.getElection().getElectionState(), ElectionState.RUNNING);
            }
        });
    }

    @Test
    public void getElectionsTest() throws RemoteException {
        try{
        assertEquals(administrationService.getElectionState(), ElectionState.NOT_STARTED);
        assertEquals(administrationServiceFinished.getElectionState(), ElectionState.FINISHED);
        assertEquals(administrationServiceRunning.getElectionState(), ElectionState.RUNNING);
        }catch(ServiceException exception){
            fail();
        }
    }

    @Test
    public void closeElectionsTest() {
        service.execute(() -> {
            try{
                administrationService.finishElections();
                fail();
            } catch (ElectionStateException | RemoteException ignore) {
            }
            try{
                administrationServiceFinished.finishElections();
                fail();
            } catch (ElectionStateException | RemoteException ignore) {
            }
            try{
                administrationServiceRunning.finishElections();
                assertEquals(administrationServiceRunning.getElection().getElectionState(),ElectionState.FINISHED);
            } catch (ElectionStateException | RemoteException ignore) {
                fail();
            }

        });
    }
}
