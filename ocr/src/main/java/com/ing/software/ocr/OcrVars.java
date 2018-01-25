package com.ing.software.ocr;

import android.annotation.SuppressLint;
import android.util.Pair;


import com.ing.software.ocr.OcrObjects.WordMatcher;

import static java.util.Collections.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    // level of detail of image to be passed to OCR engine
    static final double OCR_NORMAL_SCALE = 1.;
    static final double OCR_ADVANCED_SCALE = 1. / 3.;

    // Extended rectangle vertical multiplier
    static final float EXT_RECT_V_MUL = 3;

    // day 1 to 31 with or without 0 tens.
    // month 1 to 12 with or without 0 tens.
    // year 1900 to 2099 if format YYYY, year 1960 to 2059 if format YY.
    // go to https://regex101.com/ to check the behaviour of these regular expressions.
    //back reference/forward reference not supported in lookbehind but is supported in lookahead
    static final Pattern DATE_DMY = compile(
            "(?<!\\d)(0?[1-9]|[12]\\d|3[01])([-\\/.])(0?[1-9]|1[012])\\2((?:19)?[6-9]\\d|(?:20)?[0-5]\\d)(?!\\2|\\d)");
    static final int YEAR_CUT = 60; // YY < 60 -> 20YY;  YY >= 60 -> 19YY

    // regex groups for DMY date format
    // group 0 is the whole match
    static final int DMY_DAY = 1;
    // group 2 is the delimiter
    static final int DMY_MONTH = 3;
    static final int DMY_YEAR = 4;
    // I do not use named groups because is not supported for sdk < 24.


    // first group is the sign
    //match a number between 2 and 4 digits,
    // or match any with 0 to 6 digits before dot and 1 to 2 digits after,
    // or match any with 1 to 6 digits before dot and 0 to 2 digits after,
    // optional minus in front, optional character before end of string (could be another digit).
    static final Pattern POTENTIAL_PRICE = compile(
            "(?<![\\d.,-])-?(?:\\d{2,4}|\\d{0,6}[.,]\\d{1,2}|\\d{1,6}[.,]\\d{0,2})[^.,]?$");
    //match any number with one single dot for two decimals, optional space between, optional minus in front, optional character before end of string
    static final Pattern PRICE_NO_THOUSAND_MARK = compile("(?<![\\d.-])(-?)(?:0|[1-9]\\d*)\\. ?\\d{2}(?=[^\\d.]?$)");
    //match any number with no points, optional minus in front, optional character before end of string
    static final Pattern PRICE_NO_DECIMALS = compile("(?<![\\d.-])(-?)(?:0|[1-9]\\d*)(?=[^\\d.]?$)");
    //match upside down prices. it's designed to reject corrupted upside down prices to avoid false positives.
    static final Pattern PRICE_UPSIDEDOWN = compile("^[^'.,-]?[0OD1Il2ZEh5S9L8B6]{2} ?'[0OD1Il2ZEh5S9L8B6]+[^'.,]?$");
    //java does not support regex subroutines: I have to duplicate the character matching part


    //In principle, multiple words should be matched with a space between them,
    //but since sometimes some words are split into multiple words, I remove all spaces all together
    //and match the words without spaces, even if there were in origin effectively distinct words.
    //The accepted errors (ex: O -> U,D) are based on common errors of the ocr scanning the dataset.
    //IT matchers:
    static final List<WordMatcher> IT_AMOUNT_MATCHERS = asList(
            new WordMatcher("T[OUD]TALE", 1),
            new WordMatcher("T[OUD]TALEE[UI]R[OD]", 3),
            new WordMatcher("IMP[OU]RT[OD]", 1),
            new WordMatcher("TOT", 0),
            new WordMatcher("TOTE[UI]R[OD]", 1),
            new WordMatcher("IMP[OU]RT[OD]E[UI]R[OD]", 3)
    );
    static final List<WordMatcher> IT_CASH_MATCHERS = asList(
            new WordMatcher("CONTANT[EI]", 1),
            new WordMatcher("CARTADICREDITO", 3),
            new WordMatcher("PAGAMENTOCONTANTE", 4),
            new WordMatcher("CCRED", 0),
            new WordMatcher("ASSEGNI", 1)
    );
    //NB: change can be negative
    static final List<WordMatcher> IT_CHANGE_MATCHERS = asList(
            new WordMatcher("RESTO", 1)
    );
    static final List<WordMatcher> IT_INDOOR_MATCHERS = asList(
            new WordMatcher("COPERTO", 1)
    );
    //in italy, subtotal is rarely present, skip it

    //EN matchers:
    static final List<WordMatcher> EN_AMOUNT_MATCHERS = asList(
            new WordMatcher("T[OUD]TAL", 1),
            new WordMatcher("AMOUNT", 1),
            new WordMatcher("GRANDTOTAL", 2)
    );
    static final List<WordMatcher> EN_CASH_MATCHERS = asList(
            new WordMatcher("CASH", 1)
    );
    static final List<WordMatcher> EN_CHANGE_MATCHERS = asList(
            new WordMatcher("CHANGE", 1)
    );
    static final List<WordMatcher> EN_SUBTOTAL_MATCHERS = asList(
            new WordMatcher("SUBTOTAL", 1)
    );
    static final List<WordMatcher> EN_TAX_MATCHERS = asList(
            new WordMatcher("TAX", 0),
            new WordMatcher("SALESTAX", 1)
    );

    // currency
    // todo: accept currency symbols?
    static final List<WordMatcher> EUR_MATCHERS = asList(
            new WordMatcher("EUR[OD]", 0),
            new WordMatcher("EUR", 0)
    );
    static final List<WordMatcher> USD_MATCHERS = asList(
            new WordMatcher("USD", 0)
    );
    static final List<WordMatcher> GBP_MATCHERS = asList(
            new WordMatcher("GBP", 0)
    );



    // ideal character width / height
    static final double CHAR_ASPECT_RATIO = 5./8.;

}
