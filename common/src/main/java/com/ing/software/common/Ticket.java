package com.ing.software.common;

import android.graphics.Bitmap;
import android.net.Uri;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public Uri fileURI;

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

    /**
     * List of errors related to the creation or manipulation of the Ticket
     */
    public List<TicketError> errors = new ArrayList<>();

    public int missionId;

    public Bitmap bitmap;
}