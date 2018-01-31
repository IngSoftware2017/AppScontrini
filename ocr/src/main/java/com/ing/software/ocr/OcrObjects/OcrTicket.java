package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;
import android.util.Pair;

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
     * Restored amount contains a possibly different amount than 'amount' cause it's decoded
     * trying to get a valid amount from an invalid string.
     * May contain also the result from scheme analysis.
     */
    public BigDecimal restoredAmount;

    /**
     * List of products (label + amount)
     */
    public List<Pair<String, BigDecimal>> products;

    /**
     * Indoor amount ("coperto")
     */
    public BigDecimal indoorAmount;

    /**
     * Ordered list of vertices of rectangle (first top-left, counter-clockwise)
     */
    public List<PointF> rectangle;

    /**
     * List of errors related to the creation or manipulation of the Ticket
     */
    public List<OcrError> errors = new ArrayList<>();
}