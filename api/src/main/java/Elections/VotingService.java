package Elections;


import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsAlreadyStartedException;
import Elections.Models.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VotingService extends Remote {
    /**
     * If already open
     * @throws ElectionsAlreadyStartedException or
     * @throws AlreadyFinishedElectionException can be thrown
     */
    void vote(Vote vote) throws RemoteException, ElectionStateException;
}
