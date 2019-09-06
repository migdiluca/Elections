package Elections.client;

import Elections.Models.PoliticalParty;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.Optional;

public class FiscalClient {

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where the fiscal service is located.", required = true)
    private String ip;

    @Option(name = "-Dparty", aliases = "--partyName", usage = "Name of political party to inspect", required = true)
    private PoliticalParty party;

    @Option(name = "-Did", aliases = "--pollingPlaceNumber", usage = "Table number to inspect", required = true)
    private Integer table;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public PoliticalParty getParty() {
        return party;
    }

    public void setParty(PoliticalParty party) {
        this.party = party;
    }

    public Integer getTable() {
        return table;
    }

    public void setTable(Integer table) {
        this.table = table;
    }

    public static void main(String[] args) {
        FiscalClient client = new FiscalClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            // todo: no imprimir un stack asi nomas
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println(client.getIp());
        System.out.println(client.getParty().name());
        System.out.println(client.getTable());
    }
}
