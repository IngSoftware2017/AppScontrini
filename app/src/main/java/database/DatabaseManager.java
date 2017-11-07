package database;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DatabaseManager {

    /* Aggiunge un nuovo scontrino al database. Uno scontrino è definito dall'oggetto Ticket
     * @param ticket Ticket not null, ticket.getFileUri not null
     * @return id del record creato nel database, -1 se creazione fallita
     */
    public int addTicket(Ticket ticket){
        return -1;
    }

    /*Modifica i dati di uno scontrino già presente nel database.
    * @param ticket Ticket not null, ticket.getFileUri deve essere un percorso valido ad una foto
    *        corrisponde al record nel database di cui si vogliono modificare i valori. Viene modificato il record avente
    *        ID uguale a ticket.ID con i valori contenuti nell'oggetto Ticket. I valori che non devono essere modificati vanno lasciati nulli.
    * @return true se update andato a buon fine, false altrimenti
    */
    public boolean updateTicket(Ticket ticket){
         return false;
    }

    /*
     * @return List<Ticket> not null, contiene tutti gli oggetti Ticket presenti nel database
     */
    public List<Ticket> getAllTickets(){
        return new ArrayList<Ticket>();
    }


}
