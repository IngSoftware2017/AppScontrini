package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


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
     * @param id long the ID of the MissionEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.MISSION_TABLE_NAME+" WHERE "+ Constants.MISSION_PRIMARY_KEY_NAME+" = :id")
    long deleteMission(long id);

    /**Deletes a PersonEntity from the database
     * @param id long the ID of the PersonEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.PERSON_TABLE_NAME+" WHERE "+ Constants.PERSON_PRIMARY_KEY_NAME+" = :id")
    long deletePerson(long id);

    /**Deletes a TicketEntity from the database
     * @param id int the ID of the TicketEntity to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.TICKET_TABLE_NAME+" WHERE "+ Constants.TICKET_PRIMARY_KEY_NAME+" = :id")
    long deleteTicket(long id);

    //UPDATE

    /**Updates the given TicketEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param ticketEntity TicketEntity not null, the entity to be updated.
     *               ticketEntity.fileUri not null
     *               ticketEntity.amount not null
     *               ticketEntity.date not null
     * @return the number of updated entities
     */
    @Update
    long updateTicket(TicketEntity ticketEntity);

    /**Updates the given MissionEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param missionEntity MissionEntity not null, the entity to be inserted
     *               missionEntity.personID not null, must be an existing code
     * @return the number of updated entities
     */
    @Update
    long updateMission(MissionEntity missionEntity);

    /**Updates the given PersonEntity matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param personEntity PersonEntity not null, the entity to be inserted
     *               personEntity.name not null
     * @return the number of updated entities
     */
    @Update
    long updatePerson(PersonEntity personEntity);

    //SELECT ALL

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<TicketEntity>
     */
    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME)
    List<TicketEntity> getAllTickets();

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<MissionEntity>
     */
    @Query("SELECT * FROM "+ Constants.MISSION_TABLE_NAME)
    List<MissionEntity> getAllMission();

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<PersonEntity>
     */
    @Query("SELECT * FROM "+ Constants.PERSON_TABLE_NAME)
    List<PersonEntity> getAllPerson();

    //SELECT FROM ID

    /**
    *Gets all the TicketEnt of a MissionEntity
    *@param id long, the id of the MissionEntity
    *@return List<MissionEntity> not null (at least of 0 size) which contains all the tickets for the given mission id 
    */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME+" WHERE "+Constants.MISSION_CHILD_COLUMNS+" = :id")
    public List<TicketEntity> getTicketsForMission(long id);

    /**
    *Executes a SELECT query for a specified TicketEntity id
    *@param id long, the id of the TicketEntity
    *@return List<TicketEntity> not null (at least of 0 size) which contains all the tickets with the given id
    */
    @Query("SELECT * FROM "+Constants.TICKET_TABLE_NAME +" WHERE "+Constants.TICKET_PRIMARY_KEY_NAME+" =:id")
    public TicketEntity getTicket(long id);

    /**
     * Executes a SELECT of a specific person from id
     *
     * @param id long, identifier of the person
     * @return PersonEntity
     */
    @Query("SELECT * FROM " + Constants.PERSON_TABLE_NAME + " WHERE " + Constants.PERSON_PRIMARY_KEY_NAME + " =:id")
    PersonEntity getPerson(long id);
}
