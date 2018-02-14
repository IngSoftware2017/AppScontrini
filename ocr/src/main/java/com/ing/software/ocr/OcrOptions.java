package com.ing.software.ocr;

import android.support.annotation.IntRange;

import java.util.Locale;

import static java.lang.Math.min;

/**
 * @author Michelon
 * @author EDIT: Zaglia
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on precision)
 * Setters return the instance itself to allow builder pattern
 * NOTE: As of now (31-1) only precisions 0-3 are implemented
 *
 * For precision:
 * 0 = scan image at 1/3 of its dimension, don't reanalyze specific parts of image to get better results
 * 1 = scan image at 1/2 of its dimension, don't reanalyze specific parts of image to get better results
 * 2 = scan image at original dimension (passed by imageprocessor), don't reanalyze specific parts of image to get better results
 * 3 = scan image at original dimension (passed by imageprocessor), reanalyze total strip to get a better result (only first element)
 * 4 = scan image at original dimension (passed by imageprocessor), reanalyze total (only first element) and prices strips to get a better result
 * 5 = scan image at original dimension (passed by imageprocessor), reanalyze total (first 3 elements) and prices strips to get a better result
 */

/* ZAGLIA: at precision level 0 and 1 we should still use ocr strip reanalysis because:
   * it's almost inexpensive time-wise
   * The benefits of strip are limited at full resolution, because the strip is always created already at full resolution
 */

public class OcrOptions {

    public static final int REDO_OCR_PRECISION = 3;
    public static final int REDO_OCR_3 = 5; //must be changed with something better

    public static final int DEFAULT_PRECISION = 3;
    public static final boolean DEFAULT_FIND_TOTAL = true;
    public static final boolean DEFAULT_FIND_DATE = true;
    public static final boolean DEFAULT_FIND_PRODUCTS = true;
    public static final boolean DEFAULT_SPECULATIVE = true;
    public static final boolean DEFAULT_RETRY_UPSIDE_DOWN = false;
    public static final Locale DEFAULT_COUNTRY = Locale.ITALY;

    public boolean shouldFindTotal;
    public boolean shouldFindDate;
    public boolean shouldFindProducts;
    public boolean useSpeculative;
    public boolean canRetryUpsideDown;
    public Locale suggestedCountry;
    public int precision;

    /**
     * Return default Options
     * @return default options
     */
    public static OcrOptions getDefault() {
        return new OcrOptions()
                .shouldFindTotal(DEFAULT_FIND_TOTAL)
                .useSpeculative(DEFAULT_SPECULATIVE)
                .precision(DEFAULT_PRECISION)
                .shouldFindDate(DEFAULT_FIND_DATE)
                .shouldFindProducts(DEFAULT_FIND_PRODUCTS)
                .canRetryUpsideDown(DEFAULT_RETRY_UPSIDE_DOWN)
                .suggestedCountry(DEFAULT_COUNTRY);
    }

    /**
     * Set precision level
     * @param level precision level
     * @return OcrOptions instance
     */
    public OcrOptions precision(@IntRange(from = 0, to = 6) int level) {
        precision = level;
        return this;
    }

    /**
     * Set flag if should try to correct data returned with ticket.
     * @param flag use speculative
     * @return OcrOptions instance
     */
    public OcrOptions useSpeculative(boolean flag) {
        this.useSpeculative = flag;
        return this;
    }

    /**
     * Set flag if should find total
     * @param flag find total
     * @return OcrOptions instance
     */
    public OcrOptions shouldFindTotal(boolean flag) {
        shouldFindTotal = flag;
        return this;
    }

    /**
     * Set flag if should find date
     * @param flag find date
     * @return OcrOptions instance
     */
    public OcrOptions shouldFindDate(boolean flag) {
        shouldFindDate = flag;
        return this;
    }

    /**
     * Set flag if should find products
     * @param flag find products
     * @return OcrOptions instance
     */
    public OcrOptions shouldFindProducts(boolean flag) {
        shouldFindProducts = flag;
        return this;
    }

    /**
     * Set flag if can retry analysis with upside down image if needed
     * @param flag speculative total
     * @return OcrOptions instance
     */
    public OcrOptions canRetryUpsideDown(boolean flag) {
        canRetryUpsideDown = flag;
        return this;
    }

    /**
     * Suggested country.
     * @param locale ISO country
     * @return OcrOptions instance
     */
    public OcrOptions suggestedCountry(Locale locale) {
        suggestedCountry = locale;
        return this;
    }

    double getResolutionMultiplier() {
        switch (min(precision, 0)) {
            case 0:
                return 1. / 3.;
            case 1:
                return 1. / 2.;
            default:
                return 1;
        }
    }
}
