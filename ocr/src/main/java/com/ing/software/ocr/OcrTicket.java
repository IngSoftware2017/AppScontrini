package com.ing.software.ocr;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Pair;
import android.util.SizeF;

import java.math.BigDecimal;
import java.util.*;

/**
 * Structure containing information about a single ticket.
 */
public class OcrTicket implements Cloneable {

    /**
     * Total amount.
     */
    public BigDecimal total;

    /**
     * Currency of total.
     */
    public Currency currency;

    /**
     * Normalized, margin-less coordinates of total. Use {@link ImageProcessor#expandRectCoordinates}
     * to get the coordinates to be used to mark the total in the UI.
     */
    public RectF totalRect;

    /**
     * Language of the ticket
     */
    public Locale locale;

    /**
     * Purchase date.
     */
    public Date date;

    /**
     * List of products (label + amount)
     */
    public List<Pair<String, BigDecimal>> products;

    /**
     * Flag set if ticket contains cover
     */
    public boolean containsCover;

    /**
     * Ordered list of vertices of ticket rectangle (first top-left, counter-clockwise)
     */
    public List<PointF> rectangle;

    /**
     * List of errors related to the creation or manipulation of the Ticket
     */
    public Set<OcrError> errors = new HashSet<>();
}