package database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.net.Uri;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Federico Taschin on 08/11/2017.
 * Modified by Marco Olivieri on 14/11/2017
 * Modified by Stefano Elardo on 17/11/2017
 */
//Entity class of Ticket. Should not be used outside of the database module.

@Entity(tableName = Constants.TICKET_TABLE_NAME) @TypeConverters(Converters.class)
public class TicketEntity extends Ticket {

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Constants.TICKET_PRIMARY_KEY_NAME)
    private int ID;

    @Ignore
    /**
     * Constructor
     */
    public TicketEntity(){
        super();
    }

    /**
     * Parametric constructor
     *
     * @param fileUri Path associated with the the ticket file stored in the memory
     * @param amount total amount
     * @param shop Name of the shop in which the ticket was issued
     * @param date the issue date of the ticket
     * @param title name given
     */
    public TicketEntity(Uri fileUri, BigDecimal amount, String shop, Date date, String title) {
        super(fileUri, amount, shop, date   ,title);
    }

    /**
     * Returns the ticket ID
     * @return  ID
     */
    public int getID() {
        return ID;
    }

    /**
     * DO NOT USE
     * @param ID
     */
    protected void setID(int ID) {
        this.ID = ID;
    }

}
