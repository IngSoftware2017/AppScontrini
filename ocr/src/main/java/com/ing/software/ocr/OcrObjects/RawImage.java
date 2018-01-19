package com.ing.software.ocr.OcrObjects;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
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
    private RectF extendedRect;
    private float averageRectHeight;
    private float averageCharHeight;
    private float averageCharWidth;
    private List<TempText> allTexts = new ArrayList<>();
    private List<TempText> introTexts = new ArrayList<>();
    private List<TempText> productsTexts = new ArrayList<>();
    private List<TempText> pricesTexts = new ArrayList<>();
    private List<TempText> conclusionTexts = new ArrayList<>();

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
    public void setLines(@NonNull List<TempText> texts) {
        allTexts = texts;
        extendedRect = new RectF(getMaxRectBorders());
        averageRectHeight = checkAverageLineHeight();
    }

    /**
     * Must call setLines() before.
     * @return rect containing all rawTexts
     */
    public RectF getExtendedRect() {
        return extendedRect;
    }

    /**
     * Must call setLines() before.
     * @return average height of rawTexts
     */
    public double getAverageRectHeight() {
        return averageRectHeight;
    }

    /**
     * Must call textFitter() before.
     * @return list of Intro RawTexts
     */
    public List<TempText> getIntroTexts() {
        return introTexts;
    }

    /**
     * Must call textFitter() before.
     * @return list of Products RawTexts
     */
    public List<TempText> getProductsTexts() {
        return productsTexts;
    }

    /**
     * Must call textFitter() before.
     * @return list of Prices RawTexts
     */
    public List<TempText> getPricesTexts() {
        return pricesTexts;
    }

    /**
     * Must call textFitter() before.
     * @return list of Conclusion RawTexts
     */
    public List<TempText> getConclusionTexts() {
        return conclusionTexts;
    }

    /**
     * Finds the average height in a list of lines
     * @return average height
     */
    private float checkAverageLineHeight() {
        float average = Stream.of(allTexts).reduce(0f, (sum, currentLine) ->
                sum + currentLine.height()) / allTexts.size();
        OcrUtils.log(3, "RawImage", "AVERAGE RECT HEIGHT is: " + average);
        return average;
    }

    /**
     * Finds the average char height in a list of lines
     * @return average char height
     */
    private float checkAverageCharHeight() {
        float average = Stream.of(allTexts).reduce(0f, (sum, currentLine) ->
                sum + currentLine.charHeight()) / allTexts.size();
        OcrUtils.log(3, "RawImage", "AVERAGE CHAR HEIGHT is: " + average);
        return average;
    }

    /**
     * Finds the average char width in a list of lines
     * @return average char width
     */
    private float checkAverageCharWidth() {
        float average = Stream.of(allTexts).reduce(0f, (sum, currentLine) ->
                sum + currentLine.charWidth()) / allTexts.size();
        OcrUtils.log(3, "RawImage", "AVERAGE CHAR WIDTH is: " + average);
        return average;
    }

    /**
     * Keeps a list of all rawTexts divided by tag.
     * Must be used only once and after setLines() has been called and tags have been set.
     */
    public void textFitter() {
        for (TempText text : allTexts) {
            if (text.getTags().contains(INTRODUCTION_TAG))
                introTexts.add(text);
            else if (text.getTags().contains(PRODUCTS_TAG))
                productsTexts.add(text);
            else if (text.getTags().contains(PRICES_TAG))
                pricesTexts.add(text);
            else if (text.getTags().contains(CONCLUSION_TAG))
                conclusionTexts.add(text);
        }
    }

    /**
     * Get rect containing all lines detected
     * @return Rect containing all rects passed
     */
    private RectF getMaxRectBorders() {
        if (allTexts.size() == 0)
            return null;
        float left = width;
        float right = 0;
        float top = height;
        float bottom = 0;
        for (TempText text : allTexts) {
            if (text.box().left < left)
                left = text.box().left;
            if (text.box().right > right)
                right = text.box().right;
            if (text.box().bottom > bottom)
                bottom = text.box().bottom;
            if (text.box().top < top)
                top = text.box().top;
        }
        return new RectF(left, top, right, bottom);
    }
}
