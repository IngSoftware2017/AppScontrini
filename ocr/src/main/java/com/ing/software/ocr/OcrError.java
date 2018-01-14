package com.ing.software.ocr;

/**
 * General purpose error enum. Used by ImagePreprocessor, Ticket
 */
public enum OcrError {
    NONE,
    RECT_NOT_FOUND,
    INVALID_POINTS,
    INVALID_STATE,
    CROOKED_TICKET,
    INVALID_PROCESSOR,
    AMOUNT_NOT_FOUND,
    DATE_NOT_FOUND,
    ROTATED_180,
    UNSURE_AMOUNT, //when the amount has been corrected
    UNSURE_DATE, //when the date has been corrected
}
