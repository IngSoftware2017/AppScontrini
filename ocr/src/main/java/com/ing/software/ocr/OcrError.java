package com.ing.software.ocr;

/**
 * General purpose error enum. Used by ImagePreprocessor, Ticket
 * @author Riccardo Zaglia
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
    AMOUNT_NOT_FOUND, // on the user end, the check of the presence of this flag can be replaced with a null check
                      // but checking against this flag makes the code more readable

    /**
     * The date has been found, but with a low confidence.
     * It could be the wrong date inside the ticket.
     */
    UNCERTAIN_DATE,

    /**
     * The date has not been found.
     * The date field is null.
     */
    DATE_NOT_FOUND,

    /**
     * The cover has not been found
     */
    COVER_NOT_FOUND,

    /**
     * The undistorted image is upside down.
     * Please proceed to update the saved photo and then rotate the corners with {@link ImageProcessor}.rotate(2),
     * then save the new corners obtained with {@link ImageProcessor#getCorners()}
     */
    UPSIDE_DOWN,

}
