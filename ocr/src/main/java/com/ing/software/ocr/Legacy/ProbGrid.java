package com.ing.software.ocr.Legacy;

import android.support.annotation.NonNull;

import com.ing.software.ocr.Legacy.RawText;

import static com.ing.software.ocr.OcrVars.HEIGHT_LIST_MULTIPLIER;

/**
 * Static class containing grids for probability regions (WIP)
 * @author Michelon
 */
public class ProbGrid {

    public static final int GRID_LENGTH = 10;
    public static final int[] amountBlockIntroduction = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] amountBlockProducts = new int[] {0, 0, 0, 5, 5, 10, 15, 20, 15, 10};
    public static final int[] amountBlockConclusion = new int[] {5, 5, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] dateBlockIntroduction = new int[] {0, 0, 0, 0, 0, 5, 5, 10, 15, 15};
    public static final int[] dateBlockProducts = new int[] {10, 5, 0, 0, 0, 0, 0, 5, 15, 15};
    public static final int[] dateBlockConclusion = new int[] {15, 10, 10, 5, 5, 10, 5, 5, 10, 10};

    /**
     * Get probability considering rect height
     * @param text source rawText. Not null.
     * @return score
     */
    public static double getRectHeightScore(@NonNull RawText text) {
        double average = text.getRawImage().getAverageRectHeight();
        double diff = text.getBoundingBox().height() - average;
        return diff/average*HEIGHT_LIST_MULTIPLIER;
    }
}
