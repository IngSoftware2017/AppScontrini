package com.ing.software.ocr.OcrObjects;

import android.support.annotation.IntRange;

/**
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on precision)
 */

public class OcrOptions {

    private static final int DEFAULT_PRECISION = 2;
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
