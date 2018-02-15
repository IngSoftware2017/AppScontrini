package com.ing.software.ocr;

/**
 * List of static vars used in Ocr
 */
public class OcrVars {

    /*
    @author Michelon
     */

    public static final boolean IS_DEBUG_ENABLED = true;
    static final int LOG_LEVEL = 3; //A Higher level, means more things are logged
    public static final int MAX_STRING_DISTANCE = 3; //Max allowed distance (levDistance) between a string found in a rawtext and one from AMOUNT_STRINGS
    public static final String LEFT_TAG = "left"; //tag for rawtext on left of the receipt
    public static final String CENTER_TAG = "center"; //tag for rawtext on center of the receipt
    public static final String RIGHT_TAG = "right"; //tag for rawtext on right of the screen
    public static final String INTRODUCTION_TAG = "introduction"; //tag for rawtext on top of the receipt
    public static final String PRODUCTS_TAG = "products"; //tag for rawtext on central-left part of the receipt
    public static final String PRICES_TAG = "prices"; //tag for rawtext on central-right part of the receipt
    public static final String CONCLUSION_TAG = "conclusion"; //tag for rawtext on bottom of the receipt
    public static final double NUMBER_MIN_VALUE = 0.4; //Max allowed value to accept a string as a valid number. See OcrUtils.isPossiblePriceNumber()
    public static final double NUMBER_MIN_VALUE_ALTERNATIVE = 0.1; //Max allowed value to accept a string as a valid number. See OcrUtils.isPossiblePriceNumber(). Used in alternative search
    static final float AMOUNT_RECT_HEIGHT_EXTENDER = 1f; //Extend height of source amount text. Used in OcrAnalyzer.getAmountExtendedBox()
    static final float PRODUCT_RECT_HEIGHT_EXTENDER = 0.5f; //Extend height of source text of product price.
}
