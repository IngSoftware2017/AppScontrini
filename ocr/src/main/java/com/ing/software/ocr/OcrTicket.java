package com.ing.software.ocr;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Pair;

import java.math.BigDecimal;
import java.util.*;

/**
 * Structure containing information about a single ticket.
 */
public class OcrTicket {

    /**
     * Ordered list of vertices of ticket rectangle (first top-left, counter-clockwise)
     */
    public List<PointF> rectangle;

    /**
     * Total amount.
     */
    public BigDecimal amount;

    /**
     * Rectangle of total amount in the normalized space [0, 1]^2 of the undistorted bitmap
     * To get the rectangle in the undistorted bitmap, multiply by (width, height) of
     * the undistorted bitmap
     */
    public RectF amountRect;

    /**
     * Currency of total amount.
     */
    public Currency currency;

    /**
     * List of products (label + amount)
     */
    public List<Pair<String, BigDecimal>> products;

    /**
     * Indoor amount ("coperto")
     */
    public BigDecimal indoorAmount;

    /**
     * Purchase date.
     */
    public Date date;

    /**
     * List of errors related to the creation or manipulation of the Ticket
     */
    public List<OcrError> errors = new ArrayList<>();
}