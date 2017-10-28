package ing_sw.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Struttura contenente i dati relativi ad un singolo scontrino.
 * @author Riccardo Zaglia (Gruppo 2)
 */
public class Ticket implements Serializable {

    /**
     * Identificatore.
     */
    public int ID;

    /**
     * URI associato alla foto dello scontrino.
     */
    public URI fileURI;

    /**
     * Titolo dello scontrino.
     */
    public String title;

    /**
     * Data dello scontrino.
     */
    public Date date;

    /**
     * Importo dello scontrino.
     */
    public BigDecimal amount;
}
