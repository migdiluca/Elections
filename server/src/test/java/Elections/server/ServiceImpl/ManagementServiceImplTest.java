package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class ManagementServiceImplTest {

    private ManagementServiceImpl administrationService;
    private ManagementServiceImpl administrationServiceRunning;
    private ManagementServiceImpl administrationServiceFinished;

    private static ExecutorService service;

    @Before
    public void init() {
        try {
            Election election = new Election();
            administrationService = new ManagementServiceImpl(election);
            Election election1 = new Election();
            election1.setElectionState(ElectionState.FINISHED);
            administrationServiceFinished = new ManagementServiceImpl(election1);
            Election election2 = new Election();
            election2.setElectionState(ElectionState.RUNNING);
            administrationServiceRunning = new ManagementServiceImpl(election2);

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
        Runnable task = (() -> {
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
        try {
            service.submit(task).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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
        Runnable task = (() -> {
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
        try {
            service.submit(task).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
