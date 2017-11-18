package database;

import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents the ticket and its associated information
 * @author Marco Olivieri (Team 3)
 */

public class Ticket {

    @PrimaryKey(autoGenerate = true)
    protected int ID;
    private Uri fileUri;
    private BigDecimal amount;
    private String shop;
    private Date date;
    private String title;

    /**
     * Non parametric constructor
     */
    public Ticket() {
    }

    /**
     * Parametric constructor
     *
     * @param id Unique ID of the ticket
     * @param fileUri Path associated with the the ticket file stored in the memory
     * @param amount total amount
     * @param shop Name of the shop in which the ticket was issued
     * @param date the issue date of the ticket
     * @param title name given
     */
    public Ticket(int id, Uri fileUri, BigDecimal amount, String shop, Date date, String title) {
        this.ID = id;
        this.amount = amount;
        this.date = date;
        this.fileUri = fileUri;
        this.shop = shop;
        this.title = title;
    }

    /**
     * Returns the ticket ID
     * @return  ID
     */
    public int getID() { return ID; }

    @Deprecated
    /** This method should no longer exists, since the ID is auto-generated by the database
     * Set ticket id
     * @param ID not null
     */
    public void setID(int ID) { this.ID = ID; }

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
     *
     * @param shop not null
     */
    public void setShop (String shop) { this.shop = shop; }

    /**
     * Restituisce il titolo del Ticket
     *
     * @return title
     */
    public String getTitle() { return title; }

    /**
     * Imposta il titolo del Ticket
     *
     * @param title not null
     */
    public void setTitle (String title) { this.title = title; }

    /**
     * Returns a String with Ticket data formatted as follows:
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