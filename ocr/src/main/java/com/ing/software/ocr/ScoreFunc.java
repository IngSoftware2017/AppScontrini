package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.ocr.OcrObjects.TempText;

import static com.ing.software.ocr.OcrVars.*;

/**
 *
 */

public class ScoreFunc {

    public static final int GRID_LENGTH = 10;
    public static final int[] amountBlockIntroduction = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] amountBlockProducts = new int[] {0, 0, 0, 5, 5, 10, 15, 20, 15, 10};
    public static final int[] amountBlockConclusion = new int[] {5, 5, 0, 0, 0, 0, 0, 0, 0, 0};
    public static final int[] dateBlockIntroduction = new int[] {0, 0, 0, 0, 0, 5, 5, 10, 15, 15};
    public static final int[] dateBlockProducts = new int[] {10, 5, 0, 0, 0, 0, 0, 5, 15, 15};
    public static final int[] dateBlockConclusion = new int[] {15, 10, 10, 5, 5, 10, 5, 5, 10, 10};

    /*
    Get score for text on same height as amount text
     */
    public static double getAmountScore(TempText text) {
        return 0;
    }

    /*
    get score for rect containing amount text (='totale')
     */
    public static double getSourceAmountScore(TempText source) {
        return 0;
    }

    /**
     * Get position of text in its block, as an int from 0 to GRID_LENGTH. -1 if an error occurred.
     * @param text source text
     * @return position of the rect in its block.
     */
    public static int getInBlockPosition(TempText text) {
        if (text.getTags().contains(INTRODUCTION_TAG))
            return getTextBlockPosition(text, OcrManager.mainImage.getIntroRect());
        else if (text.getTags().contains(PRODUCTS_TAG))
            return getTextBlockPosition(text, OcrManager.mainImage.getProductsRect());
        else if (text.getTags().contains(PRICES_TAG))
            return getTextBlockPosition(text, OcrManager.mainImage.getPricesRect());
        else if (text.getTags().contains(CONCLUSION_TAG))
            return getTextBlockPosition(text, OcrManager.mainImage.getConclusionRect());
        else
            return -1;
    }

    /**
     * Find position of a text inside its block with the formula: (text.centerY-start)/(end-start)
     * @param text source rawText. Not Null. Must be inside the block.
     * @param rect rect containing the whole block.
     * @return position as a int between 0 and GRID_LENGTH.
     */
    static int getTextBlockPosition(TempText text, RectF rect) {
        float startPosition = rect.top;
        float endPosition = rect.bottom;
        float position = ((text.box().centerY() - startPosition))/(endPosition - startPosition);
        if (position > (GRID_LENGTH - 1)/GRID_LENGTH) //Fix IndexOutOfBound Exception if it's rect on bottom
            position = (GRID_LENGTH - 1)/GRID_LENGTH + 0.1f;
        return (int)(position*GRID_LENGTH);
    }
}
