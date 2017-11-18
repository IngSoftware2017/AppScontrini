package database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.net.Uri;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Federico Taschin on 08/11/2017.
 * Modified by Marco Olivieri on 14/11/2017
 */
//Entity class of Ticket. Should not be used outside of the database module.

@Entity(tableName = Constants.TICKET_TABLE_NAME) @TypeConverters(Converters.class)

public class TicketEntity extends Ticket {
    //@PrimaryKey(autoGenerate = true) @ColumnInfo(name = Constants.TICKET_PRIMARY_KEY_NAME)
    //private int ID;

}
