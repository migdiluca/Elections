package CSVUtils;

import Elections.Models.*;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class Data implements Supplier<List<Vote>> {

    private Path csvPath;

    public Data(Path csvPath) {
        this.csvPath = csvPath;
    }

    private List<Vote> getVotes(Path path) {
        List<Vote> dataList = null;
        try {
//            Reader reader = Files.newBufferedReader(path);
            dataList = (List<Vote>) CSVUtil.CSVRead(path, Vote.class);
//            reader.close();
        } catch (Exception e) {
            System.err.println("An error has been encountered while reading votes file");
            System.err.println("Exiting...");
            System.exit(1);
        }
        return dataList;
    }

    @Override
    public List<Vote> get() {
        return getVotes(csvPath);
//                .stream()
//                .map(csvBean -> new Vote(csvBean.getTable(),
//                        csvBean.getPoliticalParties(),
//                        //                Arrays.stream(csvBean.getPoliticalPartys().split(",")).map(PoliticalParty::valueOf).collect(Collectors.toList()),
//                        //                Province.valueOf(csvBean.getProvince()))
//                        csvBean.getProvince())
//                ).collect(Collectors.toList());
    }
}
