package com.ing.software.ocr;

/**
 * List of static vars used in Ocr
 */
public class OcrVars {

    static final boolean IS_DEBUG_ENABLED = true;
    static final int LOG_LEVEL = 3; //A Higher level, means more things are logged
    static final String[] AMOUNT_STRINGS = {"TOTAL", "IMPORTO"};
    static final int MAX_STRING_DISTANCE = 3;
    public static final String LEFT_TAG = "left";
    public static final String CENTER_TAG = "center";
    public static final String RIGHT_TAG = "right";
    public static final String INTRODUCTION_TAG = "introduction";
    public static final String PRODUCTS_TAG = "products";
    public static final String PRICES_TAG = "prices";
    public static final String CONCLUSION_TAG = "conclusion";
    static final double NUMBER_MIN_VALUE = 0.4;
}
