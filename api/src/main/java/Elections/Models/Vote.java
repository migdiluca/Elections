package Elections.Models;

import java.io.Serializable;
import java.util.List;

public class Vote implements Serializable {

    private int table;
    // quizas podria haber otra alternativa mejor para guardar los votos en orden de preferencia
    private List<PoliticalParty> preferredParties;
    private Province province;

    public Vote(int table, List<PoliticalParty> preferredParties, Province province) {
        this.table = table;
        this.preferredParties = preferredParties;
        this.province = province;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "table=" + table +
                ", preferredParties=" + preferredParties +
                ", province=" + province +
                '}';
    }
}
