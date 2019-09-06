package Elections;


public interface VotingService {
    /**
     * If already open
     * e@throws ElectionsAlreadyStartedException or
     * e@throws AlreadyFinishedElectionException (o un nombre mas piolin)
     */
    void vote(String vote, int desk);
}
