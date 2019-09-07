package Elections.server.ServiceImpl;

import Elections.Exceptions.ElectionStateException;
import Elections.Models.ElectionState;
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
    /**
     * return true if votes where processed
     * return false if vote could not be processed. It suggest that the request must be
     * retried
     * */
    public boolean vote(List<Vote> votes) throws ElectionStateException {
        if (electionState.getElectionState() != ElectionState.RUNNING) {
            throw new ElectionStateException("You can not vote if elections are not running");
        }
        // arrancamos un nuevo thread que procese la entrada
        // tenemos que hacer un thread pool, estático o dinámico, que se encargue de procesar
        // los votos
        return true
    }
}
