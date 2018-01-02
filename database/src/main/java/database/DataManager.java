package database;

import android.content.Context;

import java.util.Date;
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

    /** Adds a ticketEntity into the database
     * @param ticketEntity TicketEntity not null,
     *               ticketEntity.getFileUri() not null,
     *               ticketEntity.getMissionId() not null
     * @return ID of the created record, -1 if the method fails
     */
    public long addTicket(TicketEntity ticketEntity){
        ticketEntity.setID(database.ticketDao().addTicket(ticketEntity));
        return ticketEntity.getID();
    }

    /** Adds a missionEntity into the database
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *                missionEntity.personID not null, must be an existing code
     * @return the id of the inserted missionEntity
     */
    public long addMission(MissionEntity missionEntity){
        missionEntity.setID(database.ticketDao().addMission(missionEntity));
        return missionEntity.getID();
    }

    /** Executes the insert query.
     * @param personEntity PersonEntity not null, the entity to be inserted
     * @return the id of the inserted personEntity
     */
    public long addPerson(PersonEntity personEntity){
        personEntity.setID(database.ticketDao().addPerson(personEntity));
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

    /**Created by Federico Taschin
     * Updates values of the ticketEntity with the same id in the database. All fields (except ID) are updated
    * @param ticketEntity TicketEntity not null,
     *              ticketEntity.getFileUri() must be a valid photo path
     *              ticketEntity.getMissionID() must be a valid MissionEntity ID
    * @return true if the update is executed, false otherwise (i.e. invalid ID)
    */
    public boolean updateTicket(TicketEntity ticketEntity){
         return database.ticketDao().updateTicket(ticketEntity)>0; //true if at least a ticketEntity is updated
    }

    /**Created by Federico Taschin
     * Updates values of the missionEntity with the same id in the database. All fields (except ID) are updated
     * @param missionEntity MissionEntity not null
     *                missionEntity.getPersonID() must be a valid PersonEntity ID
     * @return true if the update is executed, false otherwise (i.e. invalid ID)
     */
    public boolean updateMission(MissionEntity missionEntity){
         return database.ticketDao().updateMission(missionEntity)>0;
    }

    /**Created by Federico Taschin
     * Updates values of the PersonEntity with the same id in the database. All fields (except ID) are updated
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
    
    public List<MissionEntity> getAllMission(){
        return database.ticketDao().getAllMission();
    }

    public List<PersonEntity> getAllPerson(){
        return database.ticketDao().getAllPerson();
    }

    public List<TicketEntity> getTicketsForMission(int id){
        return database.ticketDao().getTicketsForMission(id);
    }

    /**Created by Federico Taschin
     *Executes a SELECT query for a given TicketEntity id
     *@param id int, the id of the TicketEntity
     *@return List<TicketEntity> not null (at least of 0 size) which contains all the tickets with the given id
     */
    public TicketEntity getTicket(int id){
        return database.ticketDao().getTicket(id);
    }

    /**Created by Federico Taschin
     * Gets all the TicketEntity with the given date
     * @param date Date not null, the date of the ticket
     * @return List<TicketEntity> not null with all TicketEntity with the given date
     */
    public List<TicketEntity> getTicketWithDate(Date date){
        return database.ticketDao().getTicketWithDate(date);
    }

    /**Created by Federico Taschin
     * Gets all TicketEntity with the given category
     * @param category String not null, category to be searched
     * @return List<TicketEntity> not null with all TicketEntity with the given category
     */
    public List<TicketEntity> getTicketWithCategory(String category){
        return database.ticketDao().getTicketWithCategory(category);
    }

    /**Created by Federico Taschin
     * Gets all the MissionEntity with the given start date
     * @param startDate not null, the start date of the mission to be searched
     * @return List<MissionEntity> with all MissionEntity with the given start date
     */
    public List<MissionEntity> getMissionWithStartDate(Date startDate){
        return database.ticketDao().getMissionWithStartDate(startDate);
    }

    /**Created by Federico Taschin
     * Gets all the MissionEntity with the given end date
     * @param endDate not null, the end date of the mission to be searched
     * @return List<MissionEntity> with all MissionEntity with the given end date
     */
    public List<MissionEntity> getMissionWithEndDate(Date endDate){
        return database.ticketDao().getMissionWithEndDate(endDate);
    }

    /**Created by Federico Taschin
     * Gets all the MissionEntity with the given location
     * @param location not null, the location of the mission to be searched
     * @return List<MissionEntity> not null with all MissionEntity with the given location
     */
    public List<MissionEntity> getMissionWithLocation(String location){
        return database.ticketDao().getMissionWithLocation(location);
    }

    /**Created by Federico Taschin
     * Gets all the PersonEntity with the given name
     * @param name String not null
            * @return List<PersonEntity> not null, with all PersonEntity with the given name
     */
    public List<PersonEntity> getPersonWithName(String name){
        return database.ticketDao().getPersonWithName(name);
    }

    /**Created by Federico Taschin
     * Gets all the PersonEntity with the given last name
     * @param lastName String not null
     * @return List<PersonEntity> not null, with all PersonEntity with the given last name
     */
    public List<PersonEntity> getPersonWithLastName(String lastName){
        return database.ticketDao().getPersonWithLastName(lastName);
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
