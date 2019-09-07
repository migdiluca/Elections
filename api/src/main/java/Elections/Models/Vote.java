package Elections.Models;

import java.util.List;

public class Vote {

    private int table;
    // quizas podria haber otra alternativa mejor para guardar los votos en orden de preferencia
    private List<PoliticalParty> preferredParties;
    private Province province;

    public Vote(int table, List<PoliticalParty> preferredParties, Province province) {
        this.table = table;
        this.preferredParties = preferredParties;
        this.province = province;
    }

    public int getTable() {
        return table;
    }

    public void setTable(int table) {
        this.table = table;
    }

    public List<PoliticalParty> getPreferredParties() {
        return preferredParties;
    }

    public void setPreferredParties(List<PoliticalParty> preferredParties) {
        this.preferredParties = preferredParties;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
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
