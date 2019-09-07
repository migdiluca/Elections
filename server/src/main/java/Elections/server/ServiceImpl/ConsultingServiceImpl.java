package Elections.server.ServiceImpl;


import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.Dimension;
import Elections.Models.PoliticalParty;
import javafx.util.Pair;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConsultingServiceImpl extends UnicastRemoteObject implements ConsultingService {

    private ElectionPOJO electionState;

    public ConsultingServiceImpl(ElectionPOJO electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public List<Pair<Double, PoliticalParty>> checkResult(Dimension dimension)
            throws RemoteException, ElectionStateException {
        // resultados de preuba
        List<Pair<Double, PoliticalParty>> results = new LinkedList<>();
        results.add(new Pair<Double, PoliticalParty>(50.4, PoliticalParty.BUFFALO));
        results.add(new Pair<Double, PoliticalParty>(54.9, PoliticalParty.JACKALOPE));
        results.add(new Pair<Double, PoliticalParty>(45.7, PoliticalParty.LEOPARD));
        return null;
    }
}
