package Elections.Models;

import Elections.OpenCSV.PoliticalPartyListConverter;
import Elections.OpenCSV.ProvinceConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;

import java.io.Serializable;
import java.util.List;

public class Vote implements Serializable {

    @CsvBindByPosition(position = 0)
    private Integer table;

    @CsvCustomBindByPosition(converter = ProvinceConverter.class, position = 1)
    private Province province;

    @CsvCustomBindByPosition(converter = PoliticalPartyListConverter.class, position = 2)
    private List<PoliticalParty> preferredParties;

    public Vote() {
    }

    public Vote(int table, List<PoliticalParty> preferredParties, Province province) {
        this.table = table;
        this.preferredParties = preferredParties;
        this.province = province;
    }

    public Integer getTable() {
        return table;
    }

    public void setTable(Integer table) {
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
                ", province='" + province + '\'' +
                ", preferredParties='" + preferredParties + '\'' +
                '}';
    }
}
