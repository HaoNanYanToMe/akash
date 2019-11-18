package prism.akash.tools.date;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class dateParse {

    /**
     * 格式化时间parse
     * @param format   格式化
     * @param date     时间
     * @return
     * @throws ParseException
     */
    public Date parseDate(String format,String date) throws ParseException {
        return new SimpleDateFormat(format).parse(date);
    }

    /**
     * 格式化时间format
     * @param format   格式化
     * @param date     时间
     * @return
     */
    public String formatDate(String format,Date date){
        return new SimpleDateFormat(format).format(date);
    }
}
