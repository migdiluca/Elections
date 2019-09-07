package Elections.client;

import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsNotStartedException;
import Elections.InspectionClient;
import Elections.InspectionService;
import Elections.Models.PoliticalParty;
import com.sun.security.ntlm.Server;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;

public class FiscalClient implements InspectionClient {

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

        try {
            UnicastRemoteObject.exportObject(client);
            Registry registry = LocateRegistry.getRegistry(client.getIp(), 0);

            InspectionService server = (InspectionService) registry.lookup("inspection_service");
            server.addInspector(client, client.getParty(), client.getTable());

        } catch(NotBoundException e){
            System.out.println("Service not found, wrong name?");
        }catch (ElectionStateException e){
            System.out.println("The election already started or is finished");
        } catch (RemoteException e){
            System.out.println("Remote exception on client side on FiscalClient");
        }
    }

    @Override
    public void notifyVote() throws RemoteException {
        System.out.println("New vote for " + party.name() + " on pooling place " + table.toString());
    }
}
