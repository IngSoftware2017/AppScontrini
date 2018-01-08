package com.ing.software.ocr.OcrObjects;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.ocr.OcrVars.*;

/**
 * Class to store only useful properties of source images
 * @author Michelon
 */

public class RawImage {

    private int height;
    private int width;
    private Rect extendedRect;
    private double averageRectHeight;
    private List<RawText> allTexts = new ArrayList<>();
    private List<RawText> possibleIntro = new ArrayList<>();
    private List<RawText> possibleProducts = new ArrayList<>();
    private List<RawText> possiblePrices = new ArrayList<>();
    private List<RawText> possibleConclusion = new ArrayList<>();

    /**
     * Constructor, initializes variables
     * @param bitmap source photo. Not null.
     */
    public RawImage(@NonNull Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    /**
     * Must be called only once.
     * @param texts list of rawTexts. Not null.
     */
    public void setRects(@NonNull List<RawText> texts) {
        allTexts = texts;
        extendedRect = OcrUtils.getMaxRectBorders(texts);
        averageRectHeight = checkAverageHeight(texts);
    }

    /**
     * Must call setRects() before.
     * @return rect containing all rawTexts
     */
    public Rect getExtendedRect() {
        return extendedRect;
    }

    /**
     * Must call setRects() before.
     * @return average height of rawTexts
     */
    public double getAverageRectHeight() {
        return averageRectHeight;
    }

    /**
     * Must call textFitter() before.
     * @return list of Intro RawTexts
     */
    public List<RawText> getPossibleIntro() {
        return possibleIntro;
    }

    /**
     * Must call textFitter() before.
     * @return list of Products RawTexts
     */
    public List<RawText> getPossibleProducts() {
        return possibleProducts;
    }

    /**
     * Must call textFitter() before.
     * @return list of Prices RawTexts
     */
    public List<RawText> getPossiblePrices() {
        return possiblePrices;
    }

    /**
     * Must call textFitter() before.
     * @return list of Conclusion RawTexts
     */
    public List<RawText> getPossibleConclusion() {
        return possibleConclusion;
    }

    /**
     * Finds the average height in a list of rects
     * @param texts list of rawTexts. Not null.
     * @return average height
     */
    private double checkAverageHeight(@NonNull List<RawText> texts) {
        double average = 0;
        for (RawText text : texts) {
            average += text.getBoundingBox().height();
        }
        OcrUtils.log(3, "RawImage", "AVERAGE RECT HEIGHT is: " +average/texts.size());
        return average/texts.size();
    }

    /**
     * Keeps a list of all rawTexts divided by tag.
     * Must be used only once and after setRects() has been called and tags have been set.
     */
    public void textFitter() {
        for (RawText text : allTexts) {
            if (text.getTags().contains(INTRODUCTION_TAG))
                possibleIntro.add(text);
            else if (text.getTags().contains(PRODUCTS_TAG))
                possibleProducts.add(text);
            else if (text.getTags().contains(PRICES_TAG))
                possiblePrices.add(text);
            else if (text.getTags().contains(CONCLUSION_TAG))
                possibleConclusion.add(text);
        }
    }
}
