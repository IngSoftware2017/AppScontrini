package database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.ColumnInfo;
import android.net.Uri;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Represents the ticket and its associated information
 * @author Marco Olivieri (Team 3)
 */

@Entity(tableName = Constants.TICKET_TABLE_NAME,
        foreignKeys = @ForeignKey(entity = MissionEntity.class,
                parentColumns = Constants.MISSION_PRIMARY_KEY,
                childColumns = Constants.MISSION_CHILD_COLUMNS))
@TypeConverters(Converters.class) // automatic converters for database correct type

public class TicketEntity {

    @ColumnInfo(name = Constants.TICKET_PRIMARY_KEY)
    @PrimaryKey(autoGenerate = true)
    private long ID;
    private Uri fileUri;
    private BigDecimal amount;
    private String shop;
    private Date date;
    private String title;
    private List<String> category;
    private float[] corners;
    private Date insertionDate;

    @ColumnInfo(name = Constants.MISSION_CHILD_COLUMNS)
    private int missionID;

    @Ignore
    /**
     * Non parametric constructor to use when you don't want set all fields
     */
    public TicketEntity() {
    }

    /**
     * Parametric constructor
     *
     * @param fileUri Path associated with the the ticket file stored in the memory
     * @param amount total amount
     * @param shop Name of the shop in which the ticket was issued
     * @param date the issue date of the ticket
     * @param title name given
     * @param missionID code of the mission
     */
    public TicketEntity(Uri fileUri, BigDecimal amount, String shop, Date date, String title, int missionID) {
        this.amount = amount;
        this.date = date;
        this.fileUri = fileUri;
        this.shop = shop;
        this.title = title;
        this.missionID = missionID;
        corners = new float[8];
    }

    /**
     * Returns the ticket ID
     * @return  ID
     */
    public long getID() {
        return ID; }

    @Deprecated
    /** This method should no longer exists, since the ID is auto-generated by the database
     * Set ticket id
     * @param ID not null
     */
    public void setID(long ID) { this.ID = ID; }

    /**
     * Returns the date of the issue of the ticket
     *
     * @return date
     */
    public  Date getDate() { return date; }

    /**
     * Sets ticket date
     * @param date not null
     */
    public void setDate(Date date) { this.date = date; }

    /**
     * Returns ticket amount
     * @return amount
     */
    public  BigDecimal getAmount() { return amount; }

    /**
     * Sets ticket amount
     * @param amount not null
     */
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    /**
     * Returns ticket file path
     * @return fileUri
     */
    public Uri getFileUri() { return fileUri; }

    /**
     * Sets ticket file path
     * @param fileUri not null
     */
    public void setFileUri(Uri fileUri) { this.fileUri = fileUri; }

    /**
     * Returns the shop
     * @return shop
     */
    public String getShop() { return shop; }

    /**
     * Sets ticket shop
     * @param shop not null
     */
    public void setShop (String shop) { this.shop = shop; }

    /**
     * Returns the title of the ticket
     *
     * @return title
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of the ticket
     *
     * @param title not null
     */
    public void setTitle (String title) { this.title = title; }

    /**
     * Returns the list of categories of the ticket
     *
     * @return category
     */
    public List<String> getCategory() { return category; }

    /**
     * Sets the list of categories of the ticket
     * @param categories
     */
    public void setCategory(List<String> categories) {
        this.category = categories;
    }

    /**
     * Returns the mission id of this ticket
     * @return missionID
     */
    public int getMissionID() {
        return missionID;
    }

    /**
     * Sets mission id of this TicketEntity
     * @param missionID
     */
    public void setMissionID(int missionID) {
        this.missionID = missionID;
    }


    /** Created by Marco Olivieri
     * Returns the corners of the ticket
     * 8 float point of the rectangle coordinate
     * @return corners
     */
    public float[] getCorners() {
        return corners;
    }

    /** Created by Marco Olivieri
     * Sets corners of the ticket
     * Must be an array of 8 elements: the rectangle coordinate of the ticket
     * @param corners
     */
    public void setCorners(float[] corners) {
        this.corners = corners;
    }

    /**Created by Federico Taschin
     * @return insertion date (not null) of the ticket
     */
    public Date getInsertionDate() {
        return insertionDate;
    }

    /**Created by Federico Taschin
     *
     * @param insertionDate
     */
    public void setInsertionDate(Date insertionDate) {
        this.insertionDate = insertionDate;
    }

    /**
     * Returns a String with TicketEntity data formatted as follows:
     * "Shop"
     * Totale: "Amount"
     *
     * @return string
     */
    @Override
    public String toString(){
        return getShop()+"\nTotale: "+getAmount();
    }

}