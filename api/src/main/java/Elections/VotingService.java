package Elections;


import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsAlreadyStartedException;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VotingService extends Remote {
    /**
     * If already open
     * @throws ElectionsAlreadyStartedException or
     * @throws AlreadyFinishedElectionException can be thrown
     */
    void vote(int table, List<PoliticalParty> preferredParties, Province province) throws RemoteException, ElectionStateException;
}
