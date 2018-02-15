package com.ing.software.ocr;

/**
 *
 */

/**
 * Levels of scan
 * If more than 1 are present for the same target (eg VERY_QUICK and NORMAL) higher level is used.
 * One of VERY_QUICK, QUICK and NORMAL must be specified
 */

public enum OcrLevels {
    /**
     * Downscale image to 1/3
     */
    LOW_RES,

    /**
     * Downscale image to 1/2
     */
    HALF_RES,

    /**
     * Use original image
     */
    FULL_RES,

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

    /**
     * Apply correction to total (according to scheme of ticket with cash, change etc.)
     */
    FIX_PRICE,

    /**
     * Try to find a price even if no string for amount was found
     */
    VOID_SEARCH,
}

