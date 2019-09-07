package Elections.server.ServiceImpl;

import Elections.AdministrationService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import Elections.VotingService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {

    private ElectionPOJO electionState;

    public VotingServiceImpl(ElectionPOJO electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void vote(int table, List<PoliticalParty> preferredParties, Province province) throws ElectionStateException {
        Vote vote = new Vote(table,preferredParties,province);
    }
}
