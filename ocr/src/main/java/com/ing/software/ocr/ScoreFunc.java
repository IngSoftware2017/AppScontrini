package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;

import static com.ing.software.ocr.OcrVars.*;

/**
 * Class used to calculate scores for texts
 */

public class ScoreFunc {

    private static final int HEIGHT_CENTER_DIFF_MULTIPLIER = 50; //Multiplier used while analyzing difference in alignment between the center of two rects (e.g. total with it's price)
    private static final int HEIGHT_CHAR_MULTIPLIER = 50; //Multiplier used while analyzing difference between average char height and a specific rect.
    private static final int WIDTH_CHAR_MULTIPLIER = 80; //Multiplier used while analyzing difference between average char width and a specific rect.
    private static final int HEIGHT_SOURCE_DIFF_MULTIPLIER = 50; //Multiplier used while analyzing difference in height between source and target rect (e.g. total with it's price)
    private static final int NUMBER_MAX_LENGTH = 8; //Max number of digits allowed for numbers
    private static final double MIN_DIGITS_NUMBER = 2./3.; //Min number of digits in a string to be considered a number

    private static final int GRID_LENGTH = 10;
    private static final int[] amountBlockIntroduction = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] amountBlockProducts = new int[] {0, 0, 0, 5, 5, 10, 15, 20, 15, 10};
    private static final int[] amountBlockConclusion = new int[] {5, 5, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] dateBlockIntroduction = new int[] {0, 0, 0, 0, 0, 5, 5, 10, 15, 15};
    private static final int[] dateBlockProducts = new int[] {10, 5, 0, 0, 0, 0, 0, 5, 15, 15};
    private static final int[] dateBlockConclusion = new int[] {15, 10, 10, 5, 5, 10, 5, 5, 10, 10};

    /*
    In order we call:
    - getSourceAmountScore to score texts containing amount string
    - getDistFromSourceScore to score texts containing possible price for amount (based on difference from source)
    - getAmountScore to add score to texts containing possible price for amount (based on absolute height of rect and position)
     */

    /**
     * Get score for text based on position and height/width of its rect
     * @param target text to score
     * @return new score for text + old score
     */
    public static double getAmountScore(Scored<OcrText> target) {
        double positionScore = getAmountBlockScore(target.obj());
        //todo: add score for height etc.
        return positionScore + target.getScore();
    }

    /**
     * Get score for text containing amount string (='totale')
     * @param source text containing amount string
     * @return new score + old score
     */
    public static double getSourceAmountScore(Scored<OcrText> source) {
        double average = OcrManager.mainImage.getAverageCharHeight();
        double heightDiff = source.obj().charHeight() - average;
        heightDiff = heightDiff/average*HEIGHT_CHAR_MULTIPLIER;
        average = OcrManager.mainImage.getAverageCharWidth();
        double widthDiff = source.obj().charWidth() - average;
        widthDiff = widthDiff/average*WIDTH_CHAR_MULTIPLIER;
        OcrUtils.log(5, "getSourceAmountScore", "Score for text: " + source.obj().text()
                + " is: " + source.getScore() + " + (heightDiff) " + heightDiff + " + (widthDiff) " + widthDiff);
        return source.getScore() + heightDiff + widthDiff;
    }

    /**
     * Get score according to difference between source and target texts (distance between centers, height etc)
     * @param source source text
     * @param target target text
     * @return score for chosen texts
     */
    static double getDistFromSourceScore(OcrText source, OcrText target) {
        OcrUtils.log(7, "getDistFromSource:", "Source rect is (l,t,r,b): (" + source.box().left + "," +
            source.box().top + "," + source.box().right + "," + source.box().bottom + ") \n Target is: ("+
                target.box().left + "," + target.box().top + "," + target.box().right + "," + target.box().bottom + ")");
        OcrUtils.log(7, "getDistFromSource:", "Source center is: " + source.box().centerY()
            + "\n Target center is: " + target.box().centerY());
        double diffCenter = Math.abs(source.box().centerY() - target.box().centerY());
        OcrUtils.log(7, "getDistFromSource:", "Partial diff is: " + diffCenter);
        diffCenter = (source.height() - diffCenter)/source.height()* HEIGHT_CENTER_DIFF_MULTIPLIER;
        double heightDiff = ((double)Math.abs(source.height() - target.height()))/source.height();
        heightDiff = (1.-heightDiff)*HEIGHT_SOURCE_DIFF_MULTIPLIER;  //<- always 0, why? ZAGLIA: should be good now
        OcrUtils.log(5, "getDistFromSourceScore", "Score for text: " + target.text() +
            " with source: " + source.text() + " is: (diffCenter) " + diffCenter + " + (heightDiff) " + heightDiff);
        return diffCenter + heightDiff;
    }

    /**
     * Get score of text according to position in its block, -1 if an error occurred.
     * @param text source text
     * @return score of the rect in its block.
	 */
    private static int getAmountBlockScore(OcrText text) {
        if (text.getTags().contains(INTRODUCTION_TAG))
            return amountBlockIntroduction[getTextBlockPosition(text, OcrManager.mainImage.getIntroRect())];
        else if (text.getTags().contains(PRODUCTS_TAG))
            return amountBlockProducts[getTextBlockPosition(text, OcrManager.mainImage.getProductsRect())];
        else if (text.getTags().contains(PRICES_TAG))
            return amountBlockProducts[getTextBlockPosition(text, OcrManager.mainImage.getPricesRect())];
        else if (text.getTags().contains(CONCLUSION_TAG))
            return amountBlockConclusion[getTextBlockPosition(text, OcrManager.mainImage.getConclusionRect())];
        else
            return -1;
    }

    /**
     * Find position of a text inside its block with the formula: (text.centerY-start)/(end-start)
     * @param text source Text. Not Null. Must be inside the block.
     * @param rect rect containing the whole block.
     * @return position as a int between 0 and GRID_LENGTH.
     */
    private static int getTextBlockPosition(OcrText text, RectF rect) {
        float startPosition = rect.top;
        float endPosition = rect.bottom;
        float position = ((text.box().centerY() - startPosition))/(endPosition - startPosition);
        if (position > (GRID_LENGTH - 1)/GRID_LENGTH) //Fix IndexOutOfBound Exception if it's rect on bottom
            position = (GRID_LENGTH - 1)/GRID_LENGTH + 0.1f;
        return (int)(position*GRID_LENGTH);
    }

    /**
     * @author Michelon
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
