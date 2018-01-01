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
 */
class OcrVars {

    static final boolean IS_DEBUG_ENABLED = false;
    static final int LOG_LEVEL = 3; //A Higher level, means more things are logged
    static final String[] AMOUNT_STRINGS = {"TOTALE", "IMPORTO"};
    static final int MAX_STRING_DISTANCE = 3;

    static final int LANG_EN = 0;
    static final int LANG_IT = 1;

    // day 1 to 31 with or without 0 tens.
    // month 1 to 12 with or without 0 tens.
    // year 1960 to 2059 with or without hundreds.
    // go to https://regex101.com/ to check the behaviour of these regular expressions.
    static final int YEAR_CUT = 60; // YY < 60 -> 20YY;  YY >= 60 -> 19YY
    static final Pattern DATE_DMY = compile(
            "(?<!\\d)(?:0?[1-9]|[12]\\d|3[01])([-/.,])(?:0?[1-9]|1[012])\\1(?:(?:19)?[6-9]|(?:20)?[0-5])\\d(?!\\1|\\d)");
    static final Pattern DATE_MDY = compile(
            "(?<!\\d)(?:0?[1-9]|1[012])([-/.,])(?:0?[1-9]|[12]\\d|3[01])\\1(?:(?:19)?[6-9]|(?:20)?[0-5])\\d(?!\\1|\\d)");
    static final Pattern DATE_YMD = compile(
            "(?<!\\d)(?:(?:19)?[6-9]|(?:20)?[0-5])\\d([-/.,])(?:0?[1-9]|1[012])\\1(?:0?[1-9]|[12]\\d|3[01])(?!\\1|\\d)");
    //back reference/forward reference not supported in lookbehind but is supported in lookahead

    // match every number (with optional hundreds mark) with 2 decimal digits or a "-" (netherlands)
    // accept if there is something before or a single non digit after (ex: €).
    static final Pattern AMOUNT_PRICE_STRICT = compile(
            "(?<!\\d)(?:0|[1-9][\\d,.]*)[,.](?:\\d{2}|-)(?=[^\\d]?$)");

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
