package Elections.client;

import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.Dimension;
import Elections.Models.Province;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

        // si llegamos aca esta recibimos los argumentos de manera correcta
        // iniciamos la conección con el servicio de query
        String[] arr = client.getIp().split(":", -1);
        final ConsultingService cs;
        final AdministrationService as;
        try {
            final Registry registry = LocateRegistry.getRegistry(arr[0], Integer.parseInt(arr[1]));
            cs = (ConsultingService) registry.lookup(ConsultingService.SERVICE_NAME);
            as = (AdministrationService) registry.lookup("admin");
        } catch (RemoteException e) {
            System.out.println("There where problems finding the registry at ip: " + client.getIp());
            System.out.println(e.getMessage());
            return;
        } catch (NotBoundException e) {
            System.out.println("There where problems finding the service needed service ");
            System.out.println(e.getMessage());
            return;
        }

        try {
            if (client.getTable().isPresent()) {
                cs.checkResult(Dimension.DESK);
            } else if (client.getState().isPresent()) {
                cs.checkResult(Dimension.PROVINCE);
            } else {
                cs.checkResult(Dimension.NATIONAL);
            }
        } catch (RemoteException e) {
            System.out.println("There was an error retriving results from" + ConsultingService.SERVICE_NAME);
            System.out.println(e.getMessage());
        } catch (ElectionStateException e) {
            System.out.println("Elections are not open");
            System.out.println(e.getMessage());
        }

        // generar csv, todo: falta ver que estructura de datos manejamos
    }

}
