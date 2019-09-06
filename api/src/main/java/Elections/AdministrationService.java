package Elections;

import Elections.Models.ElectionState;

public interface AdministrationService {


    /**
     * If already finished @throws AlreadyFinishedElectionException (o un nombre mas piolin)
     */
    void openElections();

    ElectionState getElectionState();

    /**
     * If elections did not start @throws ElectionsNotStartedException (o un nombre mas piolin)
     */
    void finishElections();

}
