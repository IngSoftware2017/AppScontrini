package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;


/**
 * Created by Federico Taschin on 08/11/2017.
 */

//Data Access Objects Interface. Defines the queries using Room libraries

@Dao
public interface DAO {

    /* executes the insert query. Inherits the same specifications from Ticket.addTicket(Ticket)     *
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    int addTicket(TicketEntity ticket);



}
