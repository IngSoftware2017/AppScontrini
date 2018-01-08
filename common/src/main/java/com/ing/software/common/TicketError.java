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
    INVALID_PROCESSOR,
    AMOUNT_NOT_FOUND,
    DATE_NOT_FOUND,
}
