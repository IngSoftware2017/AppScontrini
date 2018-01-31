package com.ing.software.ocr.OcrObjects;

import android.support.annotation.IntRange;

/**
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on precision)
 * NOTE: As of now (31-1) only precisions 0-3 are implemented
 *
 * For precision:
 * 0 = scan image at 1/3 of its dimension, don't reanalyze specific parts of image to get better results
 * 1 = scan image at 1/2 of its dimension, don't reanalyze specific parts of image to get better results
 * 2 = scan image at original dimension (passed by imageprocessor), don't reanalyze specific parts of image to get better results
 * 3 = scan image at original dimension (passed by imageprocessor), reanalyze total strip to get a better result (only first element)
 * 4 = scan image at original dimension (passed by imageprocessor), reanalyze total (only first element) and prices strips to get a better result
 * 5 = scan image at original dimension (passed by imageprocessor), reanalyze total (first 3 elements) and prices strips to get a better result
 * 6 = scan image at original dimension (passed by imageprocessor), reanalyze total (first 3 elements) and prices strips to get a better result, if
 *      nothing was found scan also upside down
 */

public class OcrOptions {

    public static final int REDO_OCR_PRECISION = 3;
    public static final int REDO_OCR_3 = 5; //must be changed with something better
    private static final int DEFAULT_PRECISION = 3;
    private static final boolean DEFAULT_TOTAL = true;
    private static final boolean DEFAULT_DATE = true;
    private static final boolean DEFAULT_PRODUCTS = true;

    private boolean findTotal;
    private boolean findDate;
    private boolean findProducts;
    private int precision;

    /**
     * Constructor
     * @param findTotal true if you want to find total
     * @param findDate true if you want to find date
     * @param findProducts true if you want to find a list of products
     * @param precision Int from 0 to 6.
     */
    public OcrOptions(boolean findTotal, boolean findDate, boolean findProducts, @IntRange(from = 0, to = 6) int precision) {
        this.findTotal = findTotal;
        this.findDate = findDate;
        this.findProducts = findProducts;
        this.precision = precision;
    }

    /**
     * Return default Options
     * @return default options
     */
    public static OcrOptions getDefaultOptions() {
        return new OcrOptions(DEFAULT_TOTAL, DEFAULT_DATE, DEFAULT_PRODUCTS, DEFAULT_PRECISION);
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setFindTotal(boolean findTotal) {
        this.findTotal = findTotal;
    }

    public void setFindDate(boolean findDate) {
        this.findDate = findDate;
    }

    public void setFindProducts(boolean findProducts) {
        this.findProducts = findProducts;
    }

    public boolean isFindTotal() {
        return findTotal;
    }

    public boolean isFindDate() {
        return findDate;
    }

    public boolean isFindProducts() {
        return findProducts;
    }

    public int getPrecision() {
        return precision;
    }
}
