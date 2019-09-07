package CSVUtils;

import Elections.Models.*;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Data implements Supplier<List<Vote>> {

    private Path csvPath;

    public Data(Path csvPath) {
        this.csvPath = csvPath;
    }

    private List<CSVBean> getVotes(Path path) {
        List<CSVBean> dataList = null;
        try { // se cerraba solo el Reader en un try catch no?
            Reader reader = Files.newBufferedReader(path);
            dataList = CSVBean.beanBuilder(reader);
            reader.close();
        } catch (Exception e) {
            System.err.println("An error has been encountered while reading votes file");
            System.err.println("Exiting...");
            System.exit(1);
        }
        return dataList;
    }

    @Override
    public List<Vote> get() {
        return getVotes(csvPath).stream()
                .map(csvBean -> new Vote(csvBean.getTable(),
                        csvBean.getPoliticalParties(),
                        //                Arrays.stream(csvBean.getPoliticalPartys().split(",")).map(PoliticalParty::valueOf).collect(Collectors.toList()),
                        //                Province.valueOf(csvBean.getProvince()))
                        csvBean.getProvince())
                ).collect(Collectors.toList());
    }
}
