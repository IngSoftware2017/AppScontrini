package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.common.Scored;
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
    public static double getAmountScore(Scored<TempText> source) {
        return 0;
    }

    /*
    get score for rect containing amount text (='totale')
     */
    public static double getSourceAmountScore(Scored<TempText> source) {
        return 0;
    }

    /*
    get score according to difference between source and target rects (distance between centers, height etc)
     */
    public static double getDistFromSourceScore(TempText source, TempText target) {
        return 0;
    }

    /**
     * Get position of text in its block, as an int from 0 to GRID_LENGTH. -1 if an error occurred.
     * @param text source text
     * @return position of the rect in its block.
	 * todo: directly return score from grid above
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
    private static int getTextBlockPosition(TempText text, RectF rect) {
        float startPosition = rect.top;
        float endPosition = rect.bottom;
        float position = ((text.box().centerY() - startPosition))/(endPosition - startPosition);
        if (position > (GRID_LENGTH - 1)/GRID_LENGTH) //Fix IndexOutOfBound Exception if it's rect on bottom
            position = (GRID_LENGTH - 1)/GRID_LENGTH + 0.1f;
        return (int)(position*GRID_LENGTH);
    }

    /**
     * @author Michelon
     * @date 27-1-18
     * Check if a string may be a number.
     * Characters changed in sanitized are considered specials (see return statement).
     * If string is longer than NUMBER_MAX_LENGTH default is Integer.MAX_VALUE (allowed numbers up to nn.nnn,nn)
     * return is decreased if one '.' in sanitized is present, increased if more than one are present.
     * @param originalNoSpace string with original text (textnospaces)
     * @param sanitized string with sanitized text (numnospaces)
     * @return Integer.MAX_VALUE if less than MIN_DIGITS_NUMBER of the string are not numbers;
     * otherwise number of non-digit chars (*0.5 if special)/length
     */
    public static double isPossiblePriceNumber(String originalNoSpace, String sanitized) {
        double specialCharsMultiplier = 0.5;
        if (sanitized.length() >= NUMBER_MAX_LENGTH)
            return Integer.MAX_VALUE;
        int digits = 0;
        int initialLength = originalNoSpace.length();
        if (sanitized.contains(".")) {
            --initialLength;
            ++digits;
        }
        for (int i = 0; i < sanitized.length(); ++i) {
            if (Character.isDigit(sanitized.charAt(i)))
                ++digits;
        }
        if (digits < (double)sanitized.length()*MIN_DIGITS_NUMBER)
            return Integer.MAX_VALUE;
        return ((initialLength - sanitized.length())*specialCharsMultiplier + (sanitized.length() - digits))/sanitized.length();
    }
}
