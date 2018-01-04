package com.ing.software.ocr;

import com.ing.software.ocr.OcrObjects.RawText;

/**
 * Static class containing grids for probability regions (WIP)
 * @author Michelon
 */
public class ProbGrid {

    public static final int[] amountBlockIntroduction = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] amountBlockProducts = new int[] {0, 0, 0, 5, 5, 10, 15, 20, 15, 10};
    public static final int[] amountBlockConclusion = new int[] {5, 5, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] dateBlockIntroduction = new int[] {0, 0, 0, 0, 0, 5, 5, 10, 15, 15};
    public static final int[] dateBlockProducts = new int[] {10, 5, 0, 0, 0, 0, 0, 5, 15, 15};
    public static final int[] dateBlockConclusion = new int[] {15, 10, 10, 5, 5, 10, 5, 5, 10, 10};

    /**
     * Get probability to contain amount considering rect height
     * @param text
     * @return
     */
    public static double getAmountProbRectHeight(RawText text) {
        double average = text.getRawImage().getAverageRectHeight();
        double diff = text.getBoundingBox().height() - average;
        return diff/average*100;
}
}
