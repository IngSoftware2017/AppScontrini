package com.ing.software.ocr;

import android.util.Pair;
import android.util.Range;

import com.ing.software.ocr.OperativeObjects.WordMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Arrays.*;
import static java.util.regex.Pattern.compile;

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


    /*
     @author Zaglia
     */


    //match a number between 2 and 4 digits,
    // or match any with 0 to 6 digits before dot and 1 to 2 digits after,
    // or match any with 1 to 6 digits before dot and 0 to 2 digits after,
    // optional minus in front, optional character before end of string (could be another digit).
    static final Pattern POTENTIAL_PRICE = compile(
            "(?<![\\d.,-])-?(?:\\d{2,4}|\\d{0,6}[.,]\\d{1,2}|\\d{1,6}[.,]\\d{0,2})[^.,]?$");
    //match any number with a symbol for two decimals (a dot/comma or a space or a dot/comma + space),
    // optional thousands symbols, optional minus in front, optional character before end of string
    static final Pattern PRICE_WITH_SPACES = compile(
            "(?<![\\d.,'-])-?(?:0|[1-9]\\d{0,3}|[1-9]\\d{0,2}(?:(?:[.,'] |[.,' ])\\d{3})*)(?:[.,] |[., ])\\d{2}(?=[^\\d.,]?$)");
    static final Pattern PRICE_STRICT = compile(
            "(?<![\\d.,'-])-?(?:0|[1-9]\\d{0,3}|[1-9]\\d{0,2}(?:[.,']\\d{3})*)[.,]\\d{2}(?=[^\\d.,]?$)");
    //match any number with no points, optional minus in front, optional character before end of string
    static final Pattern PRICE_NO_DECIMALS = compile(
            "(?<![\\d.-])-?(?:0|[1-9]\\d*)(?=[^\\d.]?$)");
    //match upside down prices. it's designed to reject corrupted upside down prices to avoid false positives.
    static final Pattern PRICE_UPSIDEDOWN = compile(
            "^[^'.,-]?[0OD1Il2ZEh5S9L8B6]{2} ?'[0OD1Il2ZEh5S9L8B6]+[^'.,]?$");
    //java does not support regex subroutines: I have to duplicate the character matching part

    //Used to sanitize price matched with PRICE_WITH_SPACES before cast to BigDecimal
    //the lookahead with anchor makes sure to match only (or exclude) last occurrence
    static final String DECIMAL_SEPARATOR = "(?:[.,] |[., ])(?=\\d{2}$)";
    static final String THOUSAND_SEPARATOR = "(?:[.,'] |[.,' ])(?!\\d{2}$)";


    //todo: use a more general function
    static final Map<Locale, Locale> LANGUAGE_TO_COUNTRY = new HashMap<>();
    static final Map<Locale, Locale> COUNTRY_TO_LANGUAGE = new HashMap<>();

    static {
        LANGUAGE_TO_COUNTRY.put(Locale.ITALIAN, Locale.ITALY);
        LANGUAGE_TO_COUNTRY.put(Locale.ENGLISH, Locale.UK);
        //adding US would overwrite UK.

        COUNTRY_TO_LANGUAGE.put(Locale.ITALY, Locale.ITALIAN);
        COUNTRY_TO_LANGUAGE.put(Locale.UK, Locale.ENGLISH);
        COUNTRY_TO_LANGUAGE.put(Locale.US, Locale.ENGLISH);
    }


    // ideal character width / height
    static final double CHAR_ASPECT_RATIO = 5. / 8.;
}
