package com.ing.software.ocr;

import android.util.Pair;


import com.ing.software.ocr.OcrObjects.TextMatcher;
import com.mifmif.common.regex.Generex;

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

    // The supported years are from 1900 to 2099
    // day 1 to 31 with or without decimal 0: (0?[1-9]|[12]\d|3[01])
    // month 1 to 12 with or without decimal 0: (0?[1-9]|1[012])
    // year 1960 to 2059 with or without hundreds: ((19)?[6-9]|(20)?[0-5])\d
    static final Pattern DATE_DMY = compile(
            "(0?[1-9]|[12]\\d|3[01])[-/.](0?[1-9]|1[012])[-/.]((19)?[6-9]|(20)?[0-5])\\d");
    static final Pattern DATE_MDY = compile(
            "(0?[1-9]|1[012])[-/.](0?[1-9]|[12]\\d|3[01])[-/.]((19)?[6-9]|(20)?[0-5])\\d");
    static final Pattern DATE_YMD = compile(
            "((19)?[6-9]|(20)?[0-5])\\d[-/.](0?[1-9]|1[012])[-/.](0?[1-9]|[12]\\d|3[01])");

    // regex-score pairs.
    static final List<Pair<Pattern, Double>> DATE_REGEX_EN = asList(
            new Pair<>(DATE_DMY, 3.), // EN-GB
            new Pair<>(DATE_MDY, 2.) // EN-US
    );
    static final List<Pair<Pattern, Double>> DATE_REGEX_IT = singletonList(new Pair<>(DATE_DMY, 3.));

    static final List<TextMatcher> AMOUNT_MATCHERS = asList(
            new TextMatcher("T[OUD]TALE.?", 6, 1, 1),
            new TextMatcher("TOT", 3, 0, 1),
            new TextMatcher("T[OUD]TALEE[UI]R[OD]?", 8, 3, 1),
            new TextMatcher("IMP[OU]RT[OD]", 7, 1, 1),
            new TextMatcher("IMP[OU]RT[OD]E[UI]R[OD]?", 8, 3, 1)
    );
}
