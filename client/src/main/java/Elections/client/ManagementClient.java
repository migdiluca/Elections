package Elections.client;

import org.kohsuke.args4j.Option;

import java.io.IOException;

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
            // todo: no imprimir un stack asi nomas
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println(client.getAction().name());
        System.out.println(client.getIp());
    }
}
