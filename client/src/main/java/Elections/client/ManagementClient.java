package Elections.client;

import Elections.AdministrationService;
import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ServiceException;
import Elections.Models.ElectionState;
import Elections.VotingService;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ManagementClient {

    enum Action {OPEN, STATE, CLOSE}

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where administration service is located.", required = true)
    private String ip;

    @Option(name = "-Daction", aliases = "--action", usage = "Action to performe", required = true)
    private Action action;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
            e.getMessage();
            System.exit(1);
        }

        // iniciamos la conección con el servidor
        String[] serverAddr = client.getIp().split(":", -1);
        final AdministrationService as;
        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddr[0], Integer.parseInt(serverAddr[1]));
            as = (AdministrationService) registry.lookup(AdministrationService.SERVICE_NAME);
        } catch (RemoteException e) {
            System.out.println("There where problems finding the registry at ip: " + client.getIp());
            System.out.println(e.getMessage());
            return;
        } catch (NotBoundException e) {
            System.out.println("There where problems finding the service needed service");
            System.out.println(e.getMessage());
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
        } catch (ElectionStateException | ServiceException ex) {
            ex.getMessage();
        }
    }
}
