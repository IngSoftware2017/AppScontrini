package com.ing.software.ocr;

import android.util.Pair;


import com.ing.software.ocr.OcrObjects.WordMatcher;

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



    static final String LANG_EN = "en";
    static final String LANG_IT = "it";
    static final List<String> LANGS = asList(LANG_EN, LANG_IT);

    // day 1 to 31 with or without 0 tens.
    // month 1 to 12 with or without 0 tens.
    // year 1960 to 2059 with or without hundreds.
    // go to https://regex101.com/ to check the behaviour of these regular expressions.
    static final int YEAR_CUT = 60; // YY < 60 -> 20YY;  YY >= 60 -> 19YY
    // use groups "day", "month", "year" to retrieve respective values
    static final Pattern DATE_DMY = compile(
            "(?<!\\d)(?<day>0?[1-9]|[12]\\d|3[01])([-\\/.])(?<month>0?[1-9]|1[012])\\2(?<year>(?:19)?[6-9]\\d|(?:20)?[0-5]\\d)(?!\\2|\\d)");
    static final Pattern DATE_MDY = compile(
            "(?<!\\d)(?<month>0?[1-9]|1[012])([-\\/.])(?<day>0?[1-9]|[12]\\d|3[01])\\2(?<year>(?:19)?[6-9]\\d|(?:20)?[0-5]\\d)(?!\\2|\\d)");
    static final Pattern DATE_YMD = compile(
            "(?<!\\d)(?<year>(?:19)?[6-9]\\d|(?:20)?[0-5]\\d)([-\\/.])(?<month>0?[1-9]|1[012])\\2(?<day>0?[1-9]|[12]\\d|3[01])(?!\\2|\\d)");
    //back reference/forward reference not supported in lookbehind but is supported in lookahead

    // match every number (with optional thousands mark) with 2 decimal digits or a "-" (netherlands)
    // accept if there is something before or a single non digit after (ex: €).
    static final Pattern PRICE_PERMISSIVE = compile(
            "(?<!\\d|\\.)(?:0|[1-9][\\d.]*?)\\.(?:\\d{2}|-)(?=[^\\d]?$)");
    static final Pattern PRICE_NO_THOUSAND_MARK = compile(
            "(?<!\\d|\\.)(?:0|[1-9]\\d*?)\\.(?:\\d{2}|-)(?=[^\\d.]?$)");

    // regex-score pairs.
    static final List<Pair<Pattern, Double>> DATE_REGEX_EN = asList(
            new Pair<>(DATE_DMY, 3.), // EN-GB
            new Pair<>(DATE_MDY, 2.) // EN-US
    );
    static final List<Pair<Pattern, Double>> DATE_REGEX_IT = singletonList(new Pair<>(DATE_DMY, 3.));

    static final List<WordMatcher> AMOUNT_MATCHERS = asList(
            new WordMatcher("T[OUD]TALE", 6, 1),
            new WordMatcher("TOT", 3, 0),
            new WordMatcher("T[OUD]TALEE[UI]R[OD]", 8, 3),
            new WordMatcher("IMP[OU]RT[OD]", 7, 1),
            new WordMatcher("IMP[OU]RT[OD]E[UI]R[OD]", 8, 3)
    );

    // ideal character width / height
    static final double CHAR_ASPECT_RATIO = 5./8.;

}
