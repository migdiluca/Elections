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
    private static final int DEFAULT_PORT = 1099;

    public static void main(String[] args) throws RemoteException {
        int port = DEFAULT_PORT;

        if (args.length == 1) {
            if (!args[0].matches("^[0-9]+$")) {
                System.out.println("Invalid port number");
                System.exit(1);
            }
            // If it matches regular expresion, it can be parsed to integer. But still we don't know if port is an available port or existent
            port = Integer.parseInt(args[0]);
        } else if (args.length > 1) {
            System.out.println("Invalid arguments, only enter port number.\nIf not default port will be used");
            System.exit(1);
        }

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

        System.out.println("Server up and running on port: " + port);
    }
}
