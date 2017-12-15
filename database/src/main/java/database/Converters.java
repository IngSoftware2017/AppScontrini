package database;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Federico Taschin on 12/11/2017.
 * This class defines the converters for the database. In fact, the Room library cannot handle Date, BigDecimal and Uri objects.
 * Therefore, we defined these methods that transform those objects into objects that the Database can handle. 
 * For instance, we defined the dateToTimeStamp(Date date) method. When the Database has to write a Date object, it uses the 
 * dateToTimeStamp(Date date) method to cast it to a Long object (that the Database can read). Conversely, when the Database needs 
 * to read a Date object, it reads a Long and then it cast it to Date with the fromTimestamp(Long value) method.
 * These methods are automatically called by the Room library since they're annotated with @TypeConverter.
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
