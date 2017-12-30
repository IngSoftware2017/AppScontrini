package com.ing.software.ocr;

import android.util.Pair;


import com.ing.software.ocr.OcrObjects.TextMatcher;
import com.mifmif.common.regex.Generex;

import static java.util.Arrays.*;
import java.util.List;

/**
 * List of static vars used in Ocr
 */
class OcrVars {

    static final boolean IS_DEBUG_ENABLED = false;
    static final int LOG_LEVEL = 3; //A Higher level, means more things are logged
    static final String[] AMOUNT_STRINGS = {"TOTALE", "IMPORTO"};
    static final int MAX_STRING_DISTANCE = 3;

    static List<TextMatcher> AMOUNT_MATCHERS = asList(
            new TextMatcher("T[OUD]TALE[E]{0,1}", 6, 1, 1),
            new TextMatcher("TOT", 3, 0, 1),
            new TextMatcher("T[OUD]TALEE[UL]R[OD]{0,1}", 8, 3, 1),
            new TextMatcher("IMP[OU]RT[OD]", 7, 1, 1),
            new TextMatcher("IMP[OU]RT[OD]E[UL]R[OD]{0,1}", 8, 3, 1)
    );
}
