package com.ing.software.ocr;

import android.util.Pair;


import static java.util.Collections.*;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.*;
import static java.util.regex.Pattern.compile;

/**
 * List of static vars used in Ocr
 * todo: describe what these values are
 */
public class OcrVars {

    static final boolean IS_DEBUG_ENABLED = false;
    static final int LOG_LEVEL = 2; //A Higher level, means more things are logged
    static final String[] AMOUNT_STRINGS = {"TOTAL", "IMPORTO"}; //Array of strings that contains the definition of total
    static final int MAX_STRING_DISTANCE = 3; //Max allowed distance (levDistance) between a string found in a rawtext and one from AMOUNT_STRINGS
    public static final String LEFT_TAG = "left"; //tag for rawtext on left of the receipt
    public static final String CENTER_TAG = "center"; //tag for rawtext on center of the receipt
    public static final String RIGHT_TAG = "right"; //tag for rawtext on right of the screen
    public static final String INTRODUCTION_TAG = "introduction"; //tag for rawtext on top of the receipt
    public static final String PRODUCTS_TAG = "products"; //tag for rawtext on central-left part of the receipt
    public static final String PRICES_TAG = "prices"; //tag for rawtext on central-right part of the receipt
    public static final String CONCLUSION_TAG = "conclusion"; //tag for rawtext on bottom of the receipt
    static final double NUMBER_MIN_VALUE = 0.4; //Max allowed value to accept a string as a valid number. See OcrUtils.isPossiblePriceNumber()
    static final double NUMBER_MIN_VALUE_ALTERNATIVE = 0.1; //Max allowed value to accept a string as a valid number. See OcrUtils.isPossiblePriceNumber(). Used in alternative search
    static final int HEIGHT_CENTER_DIFF_MULTIPLIER = 50; //Multiplier used while analyzing difference in alignment between the center of two rects in dataAnalyzer
    static final int HEIGHT_LIST_MULTIPLIER = 80; //Multiplier used while analyzing difference between average height of rects and a specific rect. Used in ProbGrid.getRectHeightScore()
    public static final int HEIGHT_SOURCE_DIFF_MULTIPLIER = 50; //Multiplier used while analyzing difference in height between source and target rect (total with it's price)

}
