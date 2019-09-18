package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.FiscalCallBack;
import Elections.FiscalService;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import Elections.server.Server;
import Elections.Models.Pair;;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class FiscalServiceImpl extends UnicastRemoteObject implements FiscalService {

    private Election electionState;
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private final Object addClientMutex = "Add client mutex" ;

    public FiscalServiceImpl(Election electionState) throws RemoteException {
        this.electionState = electionState;
    }

    @Override
    public void addInspector(FiscalCallBack fiscalCallBack, PoliticalParty party, int desk) throws RemoteException, ElectionStateException {
        if (!electionState.getElectionState().equals(ElectionState.NOT_STARTED)) {
            try {
                fiscalCallBack.submitError(electionState.getElectionState());
            } catch (RemoteException e) {
                System.out.println("Cannot add inspector when elections are already started or ended");
            }
            return;
        }

        Pair<PoliticalParty, Integer> votePair = new Pair<>(party, desk);
        synchronized (addClientMutex) {
            electionState.getFiscalClients().computeIfPresent(votePair, (key, clientsList) -> {
                clientsList.add(fiscalCallBack);
                return clientsList;
            });
            electionState.getFiscalClients().computeIfAbsent(votePair, clientsList -> new ArrayList<>()).add(fiscalCallBack);

        }

        logger.info("A fiscal has been registered in desk " + desk + " for party " + party.name());

    }

}
