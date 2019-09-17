package CSVUtils;

import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import com.opencsv.bean.*;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVUtil {

    /*
        Returns true if no IOExceptin, otherwise false
     */
    private static void writePair(Pair<BigDecimal, PoliticalParty> pair, Writer writer) throws RuntimeException {
        try {
            writer.write(pair.getKey().toString() + "%;" + pair.getValue().name() + "\n");
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /*
        Returns true if no IOExceptin, otherwise false
     */
    public static void CSVWrite(Path path, List<Pair<BigDecimal, PoliticalParty>> result) throws IOException {
        Writer writer = new FileWriter(path.toString());
        writer.write("Porcentaje;Partido\n");
        try {
            result.forEach(pair -> writePair(pair, writer));
            writer.close();
        } catch (RuntimeException ex) {
            throw new IOException();
        }
    }

    public static List<Vote> CSVRead(Path path) throws Exception {
        ColumnPositionMappingStrategy<Vote> ms = new ColumnPositionMappingStrategy<Vote>();
        ms.setType(Vote.class);

        Reader reader = Files.newBufferedReader(path);

        CsvToBean<Vote> cb = new CsvToBeanBuilder<Vote>(reader)
                .withMappingStrategy(ms)
                .withSeparator(';')
                .build();
        List<Vote> beanList = cb.parse();
        reader.close();
        return beanList;
    }
}
