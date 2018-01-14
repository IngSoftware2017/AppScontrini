package com.ing.software.ocr;

import android.graphics.PointF;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Structure containing information about a single ticket.
 */
public class OcrTicket {

    /**
     * Purchase date.
     */
    public Date date;

    /**
     * Total amount.
     */
    public BigDecimal amount;

    /**
     * Ordered list of vertices of rectangle (first top-left, counter-clockwise)
     */
    public List<PointF> rectangle;

    /**
     * List of errors related to the creation or manipulation of the Ticket
     */
    public List<OcrError> errors = new ArrayList<>();
}