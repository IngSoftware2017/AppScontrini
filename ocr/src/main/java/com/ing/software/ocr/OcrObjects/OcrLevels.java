package com.ing.software.ocr.OcrObjects;

/**
 * Levels of scan
 * If more than 1 are present for the same target (eg VERY_QUICK and NORMAL) higher level is used.
 * One of VERY_QUICK, QUICK and NORMAL must be specified
 */

public enum OcrLevels {
    /**
     * Downscale image to 1/3
     */
    VERY_QUICK,

    /**
     * Downscale image to 1/2
     */
    QUICK,

    /**
     * Use original image
     */
    NORMAL,

    /**
     * Use fast detected texts
     */
    AMOUNT_NORMAL,

    /**
     * Redo ocr on target strip
     */
    AMOUNT_DEEP,

    /**
     * Use fast detected texts
     */
    PRICES_NORMAL,

    /**
     * Redo ocr on target strip
     */
    PRICES_DEEP,

    /**
     * Use fast detected texts
     */
    DATE_NORMAL,

    /**
     * Redo search if first amount target is not a valid amount
     * (up to 3 searches). This overwrites AMOUNT_XXXX 'cause it uses AMOUNT_DEEP
     */
    EXTENDED_SEARCH,

    /**
     * Scan image upside down if nothing was found
     */
    UPSIDE_DOWN_SEARCH,
}
