package prism.akash.tools.date;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class dateParse {

    public Date parseDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyyMMdd").parse(date);
    }

    public Date parseDateTime(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
    }

    public Date parseTime(String date) throws ParseException {
        return new SimpleDateFormat("HH:mm:ss").parse(date);
    }
}
