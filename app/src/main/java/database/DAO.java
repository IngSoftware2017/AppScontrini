package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


/**
 * Created by Federico Taschin on 08/11/2017.
 * Modified by Marco Olivieri on 28/11/2017
 */

//Data Access Objects Interface. Defines the queries using Room libraries

@Dao
public interface DAO {

    //INSERT

    /** Executes the insert query.
     * @param ticket Ticket not null, the entity to be inserted
     *               ticket.fileUri not null, must be a valid file path
     *               ticket.amount not null
     *               ticket.date not null
     *               ticket.missionID not null, must be an existing code
     * @return the number of inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addTicket(Ticket ticket);

    /** Executes the insert query.
     * @param mission Mission not null, the entity to be inserted
     *                mission.personID not null, must be an existing code
     * @return the number of inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addMission(Mission mission);

    /** Executes the insert query.
     * @param person Person not null, the entity to be inserted
     *               person.name not null
     * @return the number of inserted entities.
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long addPerson(Person person);

    //DELETE

    /**Deletes a Mission from the database
     * @param id int the ID of the Mission to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.MISSION_TABLE_NAME+" WHERE "+ Constants.MISSION_PRIMARY_KEY_NAME+" = :id")
    int deleteMission(int id);

    /**Deletes a Person from the database
     * @param id int the ID of the Person to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.PERSON_TABLE_NAME+" WHERE "+ Constants.PERSON_PRIMARY_KEY_NAME+" = :id")
    int deletePerson(int id);

    /**Deletes a Ticket from the database
     * @param id int the ID of the Ticket to be deleted
     * @return the number of deleted entities.
     */
    @Query("DELETE FROM "+ Constants.TICKET_TABLE_NAME+" WHERE "+ Constants.TICKET_PRIMARY_KEY_NAME+" = :id")
    int deleteTicket(int id);

    //UPDATE

    /**Updates the given Ticket matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param ticket Ticket not null, the entity to be updated.
     *               ticket.fileUri not null
     *               ticket.amount not null
     *               ticket.date not null
     * @return the number of updated entities
     */
    @Update
    int updateTicket(Ticket ticket);

    /**Updates the given Mission matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param mission Mission not null, the entity to be inserted
     *               mission.personID not null, must be an existing code
     * @return the number of updated entities
     */
    @Update
    int updateMission(Mission mission);

    /**Updates the given Person matching its ID. All fields (except ID) with values other than those in the database will be updated
     * @param person Person not null, the entity to be inserted
     *               person.name not null
     * @return the number of updated entities
     */
    @Update
    int updatePerson(Person person);

    //SELECT ALL

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<Ticket>
     */
    @Query("SELECT * FROM "+ Constants.TICKET_TABLE_NAME)
    List<Ticket> getAllTickets();

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<Mission>
     */
    @Query("SELECT * FROM "+ Constants.MISSION_TABLE_NAME)
    List<Mission> getAllMission();

    /**
     * Executes a SELECT of all the entities in the database
     * @return List<Person>
     */
    @Query("SELECT * FROM "+ Constants.PERSON_TABLE_NAME)
    List<Person> getAllPerson();

}
