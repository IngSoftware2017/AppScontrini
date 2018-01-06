package com.ing.software.ocr;

import android.util.Pair;


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
}
