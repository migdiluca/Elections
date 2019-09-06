package Elections.client.voteClient;

import java.nio.file.Paths;

public class VoteClient {

    public static void main(String[] args) {
        Data data = new Data(Paths.get("/Users/fermingomez/Desktop/Elections/generatedVotes1.csv"));
        System.out.println(data.get());
    }
}
