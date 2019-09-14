package Elections.client;

import CSVUtils.Data;
import Elections.AdministrationService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.ElectionState;
import Elections.Models.Vote;
import Elections.VotingService;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class VoteClient {

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where voting service is located.", required = true)
    private String ip;

    @Option(name = "-DvotesPath", aliases = "--file", usage = "Fully qualified path and name of votes file.", required = true)
    private String votesFileName;

    public String getVotesFileName() {
        return votesFileName;
    }

    public void setVotesFileName(String votesFileName) {
        this.votesFileName = votesFileName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public static void main(String[] args) {
        VoteClient client = new VoteClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            e.getMessage();
            System.exit(1);
        }
        // si llegamos aca esta recibimos los argumentos de manera correcta
        // levantamos la información del csv
        Data data = new Data(Paths.get(client.getVotesFileName()));
        List<Vote> votes = new ArrayList<>(data.get());

        // iniciamos la conección con el servidor
        String[] serverAddr = client.getIp().split(":", -1);
        final VotingService vs;
        final AdministrationService as;
        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddr[0], Integer.parseInt(serverAddr[1]));
            vs = (VotingService) registry.lookup(VotingService.SERVICE_NAME);
            as = (AdministrationService) registry.lookup(AdministrationService.SERVICE_NAME);
            if (as.getElectionState() != ElectionState.RUNNING) {
                throw new ElectionStateException("Elections are not running");
            }
        } catch (RemoteException e) {
            System.out.println("There where problems finding the registry at ip: " + client.getIp());
            System.out.println(e.getMessage());
            return;
        } catch (NotBoundException e) {
            System.out.println("There where problems finding the service needed service");
            System.out.println(e.getMessage());
            return;
        } catch (ElectionStateException e) {
            System.out.println("Elections are not open");
            System.out.println(e.getMessage());
            return;
        }

        // si llegamos acá es porque los comicios estaban abiertos haces unos segundos
        // subimos los votos
        try {
            if (client.uploadVotes(vs, votes)) {
                System.out.println(votes.size() + " votes registered");
            }
        } catch (RemoteException e) {
            System.out.println("There was an error uploading the votes" + VotingService.SERVICE_NAME);
            System.out.println(e.getMessage());
        } catch (ElectionStateException e) {
            System.out.println("Elections are not open: " + VotingService.SERVICE_NAME);
            System.out.println(e.getMessage());
        }
    }

    private boolean uploadVotes(VotingService vs, List<Vote> votes) throws RemoteException, ElectionStateException {
        int bulkPacketsAmount = (int) Math.ceil(votes.size() / VotingService.bulkSize);
        for (int i = 0; i < bulkPacketsAmount; i++) {
            List<Vote> sublist = new ArrayList<>(votes.subList(i * bulkPacketsAmount, i * bulkPacketsAmount + VotingService.bulkSize));
            vs.vote(sublist);
        }
        return true;
    }
}
