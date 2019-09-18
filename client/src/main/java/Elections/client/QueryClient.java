package Elections.client;

import CSVUtils.CSVUtil;
import Elections.ManagementService;
import Elections.QueryService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Pair;;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QueryClient {

    private String ip;

    @Option(name = "-Dstate", forbids = {"-Did"}, aliases = "--stateName", usage = "Name of province to query")
    private Province state;

    @Option(name = "-Did", forbids = {"-Dstate"}, aliases = "--pollingPlaceNumber", usage = "Desk number to query")
    private Integer desk;

    @Option(name = "-DoutPath", aliases = "--file", usage = "Fully qualified path and name of file to output results.", required = true)
    private String resultFileName;

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where the query service is located.", required = true)
    public void setIp(String ip) throws CmdLineException {
        if (!ip.matches("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})")) {
            throw new CmdLineException("Invalid ip and port address");
        }
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public Optional<Province> getState() {
        return Optional.ofNullable(this.state);
    }

    public void setState(Province state) {
        this.state = state;
    }

    public Optional<Integer> getDesk() {
        return Optional.ofNullable(this.desk);
    }

    public void setDesk(Integer desk) {
        this.desk = desk;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

    public static void main(String[] args) {
        QueryClient client = new QueryClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            System.out.println("There was a problem reading the arguments");
            System.exit(1);
        }

        // if it gets here, than it is receiving the args correctly
        //starting the connection with query service

        String[] arr = client.getIp().split(":", -1);
        final QueryService cs;
        final ManagementService as;
        try {
            final Registry registry = LocateRegistry.getRegistry(arr[0], Integer.parseInt(arr[1]));
            cs = (QueryService) registry.lookup(QueryService.SERVICE_NAME);
        } catch (RemoteException e) {
            System.out.println("There were problems finding the registry at ip: " + client.getIp());
            return;
        } catch (NotBoundException e) {
            System.out.println("There were problems finding the service needed service ");
            return;
        }

        List<Pair<BigDecimal, PoliticalParty>> results;
        try {
            if (client.getDesk().isPresent()) {
                results = cs.checkResultDesk(client.getDesk().get());
            } else if (client.getState().isPresent()) {
                results = cs.checkResultProvince(client.getState().get());
            } else {
                results = cs.checkResultNational();
            }
        } catch (RemoteException e) {
            System.out.println("There was an error retrieving results from " + QueryService.SERVICE_NAME);
            return;
        } catch (ElectionStateException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (results != null && results.size() > 0) {
            // The winner is always the first one on the list.
            Pair<BigDecimal, PoliticalParty> winner = results.get(0);
            // If there was a draw we get a list with the parties.
            List<Pair<BigDecimal, PoliticalParty>> winners = results.stream().filter(p -> p.getKey().compareTo(winner.getKey()) == 0).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            winners.forEach(p -> sb.append(p.getValue().name()).append(", "));
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("won the election");
            System.out.println(sb);
            try {
                CSVUtil.CSVWrite(Paths.get(client.getResultFileName()), results);
            } catch (IOException e) {
                System.out.println("There was an error while writing results to file: " + client.getResultFileName());
                System.exit(1);
            }
        } else {
            if (client.getDesk().isPresent()) {
                System.out.println("Table " + client.getDesk().get() + " has no votes.");
            } else if (client.getState().isPresent()) {
                System.out.println("State " + client.getState().get() + " has no votes.");
            } else {
                System.out.println("No votes found for query.");
            }
        }
    }
}
