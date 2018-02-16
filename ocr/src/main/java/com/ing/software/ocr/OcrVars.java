package com.ing.software.ocr;


/**
 * List of static vars used in Ocr
 */
public class OcrVars {

    /*
    @author Michelon
     */

    static final boolean IS_DEBUG_ENABLED = true;
    static final int LOG_LEVEL = 3; //A Higher level, means more things are logged
    public static final double NUMBER_MIN_VALUE = 0.4; //Max allowed value to accept a string as a valid number. See OcrUtils.isPossiblePriceNumber()
    static final float AMOUNT_RECT_HEIGHT_EXTENDER = 0.7f; //Extend height of source amount text. Used in OcrAnalyzer.getAmountExtendedBox()
    static final float PRODUCT_RECT_HEIGHT_EXTENDER = 0.5f; //Extend height of source text of product price.
}
