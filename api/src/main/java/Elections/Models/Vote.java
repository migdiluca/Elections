package Elections.Models;

import Elections.OpenCSV.PoliticalPartyListConverter;
import Elections.OpenCSV.ProvinceConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;

import java.io.Serializable;
import java.util.List;

public class Vote implements Serializable {

    @CsvBindByPosition(position = 0)
    private int desk;

    @CsvCustomBindByPosition(converter = ProvinceConverter.class, position = 1)
    private Province province;

    /*
        Political parties are ordered by preference in descending order
     */
    @CsvCustomBindByPosition(converter = PoliticalPartyListConverter.class, position = 2)
    private List<PoliticalParty> preferredParties;

    public Vote() {
    }

    public Vote(int desk, List<PoliticalParty> preferredParties, Province province) {
        this.desk = desk;
        this.preferredParties = preferredParties;
        this.province = province;
    }

    public Vote(Vote v) {
        this.desk = v.getDesk();
        this.preferredParties = v.getPreferredParties();
        this.province = v.getProvince();
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
                ", province='" + province + '\'' +
                ", preferredParties='" + preferredParties + '\'' +
                '}';
    }
}
