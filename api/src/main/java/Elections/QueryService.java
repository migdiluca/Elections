package Elections;


import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.Models.PoliticalParty;
import Elections.Models.Pair;
import Elections.Models.Province;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface QueryService extends Remote {

    String SERVICE_NAME = "consulting_service" ;

    /**
     * gets national results
     * If elections are running, answers must be FPTP else respect each voting.
     *
     * @throws ElectionsNotStartedException if elections did not start
     */
    List<Pair<BigDecimal, PoliticalParty>> checkResultNational() throws RemoteException, ElectionStateException;

    /**
     * gets province results
     * If elections are running, answers must be FPTP else respect each voting.
     *
     * @param province the province enum corresponding to which province results are wanted
     * @throws ElectionsNotStartedException if elections did not start
     */
    List<Pair<BigDecimal, PoliticalParty>> checkResultProvince(Province province) throws RemoteException, ElectionStateException;

    /**
     * gets province results
     * If elections are running, answers must be FPTP else respect each voting.
     *
     * @param desk the desk number
     * @throws ElectionsNotStartedException if elections did not start
     */
    List<Pair<BigDecimal, PoliticalParty>> checkResultDesk(int desk) throws RemoteException, ElectionStateException;
}
