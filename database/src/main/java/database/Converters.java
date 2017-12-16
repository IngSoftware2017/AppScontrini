package database;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

import java.io.File;
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
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Converts from Date to the corresponding timestamp
     * @param date not null, the Date object to be converted
     * @return the corresponding timestamp
     */
    @TypeConverter
    public Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Converts from a double to a BigDecimal object
     * @param value the value to be converted
     * @return the corresponding BigDecimal
     */
    @TypeConverter
    public BigDecimal fromDouble(Double value){
        return value == null ? null :  new BigDecimal(value);
    }

    /**
     * Converts from BigDecimal to double
     * @param value not null
     * @return
     */
    @TypeConverter
    public Double bigDecimalToDoouble(BigDecimal value){
        return value == null ? null:value.doubleValue();
    }

    @TypeConverter
    public String toString(Uri uri){
        return uri == null ? null: uri.getPath();
        //return uri.getPath();
    }

    @TypeConverter
    public Uri toUri(String path){
        return path == null ? null : Uri.fromFile(new File(path));
    }
}
