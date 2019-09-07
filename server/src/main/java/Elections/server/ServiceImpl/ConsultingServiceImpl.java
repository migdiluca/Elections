package Elections.server.ServiceImpl;


import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.Dimension;
import Elections.Models.PoliticalParty;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

public class ConsultingServiceImpl extends UnicastRemoteObject implements ConsultingService {

    public ConsultingServiceImpl(int port) throws RemoteException {
        super(port);
    }

    public ConsultingServiceImpl() throws RemoteException{

    }

    @Override
    public Map<PoliticalParty, Integer> checkResult(Dimension dimension)
            throws RemoteException, ElectionStateException {
        return null;
    }
}
