package com.ing.software.ticketapp.common;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;

/**
 * Structure containing information about a single ticket.
 */
public class Ticket {

    /**
     * Database identifier.
     */
    public int ID;

    /**
     * URI pointing to the ticket photo (local or remote).
     */
    public URI fileURI;

    /**
     * Ticket title
     */
    public String title;

    /**
     * Purchase date.
     */
    public Date date;

    /**
     * Purchase amount.
     */
    public BigDecimal amount;
}
