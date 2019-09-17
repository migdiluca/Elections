package CSVUtils;

import Elections.Models.PoliticalParty;
import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CSVUtil {
    
    public static void writeCsv(Path path, List<Pair<BigDecimal, PoliticalParty>> result) {
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

    public static void writeCsvBean(Path path, List result, Class clazz, String[] beanColumnNames) {
        try {
            Writer writer = Files.newBufferedWriter(path);
            ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
            strategy.setColumnMapping(beanColumnNames);
            strategy.setType(clazz);


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


    private static String[] toCsvRow(Pair<BigDecimal, PoliticalParty> pair) {
        return new String[]{pair.getKey().toString() + "%", pair.getValue().name()};
    }

    /*
        Returns true if no IOExceptin, otherwise false
     */
    private static void writePair(Pair<BigDecimal, PoliticalParty> pair, Writer writer) throws RuntimeException {
        try {
            writer.write(pair.getKey().toString() + "%" + pair.getValue().name() + "\n");
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static void csvWrite(Path path, List<Pair<BigDecimal, PoliticalParty>> result) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(path.toString()), ';', '\0','\0', "\r\n");

        List<String[]> rows = result.stream().map(CSVUtil::toCsvRow).collect(Collectors.toList());

        // Write column names
        writer.writeNext(new String[]{"Porcentaje", "Partido"});
        // Write rows
        writer.writeAll(rows, false);

        writer.close();
    }

    public static void csvWrite2(Path path, List<Pair<BigDecimal, PoliticalParty>> result) throws IOException {
        Writer writer = new FileWriter(path.toString());
        writer.write("Porcentaje;Partido\n");
        try {
            result.forEach(pair -> writePair(pair, writer));
        } catch (RuntimeException ex) {
            throw new IOException();
        }
        writer.close();
    }

    public static List CSVRead(Path path, Class bean) throws Exception {
        ColumnPositionMappingStrategy ms = new ColumnPositionMappingStrategy();
        ms.setType(bean);

        Reader reader = Files.newBufferedReader(path);

        CsvToBean cb = new CsvToBeanBuilder(reader)
                .withMappingStrategy(ms)
                .withSeparator(';')
                .build();
        List beanList = cb.parse();
        reader.close();
        return beanList;
    }
}
