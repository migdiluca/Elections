package Elections.client;

import CSVUtils.CSVUtil;
import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import javafx.util.Pair;
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
            System.out.println("There was a problem reading the arguments");
            System.exit(1);
        }

        // si llegamos aca esta recibimos los argumentos de manera correcta
        // iniciamos la conección con el servicio de query
        String[] arr = client.getIp().split(":", -1);
        final ConsultingService cs;
        final AdministrationService as;
        try {
            final Registry registry = LocateRegistry.getRegistry(arr[0], Integer.parseInt(arr[1]));
            cs = (ConsultingService) registry.lookup(ConsultingService.SERVICE_NAME);
        } catch (RemoteException e) {
            System.out.println("There where problems finding the registry at ip: " + client.getIp());
            return;
        } catch (NotBoundException e) {
            System.out.println("There where problems finding the service needed service ");
            return;
        }

        List<Pair<BigDecimal, PoliticalParty>> results = null;
        try {
            if (client.getTable().isPresent()) {
                results = cs.checkResultDesk(client.getTable().get());
            } else if (client.getState().isPresent()) {
                results = cs.checkResultProvince(client.getState().get());
            } else {
                results = cs.checkResultNational();
            }
        } catch (RemoteException e) {
            System.out.println("There was an error retriving results from" + ConsultingService.SERVICE_NAME);
            return;
        } catch (ElectionStateException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println(results);
        CSVUtil.writeCsv(Paths.get(client.getVotesFileName()), results);
    }
}
