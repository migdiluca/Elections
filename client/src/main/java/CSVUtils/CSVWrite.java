package CSVUtils;

import Elections.Models.PoliticalParty;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CSVWrite {

    public static void writeCsvFromBean(Path path, List<Pair<Double, PoliticalParty>> result) {
        try {
            Writer writer = Files.newBufferedWriter(path);
            ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
            String[] beanColumnNames = {"Key", "Value"};
            String[] headerColumnNames = {"Porcentaje", "Partido"};
            strategy.setColumnMapping(beanColumnNames);
            strategy.setType(Pair.class);

            // tratar de solucionar el problema este, esto es nefasto
            writer.append(headerColumnNames[0]);
            writer.append(';');
            writer.append(headerColumnNames[1]);
            writer.append('\n');

            StatefulBeanToCsv sbc = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withMappingStrategy(strategy)
                    .withSeparator(';')
                    .build();

            sbc.write(result);
            writer.close();
        } catch (CsvDataTypeMismatchException e) {
            e.printStackTrace();
        } catch (CsvRequiredFieldEmptyException e) {
            e.getMessage();
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
