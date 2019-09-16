package Elections.Models;

import java.io.Serializable;
import java.util.List;

public class Vote implements Serializable {

    private int desk;
    // quizas podria haber otra alternativa mejor para guardar los votos en orden de preferencia
    private List<PoliticalParty> preferredParties;
    private Province province;

    public Vote(int desk, List<PoliticalParty> preferredParties, Province province) {
        this.desk = desk;
        this.preferredParties = preferredParties;
        this.province = province;
    }

    public int getDesk() {
        return desk;
    }

    public void setDesk(int desk) {
        this.desk = desk;
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
                "desk=" + desk +
                ", preferredParties=" + preferredParties +
                ", province=" + province +
                '}';
    }
}
