package Elections.OpenCSV;

import Elections.Models.PoliticalParty;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PoliticalPartyListConverter extends AbstractBeanField<String> {

    @Override
    protected Object convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        try {
            return Arrays.stream(s.split(",")).map(PoliticalParty::valueOf).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new CsvDataTypeMismatchException(e.getMessage());
        }
    }
}
