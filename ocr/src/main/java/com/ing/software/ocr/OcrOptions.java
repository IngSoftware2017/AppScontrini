package com.ing.software.ocr;

import java.util.Locale;

/**
 * @author Michelon
 * @author EDIT: Zaglia
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on image scale)
 */

public class OcrOptions {

    //NB: the flags inside these enums are ordered in a certain way to make use of ordinal().
    // Do not reorder.
    public enum Resolution {

        /**
         * Use original image
         */
        NORMAL,

        /**
         * Downscale image to 1/2
         */
        HALF,

        /**
         * Downscale image to 1/3
         */
        THIRD,
    }

    public enum DateSearch {

        SKIP,

        /**
         * Use fast detected texts
         */
        NORMAL,
    }

    public enum TotalSearch {

        SKIP,

        /**
         * Use fast detected texts
         */
        NORMAL,

        /**
         * Redo ocr on target strip
         */
        DEEP,

        /**
         * Redo search if first amount target is not a valid amount (up to 3 searches).
         */
        EXTENDED_SEARCH,
    }

    public enum ProductsSearch {

        SKIP,

        /**
         * Use fast detected texts
         */
        NORMAL,

        /**
         * Redo ocr on target strip
         */
        DEEP,
    }

    public enum Orientation {

        NORMAL,

        /**
         * Rescan image upside down if nothing was found
         */
        ALLOW_UPSIDE_DOWN,

        FORCE_UPSIDE_DOWN,
    }

    public static final Resolution DEFAULT_RESOLUTION = Resolution.HALF;
    public static final TotalSearch DEFAULT_TOTAL_SEARCH = TotalSearch.DEEP;
    public static final DateSearch DEFAULT_DATE_SEARCH = DateSearch.NORMAL;
    public static final ProductsSearch DEFAULT_PRODUCTS_SEARCH = ProductsSearch.DEEP;
    public static final Orientation DEFAULT_ORIENTATION = Orientation.NORMAL;
    public static final Locale DEFAULT_COUNTRY = Locale.ITALY;

    public Resolution resolution;
    public TotalSearch totalSearch;
    public DateSearch dateSearch;
    public ProductsSearch productsSearch;
    public Orientation orientation;
    public Locale suggestedCountry;

    /**
     * Return default Options
     * @return default options
     */
    public static OcrOptions getDefault() {
        return new OcrOptions()
                .total(DEFAULT_TOTAL_SEARCH)
                .resolution(DEFAULT_RESOLUTION)
                .date(DEFAULT_DATE_SEARCH)
                .products(DEFAULT_PRODUCTS_SEARCH)
                .orientation(DEFAULT_ORIENTATION)
                .suggestedCountry(DEFAULT_COUNTRY);
    }

    /**
     * Set resolution level
     * @param level resolution level
     * @return OcrOptions instance
     */
    public OcrOptions resolution(Resolution level) {
        resolution = level;
        return this;
    }

    /**
     * Set total search criteria
     * @param criteria
     * @return OcrOptions instance
     */
    public OcrOptions total(TotalSearch criteria) {
        totalSearch = criteria;
        return this;
    }

    /**
     * Set date search criteria
     * @param criteria
     * @return OcrOptions instance
     */
    public OcrOptions date(DateSearch criteria) {
        dateSearch = criteria;
        return this;
    }

    /**
     * Set products search criteria
     * @param criteria
     * @return OcrOptions instance
     */
    public OcrOptions products(ProductsSearch criteria) {
        productsSearch = criteria;
        return this;
    }

    /**
     * Set orientation criteria
     * @param criteria
     * @return OcrOptions instance
     */
    public OcrOptions orientation(Orientation criteria) {
        orientation = criteria;
        return this;
    }

    /**
     * Set suggested country.
     * @param locale ISO country
     * @return OcrOptions instance
     */
    public OcrOptions suggestedCountry(Locale locale) {
        suggestedCountry = locale;
        return this;
    }

    double getResolutionMultiplier() {
        return 1. / (resolution.ordinal() + 1);
    }
}
