package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


/**
 * Created by Federico Taschin on 08/11/2017.
 */

//Data Access Objects Interface. Defines the queries using Room libraries

@Dao
public interface DAO {
    /* executes the insert query. Inherits the same specifications from Ticket.addTicket(Ticket)     *
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addTicket(TicketEntity ticket);

    /***
     * // AGGIUNGERE LE SPECIFICHE
     *
     * @param id
     * @return
     */
    @Query("DELETE FROM "+ Constants.TICKET_TABLE_NAME+" WHERE "+ Constants.TICKET_PRIMARY_KEY_NAME+" = :id")
    int deleteTicket(int id);

    @Update
    int updateTicket(TicketEntity ticket);

    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME)
    List<TicketEntity> getAllTickets();

}
