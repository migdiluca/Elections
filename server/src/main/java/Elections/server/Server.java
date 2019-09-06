package Elections.server;

import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.InspectionService;
import Elections.VotingService;
import Elections.server.ServiceImpl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("Elections Server Starting ...");

        ElectionPOJO electionState = new ElectionPOJO();

        AdministrationService as = new AdministrationServiceImpl(electionState);
        VotingService vs = new VotingServiceImpl(electionState);
        InspectionService is = new InspectionServiceImpl(electionState);
        ConsultingService cs = new ConsultingServiceImpl(electionState);

        final Remote remoteAS = UnicastRemoteObject.exportObject(as, 900);
        final Remote remoteVS = UnicastRemoteObject.exportObject(vs, 901);
        final Remote remoteIS = UnicastRemoteObject.exportObject(is, 902);
        final Remote remoteCS = UnicastRemoteObject.exportObject(cs, 903);

        final Registry registry = LocateRegistry.getRegistry();

        registry.rebind("administration_service", remoteAS);
        registry.rebind("voting_service", remoteVS);
        registry.rebind("inspection_service", remoteIS);
        registry.rebind("consulting_service", remoteCS);

    }
}
