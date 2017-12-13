package database;

import android.content.Context;

import java.util.List;

/**
 * Created by Federico Taschin on 07/11/2017.
 * Modified by Marco Olivieri on 14/11/2017
 *
 * Modify: improve methods and complete documentation
 * @author Matteo Mascotto
 */

public class DataManager {
    private static DataManager dataManager;
    private Database database; //Database object. All operations on the database pass through this
                                     //Queries are defined in the DAO interface

    private DataManager(Context context){
        //receives the instance of the database
        database = Database.getAppDatabase(context);
    }

    public static DataManager getInstance(Context context){
        if(dataManager ==null){
            dataManager = new DataManager(context);
            return dataManager;
        }else{
            return dataManager;
        }
    }

    /** Adds a ticketEntity into the database
     * @param ticketEntity TicketEntity not null,
     *               ticketEntity.getFileUri() not null,
     *               ticketEntity.getMissionId() not null
     * @return ID of the created record, -1 if the method fails
     */
    public int addTicket(TicketEntity ticketEntity){
        ticketEntity.setID((int) database.ticketDao().addTicket(ticketEntity));
        return ticketEntity.getID();
    }

    /** Adds a missionEntity into the database
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *                missionEntity.personID not null, must be an existing code
     * @return the id of the inserted missionEntity
     */
    public int addMission(MissionEntity missionEntity){
        missionEntity.setID((int) database.ticketDao().addMission(missionEntity));
        return missionEntity.getID();
    }

    /** Executes the insert query.
     * @param personEntity PersonEntity not null, the entity to be inserted
     * @return the id of the inserted personEntity
     */
    public int addPerson(PersonEntity personEntity){
        personEntity.setID((int) database.ticketDao().addPerson(personEntity));
        return personEntity.getID();
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

    /**Updates values of the ticketEntity with the same id in the database. All fields (except ID) are updated
    * @param ticketEntity TicketEntity not null,
     *              ticketEntity.getFileUri() must be a valid photo path
     *              ticketEntity.getMissionID() must be a valid MissionEntity ID
    * @return true if the update is executed, false otherwise (i.e. invalid ID)
    */
    public boolean updateTicket(TicketEntity ticketEntity){
         return database.ticketDao().updateTicket(ticketEntity)>0; //true if at least a ticketEntity is updated
    }

    /**Updates values of the missionEntity with the same id in the database. All fields (except ID) are updated
     * @param missionEntity MissionEntity not null
     *                missionEntity.getPersonID() must be a valid PersonEntity ID
     * @return true if the update is executed, false otherwise (i.e. invalid ID)
     */
    public boolean updateMission(MissionEntity missionEntity){
         return database.ticketDao().updateMission(missionEntity)>0;
    }

    /**Updates values of the PersonEntity with the same id in the database. All fields (except ID) are updated
     * @param personEntity PersonEntity not null
     * @return true if the update is executed, false otherwise (i.e. invalid ID)
     */
    public boolean updatePerson(PersonEntity personEntity){
        return database.ticketDao().updatePerson(personEntity)>0;
    }

    /**
     * @return List<TicketEntity> not null, which contains all the tickets in the database
     */
    public List<TicketEntity> getAllTickets(){
        return database.ticketDao().getAllTickets();
    }

    /**
     * Return a list of the all Persons
     *
     * @return List<MissionEntity>
     */
    public List<MissionEntity> getAllMission(){return database.ticketDao().getAllMission(); }

    /**
     * Return a list of the all Persons
     *
     * @return List<PersonEntity>
     */
    public List<PersonEntity> getAllPerson(){return database.ticketDao().getAllPerson(); }

    /**
     * Return a list of the tickets associate to a specific Mission
     *
     * @param id identifier of the Mission
     * @return List<TicketEntity>
     */
    public List<TicketEntity> getTicketsForMission(int id){
        return database.ticketDao().getTicketsForMission(id);
    }

    /**
     * Return a specific Ticket from a given ID
     *
     * @param id identifier of the Ticket
     * @return TicketEntity
     */
    public TicketEntity getTicket(int id){
        return database.ticketDao().getTicket(id);
    }

    /**
     * Return a specific Person from a given ID
     *
     * @param id identifier of the Person
     * @return PersonEntity
     */
    public PersonEntity getPerson(int id){
        return database.ticketDao().getPerson(id);
    }
//    /**
//     Turns a List of TicketEntity into a List of TicketEntity
//     * @param ticketEntities not null
//     * @return an instance of TicketEntity with values from the TicketEntity received
//     */
//    private List<TicketEntity> toTicket(List<TicketEntity> ticketEntities){
//        ArrayList<TicketEntity> tickets = new ArrayList<TicketEntity>();
//        for(TicketEntity entity : ticketEntities){
//            tickets.add((TicketEntity) entity);
//        }
//        return tickets;
//    }
}
