package Elections.server.ServiceImpl;


import Elections.Exceptions.ElectionStateException;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Elections.Models.PoliticalParty.*;
import static Elections.Models.Province.JUNGLE;
import static Elections.Models.Province.SAVANNAH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VotingServiceImplTest {

    private VotingServiceImpl votingServiceRunning;
    private VotingServiceImpl votingServiceNotStarted;
    private static ExecutorService service;
    private Election electionRunning;
    private Election electionNotStarted;
    private List<Vote> votes;


    @Before
    public void init(){
        try {

            electionRunning = new Election();
            electionRunning.setElectionState(ElectionState.RUNNING);
            electionNotStarted = new Election();
            votingServiceRunning = new VotingServiceImpl(electionRunning);
            votingServiceNotStarted = new VotingServiceImpl(electionNotStarted);
            service = Executors.newFixedThreadPool(200);
            votes = new ArrayList<>();


            //para el testeo
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

        } catch (RemoteException e) {
            System.out.println("remote exception error");
        }
    }

    @After
    public final void after() {
        service.shutdownNow();
    }

    @Test
    public void voteTest() throws RemoteException{
        try{
            votingServiceNotStarted.vote(votes);
            fail();
        }catch (ElectionStateException ignore) {}

        try{
            votingServiceRunning.vote(votes);
        } catch (ElectionStateException e) {
            fail();
        }

        assertEquals(votes,electionRunning.getVotingList());

    }
}
