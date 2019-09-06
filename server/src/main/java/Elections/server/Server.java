package Elections.server;

import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.InspectionService;
import Elections.VotingService;
import Elections.server.ServiceImpl.AdministrationServiceImpl;
import Elections.server.ServiceImpl.ConsultingServiceImpl;
import Elections.server.ServiceImpl.InspectionServiceImpl;
import Elections.server.ServiceImpl.VotingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int port = 8090;

    public static void main(String[] args) throws RemoteException, NotBoundException {

        logger.info("Elections Server Starting ...");

        AdministrationService as = new AdministrationServiceImpl();
        VotingService vs = new VotingServiceImpl();
        InspectionService is = new InspectionServiceImpl();
        ConsultingService cs = new ConsultingServiceImpl();


        final Registry registry = LocateRegistry.createRegistry(port);

        registry.rebind("administration_service", as);
        registry.rebind("voting_service", vs);
        registry.rebind("inspection_service", is);
        registry.rebind("consulting_service", cs);

        System.out.println("Server up and running: " + registry);
        System.out.println(as);
        System.out.println(vs);
        System.out.println(is);
        System.out.println(cs);
    }
}
