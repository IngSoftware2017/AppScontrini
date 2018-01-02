package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;


/**
 * Created by Federico Taschin on 08/11/2017.
 * Modified by Marco Olivieri on 28/11/2017
 */

/*Data Access Objects Interface. Defines the queries using Room libraries
  These methods won't be implemented in any class, since the Room library takes care of doing the SQL operatoins by taking the  
  methods parameters and executing the SQL operation defined in the correspondent '@' annotation.
  For instance, when the method addTicket(TicketEntity ticketEntity) is called, the Room library takes the ticketEntity object
  in the parameters and executes the correspondent SQL INSERT query. The use of these methods can be seen in the DataManager class.
*/
@Dao
public interface DAO {

    //INSERT

    /** Executes the insert query.
     * @param ticketEntity TicketEntity not null, the entity to be inserted
     *               ticketEntity.fileUri not null, must be a valid file path
     *               ticketEntity.amount not null
     *               ticketEntity.date not null
     *               ticketEntity.missionID not null, must be an existing code
     * @return the id of the inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addTicket(TicketEntity ticketEntity);

    /** Executes the insert query.
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *                missionEntity.personID not null, must be an existing code
     * @return the id of inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addMission(MissionEntity missionEntity);

    /** Executes the insert query.
     * @param personEntity PersonEntity not null, the entity to be inserted
     *               personEntity.name not null
     * @return the id of inserted entity.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addPerson(PersonEntity personEntity);

    //DELETE

    /**Deletes a MissionEntity from the database
     * @param id int the ID of the MissionEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.MISSION_TABLE_NAME+" WHERE "+ Constants.MISSION_PRIMARY_KEY +" = :id")
    int deleteMission(int id);

    /**Deletes a PersonEntity from the database
     * @param id int the ID of the PersonEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.PERSON_TABLE_NAME+" WHERE "+ Constants.PERSON_PRIMARY_KEY +" = :id")
    int deletePerson(int id);

    /**Deletes a TicketEntity from the database
     * @param id int the ID of the TicketEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.TICKET_TABLE_NAME+" WHERE "+ Constants.TICKET_PRIMARY_KEY +" = :id")
    int deleteTicket(int id);

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

    /**Updates the given MissionEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *               missionEntity.personID not null, must be an existing code
     * @return the number of updated entities
     */
    @Update
    int updateMission(MissionEntity missionEntity);

    /**Updates the given PersonEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param personEntity PersonEntity not null, the entity to be inserted
     *               personEntity.name not null
     * @return the number of updated entities
     */
    @Update
    int updatePerson(PersonEntity personEntity);

    //SELECT ALL

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<TicketEntity> not null, list of all TicketEntity in the database
     */
    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME)
    List<TicketEntity> getAllTickets();

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<MissionEntity> not null, list of all MissionEntity in the database
     */
    @Query("SELECT * FROM "+ Constants.MISSION_TABLE_NAME)
    List<MissionEntity> getAllMission();

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<PersonEntity> not null list of all PersonEntity in the database
     */
    @Query("SELECT * FROM "+ Constants.PERSON_TABLE_NAME)
    List<PersonEntity> getAllPerson();

    /**
    *Gets all the TicketEntity of a MissionEntity
    *@param id int, the id of the MissionEntity
    *@return List<MissionEntity> not null (at least of 0 size) which contains all the tickets for the given mission id 
    */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME+" WHERE "+Constants.MISSION_CHILD_COLUMNS+" = :id")
     List<TicketEntity> getTicketsForMission(int id);

    /**
    *Executes a SELECT query for a given TicketEntity id
    *@param id int, the id of the TicketEntity
    *@return List<TicketEntity> not null (at least of 0 size) which contains all the tickets with the given id
    */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME +" WHERE "+Constants.TICKET_PRIMARY_KEY +" =:id")
     TicketEntity getTicket(int id);

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
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME+" WHERE "+Constants.TICKET_FIELD_CATEGORY +" LIKE '%:category%' ")
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
}
