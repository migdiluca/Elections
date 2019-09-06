package Elections.Models;

import java.util.List;

public class Vote {

    private int tabel;
    // quizas podria haber otra alternativa mejor para guardar los votos en orden de preferencia
    private List<PoliticalParty> prefferedParties;
    private Province province;

    public Vote(int tabel, List<PoliticalParty> prefferedParties, Province province) {
        this.tabel = tabel;
        this.prefferedParties = prefferedParties;
        this.province = province;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "tabel=" + tabel +
                ", prefferedParties=" + prefferedParties +
                ", province=" + province +
                '}';
    }
}
