package CSVUtils;

import Elections.Models.Province;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class ProvinceConverter extends AbstractBeanField<String> {

    @Override
    protected Object convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        try {
            return Province.valueOf(s);
        } catch (RuntimeException e) {
            throw new CsvDataTypeMismatchException(e.getMessage());
        }
    }
}
