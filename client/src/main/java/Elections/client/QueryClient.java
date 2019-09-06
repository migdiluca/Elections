package Elections.client;

import Elections.Models.Province;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.Optional;

public class QueryClient {

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where the query service is located.", required = true)
    private String ip;

    @Option(name = "-Dstate", forbids = {"-Did"}, aliases = "--stateName", usage = "Name of province to query")
    private Province state;

    @Option(name = "-Did", forbids = {"-Dstate"}, aliases = "--pollingPlaceNumber", usage = "Table number to query")
    private Integer table;

    @Option(name = "-DoutPath", aliases = "--file", usage = "Fully qualified path and name of file to output results.", required = true)
    private String votesFileName;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Optional<Province> getState() {
        return Optional.ofNullable(this.state);
    }

    public void setState(Province state) {
        this.state = state;
    }

    public Optional<Integer> getTable() {
        return Optional.ofNullable(this.table);
    }

    public void setTable(Integer table) {
        this.table = table;
    }

    public String getVotesFileName() {
        return votesFileName;
    }

    public void setVotesFileName(String votesFileName) {
        this.votesFileName = votesFileName;
    }

    public static void main(String[] args) {
        QueryClient client = new QueryClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            // todo: no imprimir un stack asi nomas
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println(client.getIp());
        client.getState().ifPresent(System.out::println);
        client.getTable().ifPresent(System.out::println);
        System.out.println(client.getVotesFileName());
    }
}
