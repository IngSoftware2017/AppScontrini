package database;

import android.content.Context;
import android.widget.ListView;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Federico Taschin on 07/11/2017.
 * Modified by Marco Olivieri
 * Modify by Matteo Mascotto: improve methods and complete documentation
 */

public class DataManager {
    private static DataManager dataManager;
    private Database database; //Database object. All operations on the database pass through this
                                     //Queries are defined in the DAO interface

    public DataManager(Context context){
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
    public long addTicket(TicketEntity ticketEntity){
        ticketEntity.setID(database.ticketDao().addTicket(ticketEntity));
        return ticketEntity.getID();
    }

    /**
     * @author Marco Olivieri
     * Adds a missionEntity into the database
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *                missionEntity.personID not null, must be an existing code
     * @return the id of the inserted missionEntity
     */
    public long addMission(MissionEntity missionEntity){
        missionEntity.setID(database.ticketDao().addMission(missionEntity));
        return missionEntity.getID();
    }

    /**
     * @author Marco Olivieri
     * Executes the insert query.
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
    public boolean deleteTicket(long id){
        return database.ticketDao().deleteTicket(id)>0;
    }

    /**
     * @author Marco Olivieri
     * Deletes a mission from the database
     * @param id the id of the mission to be deleted
     * @return true if the mission is deleted, false otherwise
     */
    public boolean deleteMission(long id){
        return database.ticketDao().deleteMission(id)>0;
    }

    /**
     * @author Marco Olivieri
     * Deletes a person from the db
     * @param id the id of the person to be deleted
     * @return true if the person is deleted, false otherwise
     */
    public boolean deletePerson(long id){
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

    /**
     * @author Marco Olivieri
     * Updates values of the missionEntity with the same id in the database. All fields (except ID) are updated
     * @param missionEntity MissionEntity not null
     *                missionEntity.getPersonID() must be a valid PersonEntity ID
     * @return true if the update is executed, false otherwise (i.e. invalid ID)
     */
    public boolean updateMission(MissionEntity missionEntity){
         return database.ticketDao().updateMission(missionEntity)>0;
    }

    /**
     * @author Marco Olivieri
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

    /**
     * Return a list of the all Persons
     *
     * @return List<MissionEntity>
     */
    public List<MissionEntity> getAllMission(){
        return database.ticketDao().getAllMission();
    }

    /**
     * Return a list of the all Persons
     *
     * @return List<PersonEntity>
     */
    public List<PersonEntity> getAllPerson(){
        return database.ticketDao().getAllPerson();
    }


    /**
     * @author Marco Olivieri
     * Return a list of the all Persons with last name in alphabetical order
     *
     * @return List<PersonEntity>
     */
    public List<PersonEntity> getAllPersonOrder(){
        return database.ticketDao().getAllPersonOrder();
    }

    /**
     * @author Marco Olivieri
     * Return a list of the all Persons with name in alphabetical order
     *
     * @return List<PersonEntity>
     */
    public List<PersonEntity> getAllPersonNameOrder(){
        return database.ticketDao().getAllPersonNameOrder();
    }

    /**
     * Return a list of the tickets associate to a specific Mission
     *
     * @param id identifier of the Mission
     * @return List<TicketEntity>
     */
    public List<TicketEntity> getTicketsForMission(long id){
        return database.ticketDao().getTicketsForMission(id);
    }

    /**
     * @author Marco Olivieri
     * Return a list of the missions done by a specific Person
     *
     * @param id identifier of the Person
     * @return List<MissionEntity>
     */
    public List<MissionEntity> getMissionsForPerson(long id){
        return database.ticketDao().getMissionsForPerson(id);
    }

    /**
     * @author Federico Taschin
     * Return a list of the missions done by a specific Person, ordered by start date
     *
     * @param id identifier of the Person
     * @return List<MissionEntity>
     */
    public List<MissionEntity> getMissionsForPersonOrderByStartDate(long id){
        return database.ticketDao().getMissionsForPersonOrderByStartDate(id);
    }

    /**
     * @author Marco Olivieri
     * Return a specific Ticket from a given ID
     *
     * @param id identifier of the Ticket
     * @return TicketEntity
     */
    public TicketEntity getTicket(long id){
        return database.ticketDao().getTicket(id);
    }

    /**
     * Return a specific Person from a given ID
     *
     * @param id identifier of the Person
     * @return PersonEntity
     */
    public PersonEntity getPerson(long id){
        return database.ticketDao().getPerson(id);
    }

    /**
     * Return a specific Mission from a given ID
     *
     * @param id identifier of the Mission
     * @return MissionEntity
     */
    public MissionEntity getMission(long id){
        return database.ticketDao().getMission(id);
    }

    /**
     * @author Marco Olivieri
     * Gets the total amount of all tickets associate to a specific Mission
     * @param id
     * @return
     */
    public BigDecimal getTotalAmountForMission(long id){
        List<TicketEntity> tickets = getTicketsForMission(id);
        BigDecimal totAmount = BigDecimal.ZERO;
        for(int i=0; i<tickets.size(); i++)
            if(tickets.get(i).getAmount() != null)
                totAmount = totAmount.add(tickets.get(i).getAmount());
        return totAmount;
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

    /**
     * @author Marco Olivieri
     * Gets only active or repaid missions.
     *
     * @param repaid, boolean - true if you want mission repaid, false if you want active mission.
     * @return List<MissionEntity> not null with all active or repaid missions.
     */
    public List<MissionEntity> getMissionRepaid(boolean repaid){
        return database.ticketDao().getMissionRepaid(repaid);
    }


    /**
     * @author Marco Olivieri
     * Gets only active or repaid missions of a specific person.
     * @param repaid, boolean - true if you want mission repaid, false if you want active mission
     * @param personId Long not null, the person's id
     * @return List<MissionEntity> not null all active or repaid missions of the specific person
     */
    public List<MissionEntity> getMissionRepaidForPerson(boolean repaid, long personId){
        return database.ticketDao().getMissionRepaidForPerson(repaid, personId);
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

    /**Created by Federico Taschin
     * All tickets ordered by the date of their insertion into the database
     * @return List<TicketEntity> of all TicketEntity
     */
    public List<TicketEntity> getTicketForMissionOrderedByInsertionDate(int missionId){
        return database.ticketDao().getTicketsForMissionOrderedByDate(missionId);
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
