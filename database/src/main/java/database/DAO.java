package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;
import android.content.Context;
import android.provider.SyncStateContract;
import android.widget.ListView;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionService;


/**
 * Created by Federico Taschin on 08/11/2017.
 * Modified by Marco Olivieri
 * Modify by Matteo Mascotto: improve getPerson from id and complete methods documentation
 */

/*Data Access Objects Interface. Defines the queries using Room libraries
  These methods won't be implemented in any class, since the Room library takes care of doing the SQL operatoins by taking the  
  methods parameters and executing the SQL operation defined in the correspondent '@' annotation.
  For instance, when the method addTicket(TicketEntity ticketEntity) is called, the Room library takes the ticketEntity object
  in the parameters and executes the correspondent SQL INSERT query. The use of these methods can be seen in the DataManager class.
*/
@Dao
@TypeConverters(Converters.class)
public interface DAO {

    //INSERT

    /**
     * @author Federico Taschin
     * Executes the insert query.
     * @param ticketEntity TicketEntity not null, the entity to be inserted
     *               ticketEntity.fileUri not null, must be a valid file path
     *               ticketEntity.amount not null
     *               ticketEntity.date not null
     *               ticketEntity.missionID not null, must be an existing code
     * @return the id of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addTicket(TicketEntity ticketEntity);

    /**
     * @author Federico Taschin
     * Executes the insert query.
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *                missionEntity.personID not null, must be an existing code
     * @return the id of inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addMission(MissionEntity missionEntity);

    /**
     * Executes the insert query.
     * @param personEntity PersonEntity not null, the entity to be inserted
     *               personEntity.name not null
     * @return the id of inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addPerson(PersonEntity personEntity);

    //DELETE

    /**
     * @author Marco Olivieri
     * Deletes a MissionEntity from the database
     * @param id long the ID of the MissionEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.MISSION_TABLE_NAME+" WHERE "+ Constants.MISSION_PRIMARY_KEY+" = :id")
    int deleteMission(long id);

    /**
     * @author Marco Olivieri
     * Deletes a PersonEntity from the database
     * @param id long the ID of the PersonEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.PERSON_TABLE_NAME+" WHERE "+ Constants.PERSON_PRIMARY_KEY+" = :id")
    int deletePerson(long id);

    /**Deletes a TicketEntity from the database
     * @param id int the ID of the TicketEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.TICKET_TABLE_NAME+" WHERE "+ Constants.TICKET_PRIMARY_KEY+" = :id")
    int deleteTicket(long id);

    //UPDATE

    /**Updates the given TicketEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param ticketEntity TicketEntity not null, the entity to be updated.
     *               ticketEntity.fileUri not null
     *               ticketEntity.amount not null
     *               ticketEntity.date not null
     * @return the number of updated entities
     */
    @Update
    int updateTicket(TicketEntity ticketEntity);

    /**
     * @author Marco Olivieri
     * Updates the given MissionEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *               missionEntity.personID not null, must be an existing code
     * @return the number of updated entities
     */
    @Update
    int updateMission(MissionEntity missionEntity);

    /**
     * @author Marco Olivieri
     * Updates the given PersonEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param personEntity PersonEntity not null, the entity to be inserted
     *               personEntity.name not null
     * @return the number of updated entities
     */
    @Update
    int updatePerson(PersonEntity personEntity);

    //SELECT ALL

    /**
     * @author Federico Taschin
     * Executes a SELECT of all the TicketEntity in the database
     * @return List<TicketEntity>
     */
    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME)
    List<TicketEntity> getAllTickets();

    /**
     * @author Federico Taschin
     * Executes a SELECT of all the MissionEntity in the database
     * @return List<MissionEntity>
     */
    @Query("SELECT * FROM "+ Constants.MISSION_TABLE_NAME)
    List<MissionEntity> getAllMission();

    /**
     * @author Federico Taschin
     * Executes a SELECT of all the PersonEntity in the database
     * @return List<PersonEntity>
     */
    @Query("SELECT * FROM "+ Constants.PERSON_TABLE_NAME)
    List<PersonEntity> getAllPerson();


    /**
     * @author Marco Olivieri
     * Executes a SELECT of all the PersonEntity with last name in the database in alphabetical order
     * @return List<PersonEntity>
     */
    @Query("SELECT * FROM "+ Constants.PERSON_TABLE_NAME + " ORDER BY " + Constants.PERSON_FIELD_LAST_NAME + " ASC")
    List<PersonEntity> getAllPersonOrder();

    /**
     * @author Marco Olivieri
     * Executes a SELECT of all the PersonEntity with name in the database in alphabetical order
     * @return List<PersonEntity>
     */
    @Query("SELECT * FROM "+ Constants.PERSON_TABLE_NAME + " ORDER BY " + Constants.PERSON_FIELD_NAME + " ASC")
    List<PersonEntity> getAllPersonNameOrder();

    //SELECT FROM ID

    /**
    *Gets all the TicketEnt of a MissionEntity
    *@param id long, the id of the MissionEntity
    *@return List<TicketEntity> not null (at least of 0 size) which contains all the tickets for the given mission id
    */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME+" WHERE "+Constants.MISSION_CHILD_COLUMNS+" = :id")
    List<TicketEntity> getTicketsForMission(long id);

    /**
     * @author Marco Olivieri
     * Gets all the Missions done by a specific Person
     * @param id long, the id of the Person
     * @return List<MissionEntity> not null (at least of 0 size) which contains all the missions for the given person id
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.PERSON_CHILD_COLUMNS+" = :id")
    List<MissionEntity> getMissionsForPerson(long id);

    /**
     * @author Federico Taschin
     * Return a list of the missions done by a specific Person, ordered by start date
     *
     * @param id identifier of the Person
     * @return List<MissionEntity>
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.PERSON_CHILD_COLUMNS+" = :id ORDER BY "+Constants.MISSION_FIELD_START_DATE)
    List<MissionEntity> getMissionsForPersonOrderByStartDate(long id);
    /**
    *Executes a SELECT query for a specified TicketEntity id
    *@param id long, the id of the TicketEntity
    *@return List<TicketEntity> not null (at least of 0 size) which contains all the tickets with the given id
    */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME +" WHERE "+Constants.TICKET_PRIMARY_KEY+" =:id")
    TicketEntity getTicket(long id);

    /**
     * @author Marco Olivieri
     * Executes a SELECT of a specific mission from id
     *
     * @param id long, identifier of the mission
     * @return MissionEntity
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME +" WHERE "+Constants.MISSION_PRIMARY_KEY+" =:id")
    public MissionEntity getMission(long id);

    /**
     * Executes a SELECT of a specific person from id
     *
     * @param id long, identifier of the person
     * @return PersonEntity
     */
    @Query("SELECT * FROM " + Constants.PERSON_TABLE_NAME + " WHERE " + Constants.PERSON_PRIMARY_KEY + " =:id")
    PersonEntity getPerson(long id);

    /**Created by Federico Taschin
     * Gets all the TicketEntity with the given date
     * @param date Date not null, the date of the ticket
     * @return List<TicketEntity> not null with all TicketEntity with the given date
     */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME +" WHERE "+Constants.TICKET_FIELD_DATE + "=:date")
    List<TicketEntity> getTicketWithDate(Date date);

    /**Created by Federico Taschin
     * Gets all TicketEntity with the given category
     * @param category String not null, category to be searched
     * @return List<TicketEntity> not null with all TicketEntity with the given category
     */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME+" WHERE "+Constants.TICKET_FIELD_CATEGORY +" LIKE :category ")
    List<TicketEntity> getTicketWithCategory(String category);

    /**Created by Federico Taschin
     * Gets all the MissionEntity with the given start date
     * @param startDate not null, the start date of the mission to be searched
     * @return List<MissionEntity> not null with all MissionEntity with the given start date
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.MISSION_FIELD_START_DATE +" =:startDate")
    List<MissionEntity> getMissionWithStartDate(Date startDate);

    /**Created by Federico Taschin
     * Gets all the MissionEntity with the given end date
     * @param endDate not null, the end date of the mission to be searched
     * @return List<MissionEntity> not null with all MissionEntity with the given end date
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.MISSION_FIELD_END_DATE +" =:endDate")
    List<MissionEntity> getMissionWithEndDate(Date endDate);

    /**Created by Federico Taschin
     * Gets all the MissionEntity with the given location
     * @param location not null, the location of the mission to be searched
     * @return List<MissionEntity> not null with all MissionEntity with the given location
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.MISSION_FIELD_LOCATION +" =:location")
    List<MissionEntity> getMissionWithLocation(String location);

    /**
     * @author Marco Olivieri
     * Gets only active or repaid missions.
     * @param repaid, boolean - true if you want mission repaid, false if you want active mission
     * @return List<MissionEntity> not null with all active or repaid missions.
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.MISSION_FIELD_REPAID +" = :repaid")
    List<MissionEntity> getMissionRepaid(boolean repaid);

    /**
     * @author Marco Olivieri
     * Gets only active or repaid missions of a specific person.
     * @param repaid, boolean - true if you want mission repaid, false if you want active mission
     * @param personId Long not null, the person's id
     * @return List<MissionEntity> not null all active or repaid missions of the specific person
     */
    @Query("SELECT * FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.MISSION_FIELD_REPAID +" = :repaid AND "
            + Constants.PERSON_CHILD_COLUMNS + " =:personId" + " ORDER BY "+Constants.MISSION_FIELD_NAME+" ASC")
    List<MissionEntity> getMissionRepaidForPerson(boolean repaid, long personId);

    /**Created by Federico Taschin
     * Gets all the PersonEntity with the given name
     * @param name String not null
     * @return List<PersonEntity> not null, with all PersonEntity with the given name
     */
    @Query("SELECT * FROM "+Constants.PERSON_TABLE_NAME+" WHERE "+Constants.PERSON_FIELD_NAME+" =:name")
    List<PersonEntity> getPersonWithName(String name);

    /**Created by Federico Taschin
     * Gets all the PersonEntity with the given last name
     * @param lastName String not null
     * @return List<PersonEntity> not null, with all PersonEntity with the given last name
     */
    @Query("SELECT * FROM "+Constants.PERSON_TABLE_NAME+" WHERE "+Constants.PERSON_FIELD_LAST_NAME+" =:lastName")
    List<PersonEntity> getPersonWithLastName(String lastName);

    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME+" WHERE " + Constants.MISSION_CHILD_COLUMNS +"= :missionId ORDER BY "+Constants.TICKET_INSERTION_DATE+" DESC")
    List<TicketEntity> getTicketsForMissionOrderedByDate(int missionId);

    /**Created by Stefano Elardo
     * Gets the number of active missions for the given person
     * @param personID long, identifier of the person
     * @return the amount of active missions
     */
    @Query("SELECT COUNT(*) FROM "+Constants.MISSION_TABLE_NAME+" WHERE "+Constants.PERSON_CHILD_COLUMNS+"= :personID AND "+Constants.MISSION_FIELD_REPAID+"=0")
    int getActiveMissionsNumberForPerson(long personID);
}
