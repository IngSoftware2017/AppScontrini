package database;

import android.net.Uri;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Classe Ticket rappresenta l'oggetto scontrino con le informazioni ad esso associate
 *
 * @author Marco Olivieri (Gruppo 3)
 */

public class Ticket {

    int ID;     //id univoco del ticket
    Uri fileUri;    //percorso di salvataggio associato
    BigDecimal amount;  //importo totale
    String shop;    //nome del negozio
    Date date;  //data del ticket
    String title;   //titolo dato al ticket

    public Ticket() {
    }

    public Ticket(int id, Uri fileUri, BigDecimal amount, String shop, Date date, String title) {
        this.ID = id;
        this.amount = amount;
        this.date = date;
        this.fileUri = fileUri;
        this.shop = shop;
        this.title = title;
    }

    /**
     * Restituisce ID del Ticket
     *
     * @return  ID
     */
    public int getID() { return ID; }

    /**
     * Imposta ID del Ticket
     *
     * @param ID
     */
    public void setID(int ID) { this.ID = ID; }

    /**
     * Restituisce la data del Ticket
     *
     * @return date
     */
    public  Date getDate() { return date; }

    /**
     * Imposta la data del Ticket
     *
     * @param date
     */
    public void setDate(Date date) { this.date = date; }

    /**
     * Restituisce l'importo totale del Ticket
     *
     * @return amount
     */
    public  BigDecimal getAmount() { return amount; }

    /**
     * Imposta l'importo totale del Ticket
     *
     * @param amount
     */
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    /**
     * Restituisce il percorso di salvataggio del Ticket
     *
     * @return fileUri
     */
    public Uri getFileUri() { return fileUri; }

    /**
     * Imposta il percorso di salvataggio del Ticket
     *
     * @param fileUri
     */
    public void setFileUri(Uri fileUri) { this.fileUri = fileUri; }

    /**
     * Restituisce il negozio del Ticket
     *
     * @return shop
     */
    public String getShop() { return shop; }

    /**
     * Imposta il negozio del Ticket
     *
     * @param shop
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
     * @param title
     */
    public void setTitle (String title) { this.title = title; }

    /**
     * Trasforma in stringa l'oggetto Ticket nel formato
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