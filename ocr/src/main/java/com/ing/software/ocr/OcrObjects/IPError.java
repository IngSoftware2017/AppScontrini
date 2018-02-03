package com.ing.software.ocr.OcrObjects;


public enum IPError {

    /**
     * The image has not been set
     */
    IMAGE_NOT_SET,

    /**
     * Perspective rectangle of ticket has not been found, a bounding box is used instead
     * (getCorners() will return valid corners anyway).
     */
    RECT_NOT_FOUND,

    /**
     * The ticket in the original photo was framed sideways, but it has been straightened
     */
    CROOKED_TICKET,

    /**
     * The ticket orientation could not be established, the rectangle is forced to be upright.
     */
    UNCERTAIN_DIRECTION,

    /**
     * The ticket is overexposed
     */
    OVEREXPOSED,

    /**
     * The ticket is underexposed
     */
    UNDEREXPOSED,

    /**
     * The ticket is out of focus
     */
    OUT_OF_FOCUS,

    /**
     * The ticket has poor contrast relative to background.
     * <p> Could be caused by shadows </p>
     */
    POOR_BG_CONTRAST,

    /**
     * findTicket(): the corners are not found (getCorners() will return empty list).
     *
     * <p> setCorners(): the corners passed are invalid.
     * Causes: the list of points passed contains less or more than 4 points </p>
     */
    INVALID_CORNERS,
}
