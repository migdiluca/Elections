package Elections;


import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.Dimension;
import Elections.Models.PoliticalParty;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ConsultingService extends Remote {

    /**
     * If elections did not start
     * @throws ElectionsNotStartedException (o un nombre mas piolin)
     * If elections are running, answers must be FPTP else respect each voting.
     */
    Map<PoliticalParty, Integer> checkResult(Dimension dimension) throws RemoteException, ElectionStateException;


}
