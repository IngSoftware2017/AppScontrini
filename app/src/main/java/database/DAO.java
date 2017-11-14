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
    /** Executes the insert query.
     * @param ticket TicketEntity not null, the entity to be inserted
     *               ticket.fileUri not null, must be a valid file path
     *               ticket.amount not null
     *               ticket.date not null
     * @return the number of inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addTicket(TicketEntity ticket);

    /**Deletes a TicketEntity from the database
     * @param id int the ID of the TicketEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.TICKET_TABLE_NAME+" WHERE "+ Constants.TICKET_PRIMARY_KEY_NAME+" = :id")
    int deleteTicket(int id);

    /**Updates the given TicketEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param ticket TicketEntity not null, the entity to be updated.
     *               ticket.fileUri not null
     *               ticket.amount not null
     *               ticket.date not null
     * @return the number of updated entities
     */
    @Update
    int updateTicket(TicketEntity ticket);

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<TicketEntity>
     */
    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME)
    List<TicketEntity> getAllTickets();

}
