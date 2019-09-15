package Elections.server;

import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.FiscalService;
import Elections.VotingService;
import Elections.server.ServiceImpl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int port = 8090;

    public static void main(String[] args) throws RemoteException {

        logger.info("Elections Server Starting ...");

        Election electionState = new Election();

        AdministrationService as = new AdministrationServiceImpl(electionState);
        VotingService vs = new VotingServiceImpl(electionState);
        FiscalService is = new FiscalServiceImpl(electionState);
        ConsultingService cs = new ConsultingServiceImpl(electionState);

        final Registry registry = LocateRegistry.createRegistry(port);

        registry.rebind(AdministrationService.SERVICE_NAME, as);
        registry.rebind(VotingService.SERVICE_NAME, vs);
        registry.rebind(FiscalService.SERVICE_NAME, is);
        registry.rebind(ConsultingService.SERVICE_NAME, cs);

        System.out.println("Server up and running: " + registry);
        System.out.println(as);
        System.out.println(vs);
        System.out.println(is);
        System.out.println(cs);
    }
}
