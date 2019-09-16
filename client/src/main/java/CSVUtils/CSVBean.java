package CSVUtils;

import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.OpenCSV.PoliticalPartyListConverter;
import Elections.OpenCSV.ProvinceConverter;
import com.opencsv.bean.*;

import java.io.Reader;
import java.util.List;

public class CSVBean {

    @CsvBindByPosition(position = 0)
    private Integer table;

//    @CsvBindByPosition(position = 1)
//    private String province;

    @CsvCustomBindByPosition(converter = ProvinceConverter.class, position = 1)
    private Province province;

//    @CsvBindByPosition(position = 2)
//    private String politicalPartys; // string comma separeted in order of candidate preference

    @CsvCustomBindByPosition(converter = PoliticalPartyListConverter.class, position = 2)
    private List<PoliticalParty> politicalParties;

    public Integer getTable() {
        return table;
    }

    public void setTable(Integer table) {
        this.table = table;
    }

//    public String getProvince() {
//        return province;
//    }
//
//    public void setProvince(String province) {
//        this.province = province;
//    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public List<PoliticalParty> getPoliticalParties() {
        return politicalParties;
    }

    public void setPoliticalParties(List<PoliticalParty> politicalParties) {
        this.politicalParties = politicalParties;
    }


    //    public String getPoliticalPartys() {
//        return politicalPartys;
//    }
//
//    public void setPoliticalPartys(String politicalPartys) {
//        this.politicalPartys = politicalPartys;
//    }

    public static List<CSVBean> beanBuilder(Reader reader) throws Exception {
        ColumnPositionMappingStrategy<CSVBean> ms = new ColumnPositionMappingStrategy<CSVBean>();
        ms.setType(CSVBean.class);

        CsvToBean<CSVBean> cb = new CsvToBeanBuilder<CSVBean>(reader)
                .withMappingStrategy(ms)
                .withSeparator(';')
                .build();
        return cb.parse();
    }

    @Override
    public String toString() {
        return "CSVBean{" +
                "table=" + table +
                ", province='" + province + '\'' +
                ", politicalPartys='" + politicalParties + '\'' +
                '}';
    }
}
