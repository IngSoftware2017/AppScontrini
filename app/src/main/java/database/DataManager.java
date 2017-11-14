package database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DataManager {
    private Database database; //Database object. All operations on the database pass through this
                                     //Queries are defined in the DAO interface

    public DataManager(Context context){
        //receives the instance of the database
        database = Database.getAppDatabase(context);
    }

    /** Adds a ticket into the database
     * @param ticket Ticket not null, ticket.getFileUri not null
     * @return ID of the created record, -1 if the method fails
     */
    public int addTicket(Ticket ticket){
        return (int)database.ticketDao().addTicket((TicketEntity) ticket);
    }

    /**Modifies the values (with the exception of ID) of a ticket stored in the database
    * @param ticket Ticket not null, ticket.getFileUri must be a valid photo path
    *        the modified values are the not null variables of the ticket object passed as parameter. ticket.ID is used to select the
    *        correspondent ticket in the database.
    * @return true if the update is executed, false otherwise (i.e. invalid ID)
    */
    public boolean updateTicket(Ticket ticket){
         return database.ticketDao().updateTicket((TicketEntity) ticket)>0; //true if at least a ticket is updated
    }

    /**
     * @return List<Ticket> not null, which contains all the tickets in the database
     */
    public List<Ticket> getAllTickets(){
        return toTicket(database.ticketDao().getAllTickets());
    }

    /**
     Turns a List of TicketEntity into a List of Ticket
     * @param ticketEntities not null
     * @return an instance of Ticket with values from the TicketEntity received
     */
    private List<Ticket> toTicket(List<TicketEntity> ticketEntities){
        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        for(TicketEntity entity : ticketEntities){
            tickets.add((TicketEntity) entity);
        }
        return tickets;
    }
}
