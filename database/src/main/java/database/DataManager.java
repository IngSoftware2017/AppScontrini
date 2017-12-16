package database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico Taschin on 07/11/2017.
 * Modified by Marco Olivieri on 14/11/2017
 */

public class DataManager {
    private Database database; //Database object. All operations on the database pass through this
                                     //Queries are defined in the DAO interface

    public DataManager(Context context){
        //receives the instance of the database
        database = Database.getAppDatabase(context);
    }

    /** Adds a ticket into the database
     * @param ticket Ticket not null,
     *               ticket.getFileUri() not null,
     *               ticket.getMissionId() not null
     * @return ID of the created record, -1 if the method fails
     */
    public int addTicket(Ticket ticket){
        ticket.setID((int) database.ticketDao().addTicket(ticket));
        return ticket.getID();
    }

    /** Adds a mission into the database
     * @param mission Mission not null, the entity to be inserted
     *                mission.personID not null, must be an existing code
     * @return the id of the inserted mission
     */
    public int addMission(Mission mission){
        mission.setID((int) database.ticketDao().addMission(mission));
        return mission.getID();
    }

    /** Executes the insert query.
     * @param person Person not null, the entity to be inserted
     * @return the id of the inserted person
     */
    public int addPerson(Person person){
        person.setID((int) database.ticketDao().addPerson(person));
        return person.getID();
    }

    /**
     * Deletes a ticket from the database
     * @param id the id of the ticket to be deleted
     * @return true if the ticket is deleted, false otherwise
     */
    public boolean deleteTicket(int id){
        return database.ticketDao().deleteTicket(id)>0;
    }

    /**
     * Deletes a mission from the database
     * @param id the id of the mission to be deleted
     * @return true if the mission is deleted, false otherwise
     */
    public boolean deleteMission(int id){
        return database.ticketDao().deleteMission(id)>0;
    }

    /**
     * Deletes a person from the db
     * @param id the id of the person to be deleted
     * @return true if the person is deleted, false otherwise
     */
    public boolean deletePerson(int id){
        return database.ticketDao().deletePerson(id)>0;
    }

    /**Updates values of the ticket with the same id in the database. All fields (except ID) are updated
    * @param ticket Ticket not null,
     *              ticket.getFileUri() must be a valid photo path
     *              ticket.getMissionID() must be a valid Mission ID
    * @return true if the update is executed, false otherwise (i.e. invalid ID)
    */
    public boolean updateTicket(Ticket ticket){
         return database.ticketDao().updateTicket(ticket)>0; //true if at least a ticket is updated
    }

    /**Updates values of the mission with the same id in the database. All fields (except ID) are updated
     * @param mission Mission not null
     *                mission.getPersonID() must be a valid Person ID
     * @return true if the update is executed, false otherwise (i.e. invalid ID)
     */
    public boolean updateMission(Mission mission){
         return database.ticketDao().updateMission(mission)>0;
    }

    /**Updates values of the Person with the same id in the database. All fields (except ID) are updated
     * @param person Person not null
     * @return true if the update is executed, false otherwise (i.e. invalid ID)
     */
    public boolean updatePerson(Person person){
        return database.ticketDao().updatePerson(person)>0;
    }

    /**
     * @return List<Ticket> not null, which contains all the tickets in the database
     */
    public List<Ticket> getAllTickets(){
        return database.ticketDao().getAllTickets();
    }

    public List<Mission> getAllMissions(){ return database.ticketDao().getAllMission();}

    public List<Person> getAllPersons(){ return database.ticketDao().getAllPerson();}



//    /**
//     Turns a List of TicketEntity into a List of Ticket
//     * @param ticketEntities not null
//     * @return an instance of Ticket with values from the TicketEntity received
//     */
//    private List<Ticket> toTicket(List<TicketEntity> ticketEntities){
//        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
//        for(Ticket entity : ticketEntities){
//            tickets.add((Ticket) entity);
//        }
//        return tickets;
//    }
}
