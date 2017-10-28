package ing_sw.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Struttura contenente i dati relativi ad una singola foto/documento.
 * @author Riccardo Zaglia (Gruppo 2)
 */
public class Ticket implements Serializable {

    /**
     * Identificatore.
     * NB: non modificare al di fuori della classe che implementa IDatabase
     */
    public int ID;

    /**
     * URI associato al file del documento (puo' essere un percorso locale o remoto).
     * L'oggetto URI puo' essere convertito in stringa con il metodo toString.
     */
    public URI getFileURI;

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

    /**
     * Lista di oggetti contenete dati relativi a funzionalita' aggiuntive non compatibili tra gruppi.
     * E' possibile accedere ai dati con il casting.
     */
    public List<Object> opaqueData;
}
