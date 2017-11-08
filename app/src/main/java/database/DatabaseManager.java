package database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DatabaseManager {
    private TicketDatabase database; //Database object. All operations on the database pass through this
                                     //Queries are defined in the DAO interface

    public DatabaseManager(Context context){
        //receives the instance of the database
        database = TicketDatabase.getAppDatabase(context);
    }

    /* Adds a ticket into the database
     * @param ticket Ticket not null, ticket.getFileUri not null
     * @return ID of the created record, -1 if the method fails
     */
    public int addTicket(Ticket ticket){
        return database.ticketDao().addTicket(toTicketEntity(ticket));
    }

    /*Modifies the values (with the exception of ID) of a ticket stored in the database
    * @param ticket Ticket not null, ticket.getFileUri must be a valid photo path
    *        the modified values are the not null variables of the ticket object passed as parameter. ticket.ID is used to select the
    *        correspondent ticket in the database.
    * @return true if the update is executed, false otherwise (i.e. invalid ID)
    */
    public boolean updateTicket(Ticket ticket){
         return false;
    }

    /*
     * @return List<Ticket> not null, which contains all the tickets in the database
     */
    public List<Ticket> getAllTickets(){
        return new ArrayList<Ticket>();
    }

    /* Turns a Ticket object into a TicketEntity object
     * @param ticket Ticket not null
     * @return an instance of TicketEntity with values from the Ticket received
     */
    private TicketEntity toTicketEntity(Ticket ticket){
        TicketEntity entity = new TicketEntity();
        entity.setAmount(ticket.getAmount());
        entity.setDate(ticket.getDate());
        entity.setFileUri(ticket.getFileUri());
        entity.setShop(ticket.getShop());
        entity.setTitle(ticket.getTitle());
        return entity;
    }


}
