package database;

import android.arch.persistence.room.TypeConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Federico Taschin on 12/11/2017.
 */

public final class Converters {

    /**
     * Converts from a timestamp to a Date object
     * @param value the timestamp
     * @return the corresponding Date object
     */
    @TypeConverter
    public Date fromTimestamp(long value) {
        return new Date(value);
    }

    /**
     * Converts from Date to the corresponding timestamp
     * @param date not null, the Date object to be converted
     * @return the corresponding timestamp
     */
    @TypeConverter
    public long dateToTimestamp(Date date) {
        return date.getTime();
    }

    /**
     * Converts from a double to a BigDecimal object
     * @param value the value to be converted
     * @return the corresponding BigDecimal
     */
    @TypeConverter
    public BigDecimal fromDouble(double value){
        return new BigDecimal(value);
    }

    /**
     * Converts from BigDecimal to double
     * @param value not null
     * @return
     */
    public double bigDecimalToDoouble(BigDecimal value){
        return value.doubleValue();
    }
}
