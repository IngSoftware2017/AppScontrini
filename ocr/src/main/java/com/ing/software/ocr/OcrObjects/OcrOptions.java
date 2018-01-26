package com.ing.software.ocr.OcrObjects;

import android.support.annotation.IntRange;

/**
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on precision)
 *
 * For precision:
 * 0 = scan image at 1/3 of its dimension, don't reanalyze specific parts of image to get better results
 * 1 = scan image at 1/2 of its dimension, don't reanalyze specific parts of image to get better results
 * 2 = scan image at original dimension (passed by imageprocessor), don't reanalyze specific parts of image to get better results
 * 3 = scan image at original dimension (passed by imageprocessor), reanalyze total strip to get a better result
 * 4 = scan image at original dimension (passed by imageprocessor), reanalyze total and prices strips to get a better result
 * 5 = scan image at original dimension (passed by imageprocessor), reanalyze total and prices strips to get a better result, if
 *      nothing was found scan also upside down
 */

public class OcrOptions {

    public static final int REDO_OCR_PRECISION = 3;
    private static final int DEFAULT_PRECISION = 3;
    private static final boolean DEFAULT_TOTAL = true;
    private static final boolean DEFAULT_DATE = true;
    private static final boolean DEFAULT_PRODUCTS = true;

    private boolean findTotal;
    private boolean findDate;
    private boolean findProducts;
    private int precision; //Precision varies from 0 to 5, where 5 = max precision

    public OcrOptions(boolean findTotal, boolean findDate, boolean findProducts, @IntRange(from = 0, to = 5) int precision) {
        this.findTotal = findTotal;
        this.findDate = findDate;
        this.findProducts = findProducts;
        this.precision = precision;
    }

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
