package com.ing.software.ocr;

/**
 * List of static vars used in Ocr
 */
public class OcrVars {

    static final boolean IS_DEBUG_ENABLED = true;
    static final int LOG_LEVEL = 3; //A Higher level, means more things are logged
    static final String[] AMOUNT_STRINGS = {"TOTALE", "IMPORTO", "AMOUNT"};
    static final int MAX_STRING_DISTANCE = 3;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;
    public static final String AMOUNT_RECEIVED = "amount";
    public static final String DURATION_RECEIVED = "duration";
    public static final String ERROR_RECEIVED = "error";
    public static final String IMAGE_RECEIVED = "image";
}
