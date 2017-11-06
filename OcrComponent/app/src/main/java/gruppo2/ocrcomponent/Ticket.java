package gruppo2.ocrcomponent;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

/**
 * Struttura contenente i dati relativi ad una singola foto/documento.
 */
public class Ticket {

    /**
     * Identificatore.
     * NB: non modificare al di fuori della classe che implementa IDatabase
     */
    public int ID;

    /**
     * URI associato al file del documento (puo' essere un percorso locale o remoto).
     * L'oggetto URI puo' essere convertito in stringa con il metodo toString.
     */
    public URI fileURI;

    /**
     * Titolo del documento
     */
    public String title;

    /**
     * Data del documento.
     */
    public Date date;

    /**
     * Importo del documento.
     */
    public BigDecimal amount;
}
