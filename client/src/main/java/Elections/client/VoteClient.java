package Elections.client;

import CSVUtils.CSVUtil;
import CSVUtils.Data;
import CSVUtils.ResultBean;
import Elections.Exceptions.ElectionStateException;
import Elections.Models.Vote;
import Elections.VotingService;
import Elections.server.ServiceImpl.VotingSystems;
import javafx.util.Pair;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.stream.Collectors;

public class VoteClient {

    private String ip;

    @Option(name = "-DvotesPath", aliases = "--file", usage = "Fully qualified path and name of votes file.", required = true)
    private String votesFileName;

    public String getVotesFileName() {
        return votesFileName;
    }

    public void setVotesFileName(String votesFileName) {
        this.votesFileName = votesFileName;
    }

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where voting service is located.", required = true)
    public void setIp(String ip) throws CmdLineException {
        if (!ip.matches("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})")) {
            throw new CmdLineException("Invalid ip and port address");
        }
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public static void main(String[] args) {
        VoteClient client = new VoteClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            System.out.println("There was a problem reading the arguments");
            System.exit(1);
        }
        // if it gets here, than it is receiving the args correctly
        // getting the csv info
        Data data = new Data(Paths.get(client.getVotesFileName()));
        List<Vote> votes = new ArrayList<>(data.get());
        VotingSystems vs = new VotingSystems(votes);
//        try {
//            CSVUtil.writeCsvBean(Paths.get("/Users/fermingomez/Desktop/results.csv"), vs.alternativeVoteNationalLevel(), ResultBean.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        String[] headerColumnNames = {"Porcentaje", "Partido"};
        CSVUtil.writeCsvBean(Paths.get("/Users/fermingomez/Desktop/results.csv"),
                vs.alternativeVoteNationalLevel().stream().map(pair -> new ResultBean(pair.getKey(), pair.getValue())).collect(Collectors.toList()),
                ResultBean.class,
                headerColumnNames);

//
//        // starting server connection
//        String[] serverAddr = client.getIp().split(":", -1);
//        final VotingService vs;
//        try {
//            final Registry registry = LocateRegistry.getRegistry(serverAddr[0], Integer.parseInt(serverAddr[1]));
//            vs = (VotingService) registry.lookup(VotingService.SERVICE_NAME);
//        } catch (RemoteException e) {
//            System.err.println("There where problems finding the registry at ip: " + client.getIp());
//            System.err.println(e.getMessage());
//            return;
//        } catch (NotBoundException e) {
//            System.err.println("There where problems finding the service needed service ");
//            System.err.println(e.getMessage());
//            return;
//        }
//
//        // if it gets here than the election is open(for at least some seconds prior)
//        // sending the votes
//        if (client.uploadVotes(vs, votes)) {
//            System.out.println(votes.size() + " votes registered");
//        }

    }

    private boolean uploadVotes(VotingService vs, List<Vote> votes) {
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
}
