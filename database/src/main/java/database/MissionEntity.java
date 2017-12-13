package database;

import java.util.Date;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;

/**
 * Represents one mission and its associated information
 * @author Marco Olivieri(Team 3)
 */

@Entity(tableName = Constants.MISSION_TABLE_NAME,
        foreignKeys = @ForeignKey(entity = PersonEntity.class, parentColumns = Constants.PERSON_PRIMARY_KEY_NAME, childColumns = Constants.PERSON_CHILD_COLUMNS))
@TypeConverters(Converters.class) // automatic converters for database correct type

public class MissionEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.MISSION_PRIMARY_KEY_NAME)
    private int ID;
    private Date startMission;
    private Date endMission;
    private String location;
    private boolean isRepay;
    private Uri excel;
    private String name;

    @ColumnInfo(name = Constants.PERSON_CHILD_COLUMNS)
    private int personID;

    @Ignore
    /**
     * Non parametric constructor
     */
    public MissionEntity() {
    }

    /**
     * Parametric constructor
     *
     * @param startMission Date of the beginning of the mission
     * @param endMission Date of the end of the mission
     * @param location Name of location where the mission took place
     * @param personID code of the person of this mission
     */
    public MissionEntity(String name, Date startMission, Date endMission, String location, int personID) {
        this.startMission = startMission;
        this.endMission = endMission;
        this.location = location;
        isRepay = false;
        excel = null;
        this.personID = personID;
        this.name = name;
    }

    /**
     * Returns the mission ID
     * @return  ID
     */
    public int getID() {
        return ID;
    }

    @Deprecated
    /** This method should no longer exists, since the ID is auto-generated by the database
     * Set mission id
     * @param ID not null
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Returns the beginning date of the mission
     * @return startMission
     */
    public Date getStartMission() {
        return startMission;
    }

    /**
     * Sets the beginning date of the mission
     * @param startMission
     */
    public void setStartMission(Date startMission) {
        this.startMission = startMission;
    }

    /**
     * Returns the end date of the mission
     * @return endMission
     */
    public Date getEndMission() {
        return endMission;
    }

    /**
     * Sets the end date of the mission
     * @param endMission
     */
    public void setEndMission(Date endMission) {
        this.endMission = endMission;
    }

    /**
     * Returns the location where the mission took place
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location where the mission took place
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns if this mission is repay or not
     * @return
     */
    public boolean isRepay() {
        return isRepay;
    }

    /**
     * Sets the boolean value if this mission is repay or not
     * @param isRepay
     */
    public void setRepay(boolean isRepay) {
        this.isRepay = isRepay;
    }

    /**
     * Returns path of the file excel format
     * @return excel
     */
    public Uri getExcel() {
        return excel;
    }

    /**
     * Sets path of the file excel format
     * @param excel
     */
    public void setExcel(Uri excel) {
        this.excel = excel;
    }

    /**
     * Returns person id of this mission
     * @return personID
     */
    public int getPersonID() {
        return personID;
    }

    /**
     * Sets person id of this mission
     * @param personID
     */
    public void setPersonID(int personID) {
        this.personID = personID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

