package Elections;


import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.Dimension;
import Elections.Models.PoliticalParty;
import javafx.util.Pair;
import Elections.Models.Province;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ConsultingService extends Remote {

    String SERVICE_NAME = "consulting_service";

    /**
     * If elections did not start
     * @throws ElectionsNotStartedException (o un nombre mas piolin)
     * If elections are running, answers must be FPTP else respect each voting.
     */
    List<Pair<PoliticalParty, BigDecimal>> checkResultNational() throws RemoteException, ElectionStateException;

    List<Pair<PoliticalParty, BigDecimal>> checkResultProvince(Province province) throws RemoteException, ElectionStateException;

    List<Pair<PoliticalParty, BigDecimal>> checkResultDesk(int desk) throws RemoteException, ElectionStateException;
}
