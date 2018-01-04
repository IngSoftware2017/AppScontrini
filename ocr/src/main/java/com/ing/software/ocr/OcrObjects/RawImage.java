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
     * @param texts
     */
    public void setRects(List<RawText> texts) {
        allTexts = texts;
        extendedRect = OcrUtils.getMaxRectBorders(texts);
        averageRectHeight = checkAverageHeight(texts);
    }

    public Rect getExtendedRect() {
        return extendedRect;
    }

    public double getAverageRectHeight() {
        return averageRectHeight;
    }

    public List<RawText> getPossibleIntro() {
        return possibleIntro;
    }

    public List<RawText> getPossibleProducts() {
        return possibleProducts;
    }

    public List<RawText> getPossiblePrices() {
        return possiblePrices;
    }

    public List<RawText> getPossibleConclusion() {
        return possibleConclusion;
    }

    private double checkAverageHeight(List<RawText> texts) {
        double average = 0;
        for (RawText text : texts) {
            average += text.getBoundingBox().height();
        }
        return average/texts.size();
    }

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
