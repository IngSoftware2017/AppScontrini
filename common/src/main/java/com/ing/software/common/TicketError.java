package com.ing.software.common;

/**
 * General purpose error enum. Used by ImagePreprocessor, Ticket
 */
public enum TicketError {
    NONE,
    RECT_NOT_FOUND,
    INVALID_POINTS,
    INVALID_STATE,
    CROOKED_TICKET,
    //AMOUNT_NOT_FOUND,
    //TITLE_NOT_FOUND,
}
