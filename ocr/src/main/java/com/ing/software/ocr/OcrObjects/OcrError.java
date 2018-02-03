package com.ing.software.ocr.OcrObjects;

/**
 * General purpose error enum. Used by ImagePreprocessor, Ticket
 */
public enum OcrError {

    /**
     * The OcrManager ha not been successfully initialized
     */
    UNINITIALIZED,

    /**
     * An invalid ImageProcessor instance has been passed.
     * <p> Likely cause: the bitmap has not been set. </p>
     */
    INVALID_PROCESSOR,

    /**
     * The amount has been found, but with a low confidence.
     * It could be the wrong price inside the ticket or some digits could have been misidentified.
     */
    UNCERTAIN_AMOUNT,

    /**
     * The total amount has not been found.
     * The amount field is null.
     */
    AMOUNT_NOT_FOUND,

    /**
     * The date has been found, but with a low confidence.
     * It could be the wrong date inside the ticket.
     */
    UNCERTAIN_DATE,

    /**
     * The date as not been found.
     * The date field is null.
     */
    DATE_NOT_FOUND,

    /**
     * The image undistorted image is upside down.
     * Please proceed to update the saved photo and then rotate the corners with
     * ImageProcessor.rotateUpsideDown(), then save the new corners obtained with ImageProcessor.getCorners()
     */
    ROTATED_180,

}
