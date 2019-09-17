package CSVUtils;

import Elections.Models.PoliticalParty;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.math.BigDecimal;


public class ResultBean {

    @CsvBindByName(column = "Porcentaje")
    @CsvBindByPosition(position = 0)
    private BigDecimal percentage;

    @CsvBindByName(column = "Partido")
    @CsvBindByPosition(position = 1)
    private PoliticalParty party;

    public ResultBean() {
    }

    public ResultBean(BigDecimal percentage, PoliticalParty party) {
        this.percentage = percentage;
        this.party = party;
    }

    public String getPercentage() {
        return percentage + "%";
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public PoliticalParty getParty() {
        return party;
    }

    public void setParty(PoliticalParty party) {
        this.party = party;
    }

    @Override
    public String toString() {
        return "ResultBean{" +
                "percentage=" + percentage +
                ", party=" + party +
                '}';
    }
}
