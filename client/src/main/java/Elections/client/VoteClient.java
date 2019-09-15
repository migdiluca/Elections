package Elections.client;

import CSVUtils.Data;
import Elections.AdministrationService;
import Elections.ConsultingService;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.ElectionState;
import Elections.Models.Vote;
import Elections.VotingService;
import org.kohsuke.args4j.Option;
import sun.nio.ch.ThreadPool;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
        String[] arr = client.getIp().split(":", -1);
        final VotingService vs;
        try {
            final Registry registry = LocateRegistry.getRegistry(arr[0], Integer.parseInt(arr[1]));
            vs = (VotingService) registry.lookup(VotingService.SERVICE_NAME);
        } catch (RemoteException e) {
            System.err.println("There where problems finding the registry at ip: " + client.getIp());
            System.err.println(e.getMessage());
            return;
        } catch (NotBoundException e) {
            System.err.println("There where problems finding the service needed service ");
            System.err.println(e.getMessage());
            return;
        }

        // si llegamos acá es porque los comicios estaban abiertos haces unos segundos
        // subimos los votos
        if (client.uploadVotes(vs, votes)) {
            System.out.println(votes.size() + " votes registered");
        }

    }

    private boolean uploadVotes(VotingService vs, List<Vote> votes){
        int bulkPacketsAmount = (int) Math.ceil(votes.size() / VotingService.bulkSize);
        try {
            for (int i = 0; i < bulkPacketsAmount; i++) {
                List<Vote> sublist = new ArrayList<>(votes.subList(i * bulkPacketsAmount, i * bulkPacketsAmount + VotingService.bulkSize));
                vs.vote(sublist);
            }
        } catch (RemoteException e) {
            System.err.println("There was an error uploading the votes" + VotingService.SERVICE_NAME);
            System.err.println(e.getMessage());
            return false;
        } catch (ElectionStateException e) {
            System.err.println("Elections are not open: " + VotingService.SERVICE_NAME);
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    /*
    * public static void main(String[] args) {
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

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                // iniciamos la conección con el servidor
                String[] arr = client.getIp().split(":", -1);
                final VotingService vs;
                final AdministrationService as;
                try {
                    final Registry registry = LocateRegistry.getRegistry(arr[0], Integer.parseInt(arr[1]));
                    vs = (VotingService) registry.lookup(VotingService.SERVICE_NAME);
                    as = (AdministrationService) registry.lookup(AdministrationService.SERVICE_NAME);
                    // TODO borrar esto
                    // solo por testeo vamos a habilitarlas
                    as.openElections();
                    if (as.getElectionState() != ElectionState.RUNNING) {
                        throw new ElectionStateException();
                    }
                } catch (RemoteException e) {
                    System.err.println("There where problems finding the registry at ip: " + client.getIp());
                    System.err.println(e.getMessage());
                    return;
                } catch (NotBoundException e) {
                    System.err.println("There where problems finding the service needed service ");
                    System.err.println(e.getMessage());
                    return;
                } catch (ElectionStateException e) {
                    System.err.println(e.getMessage()!=null? e.getMessage(): "Elections are not open");
                    return;
                }

                // si llegamos acá es porque los comicios estaban abiertos haces unos segundos
                // subimos los votos
                if (client.uploadVotes(vs, votes)) {
                    System.out.println(votes.size() + " votes registered");

                }
            });
        }
        try {
            System.out.println("Esperando" + new Date());
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
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
    }*/
}
