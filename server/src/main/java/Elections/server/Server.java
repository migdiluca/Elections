package Elections.server;

import Elections.ManagementService;
import Elections.QueryService;
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
    private static final int port = 1099;

    public static void main(String[] args) throws RemoteException {

        logger.info("Elections Server Starting ...");

        Election electionState = new Election();

        ManagementService as = new ManagementServiceImpl(electionState);
        VotingService vs = new VotingServiceImpl(electionState);
        FiscalService is = new FiscalServiceImpl(electionState);
        QueryService cs = new QueryServiceImpl(electionState);

        final Registry registry = LocateRegistry.createRegistry(port);

        registry.rebind(ManagementService.SERVICE_NAME, as);
        registry.rebind(VotingService.SERVICE_NAME, vs);
        registry.rebind(FiscalService.SERVICE_NAME, is);
        registry.rebind(QueryService.SERVICE_NAME, cs);

        System.out.println("Server up and running on port:" + port);
    }
}
