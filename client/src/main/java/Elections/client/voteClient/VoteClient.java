package Elections.client.voteClient;

import Elections.Models.Vote;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class VoteClient {

    @Option(name="-DserverAddress", aliases="--server", usage="Fully qualified ip and port where voting service is located.", required=true)
    private String ip;

    @Option(name="-DvotesPath", aliases="--file", usage="Fully qualified path and name of votes file.", required=true)
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
            client.init(args);
        } catch (IOException e) {
            // todo: no imprimir un stack asi nomas
            e.printStackTrace();
            System.exit(1);
        }
        // si llegamos aca esta recibimos los argumentos de manera correcta
        Data data = new Data(Paths.get(client.getVotesFileName()));
        List<Vote> votes = data.get();
        // todo: inicializo conexion con el servidor de cliente de votos y enviamos los votos recibidos

        System.out.println(votes.size() + " votes registered");
    }

    private void init(final String[] args) throws IOException {
        final CmdLineParser parser = new CmdLineParser(this);
        if (args.length < 1) {
            parser.printUsage(System.err);
            System.exit(1);
        }
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e)
        {
            System.out.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }
    }
}
