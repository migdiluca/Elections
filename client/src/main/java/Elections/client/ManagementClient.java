package Elections.client;

import Elections.ManagementService;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ManagementClient {

    enum Action {OPEN, STATE, CLOSE}

    private String ip;

    @Option(name = "-Daction", aliases = "--action", usage = "Action to performe", required = true)
    private Action action;

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where administration service is located.", required = true)
    public void setIp(String ip) throws CmdLineException {
        if (!ip.matches("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})")) {
            throw new CmdLineException("Invalid ip and port address");
        }
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public static void main(String[] args) {
        ManagementClient client = new ManagementClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            System.out.println("There was a problem reading the arguments");
            System.exit(1);
        }

        // start server connection
        String[] serverAddr = client.getIp().split(":", -1);
        final ManagementService as;
        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddr[0], Integer.parseInt(serverAddr[1]));
            as = (ManagementService) registry.lookup(ManagementService.SERVICE_NAME);
        } catch (RemoteException e) {
            System.out.println("There where problems finding the registry at ip: " + client.getIp());
            return;
        } catch (NotBoundException e) {
            System.out.println("There where problems finding the service needed service");
            return;
        }
        try {
            switch (client.getAction()) {
                case OPEN:
                    as.openElections();
                    System.out.println(ElectionState.RUNNING.getDesc());
                    break;
                case CLOSE:
                    as.finishElections();
                    System.out.println(ElectionState.FINISHED.getDesc());
                    break;
                case STATE:
                    ElectionState state = as.getElectionState();
                    System.out.println(state.getDesc());
                    break;
            }
        } catch (RemoteException ex) {
            System.out.println("Could not reach service");
            return;
        } catch (ElectionStateException | ServiceException ex) {
            System.out.println(ex.getMessage());
            return;
        }
    }
}
