package com.ing.software.ocr.OperativeObjects;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.ing.software.ocr.OcrManager;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ing.software.ocr.OcrVars.*;

/**
 * Class to store only useful properties of source images and scheme of ticket
 * How to use:
 * - initialize with constructor
 * - call setLines() passing a list of all Texts
 * - call OcrSchemer.prepareScheme()
 * - now you can use the others methods in this class
 * @author Michelon
 */

public class RawImage {

    private int height;
    private int width;
    private RectF extendedRect;
    private float averageRectHeight;
    private float averageCharHeight;
    private float averageCharWidth;
    private RectF introRect;
    private RectF productsRect;
    private RectF pricesRect;
    private RectF conclusionRect;
    private List<OcrText> allTexts = new ArrayList<>();
    private List<OcrText> introTexts = new ArrayList<>();
    private List<OcrText> productsTexts = new ArrayList<>();
    private List<OcrText> pricesTexts = new ArrayList<>();
    private List<OcrText> conclusionTexts = new ArrayList<>();

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
    public void setLines(@NonNull List<OcrText> texts) {
        allTexts = texts;
        extendedRect = getMaxRectBorders(texts);
        averageRectHeight = checkAverageLineHeight();
        averageCharHeight = checkAverageCharHeight();
        averageCharWidth = checkAverageCharWidth();
    }

    /**
     * Must call setLines() before.
     * @return rect containing all texts
     */
    public RectF getExtendedRect() {
        return extendedRect;
    }

    /**
     * Must call setLines() before.
     * @return average rect height of texts
     */
    public double getAverageRectHeight() {
        return averageRectHeight;
    }

    /**
     * Must call setLines() before.
     * @return average char height of texts
     */
    public double getAverageCharHeight() {
        return averageCharHeight;
    }

    /**
     * Must call setLines() before.
     * @return average char width of texts
     */
    public double getAverageCharWidth() {
        return averageCharWidth;
    }

    /**
     * Must call setLines() before.
     * @return list of all texts
     */
    public List<OcrText> getAllTexts() {
        return allTexts;
    }

    /**
     * Must call textFitter() before.
     * @return list of Intro Texts
     */
    public List<OcrText> getIntroTexts() {
        Collections.sort(introTexts);
        return introTexts;
    }

    /**
     * Must call textFitter() before.
     * @return rect containing all Intro texts.
     */
    public RectF getIntroRect() {
        return introRect != null? introRect : new RectF(0, 0, width, 0);
    }

    /**
     * Must call textFitter() before.
     * @return list of Products Texts
     */
    public List<OcrText> getProductsTexts() {
        Collections.sort(productsTexts);
        return productsTexts;
    }

    /**
     * Must call textFitter() before.
     * @return rect containing all Products texts.
     */
    public RectF getProductsRect() {
        return productsRect != null? productsRect : new RectF(0, getIntroRect().bottom, width/2, getConclusionRect().top);
    }

    /**
     * Must call textFitter() before.
     * @return list of Prices Texts
     */
    public List<OcrText> getPricesTexts() {
        Collections.sort(pricesTexts);
        return pricesTexts;
    }

    /**
     * Must call textFitter() before.
     * @return rect containing all Prices texts.
     */
    public RectF getPricesRect() {
        return pricesRect != null? pricesRect : new RectF(width/2, getIntroRect().bottom, width, getConclusionRect().top);
    }

    /**
     * Must call textFitter() before.
     * @return list of Conclusion Texts
     */
    public List<OcrText> getConclusionTexts() {
        Collections.sort(conclusionTexts);
        return conclusionTexts;
    }

    /**
     * Must call textFitter() before.
     * @return rect containing all Conclusion texts.
     */
    public RectF getConclusionRect() {
        return conclusionRect != null? conclusionRect : new RectF(0, height, width, height);
    }

    /**
     * Finds the average height in a list of lines
     * @return average height
     */
    private float checkAverageLineHeight() {
        float average = Stream.of(allTexts)
                .reduce(0f, (sum, currentLine) -> sum + currentLine.height()) / allTexts.size();
        OcrUtils.log(5, "RawImage", "AVERAGE RECT HEIGHT is: " + average);
        return average;
    }

    /**
     * Finds the average char height in a list of lines
     * @return average char height
     */
    private float checkAverageCharHeight() {
        float average = Stream.of(allTexts)
                .reduce(0f, (sum, currentLine) -> sum + currentLine.charHeight()) / allTexts.size();
        OcrUtils.log(5, "RawImage", "AVERAGE CHAR HEIGHT is: " + average);
        return average;
    }

    /**
     * Finds the average char width in a list of lines
     * @return average char width
     */
    private float checkAverageCharWidth() {
        float average = Stream.of(allTexts)
                .reduce(0f, (sum, currentLine) -> sum + currentLine.charWidth()) / allTexts.size();
        OcrUtils.log(5, "RawImage", "AVERAGE CHAR WIDTH is: " + average);
        return average;
    }

    /**
     * Keeps a list of all rawTexts divided by tag.
     * Must be used only once and after setLines() has been called and tags have been set (OcrSchemer.prepareScheme).
     */
    public void textFitter() {
        //Remove old lists
        introTexts = new ArrayList<>();
        pricesTexts = new ArrayList<>();
        productsTexts = new ArrayList<>();
        conclusionTexts = new ArrayList<>();
        for (OcrText text : allTexts) {
            if (text.getTags().contains(INTRODUCTION_TAG))
                introTexts.add(text);
            else if (text.getTags().contains(PRODUCTS_TAG))
                productsTexts.add(text);
            else if (text.getTags().contains(PRICES_TAG))
                pricesTexts.add(text);
            else if (text.getTags().contains(CONCLUSION_TAG))
                conclusionTexts.add(text);
        }
        introRect = getMaxRectBorders(introTexts);
        productsRect = getMaxRectBorders(productsTexts);
        pricesRect = getMaxRectBorders(pricesTexts);
        conclusionRect = getMaxRectBorders(conclusionTexts);
    }

    /**
     * Get rect containing all lines detected
     * @return Rect containing all texts passed
     */
    private RectF getMaxRectBorders(List<OcrText> texts) {
        if (texts.size() == 0)
            return null;
        float left = width;
        float right = 0;
        float top = height;
        float bottom = 0;
        for (OcrText text : texts) {
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

    /**
     * Override a text with a new one
     * @param oldText old text to remove
     * @param newText new text to insert
     */
    public void overrideText(OcrText oldText, OcrText newText) {
        allTexts.remove(oldText);
        allTexts.add(newText);
        //fail silently
        introTexts.remove(oldText);
        pricesTexts.remove(oldText);
        productsTexts.remove(oldText);
        conclusionTexts.remove(oldText);
        if (newText.getTags().contains(INTRODUCTION_TAG)) {
            introTexts.add(newText);
            introRect = getMaxRectBorders(introTexts);
        } else if (newText.getTags().contains(PRODUCTS_TAG)) {
            productsTexts.add(newText);
            productsRect = getMaxRectBorders(productsTexts);
        } else if (newText.getTags().contains(PRICES_TAG)) {
            pricesTexts.add(newText);
            pricesRect = getMaxRectBorders(pricesTexts);
        } else if (newText.getTags().contains(CONCLUSION_TAG)) {
            conclusionTexts.add(newText);
            conclusionRect = getMaxRectBorders(conclusionTexts);
        }
    }

    /**
     * Remove a text from all lists
     * @param oldText text to remove
     */
    public void removeText(OcrText oldText) {
        allTexts.remove(oldText);
        //fail silently
        introTexts.remove(oldText);
        pricesTexts.remove(oldText);
        productsTexts.remove(oldText);
        conclusionTexts.remove(oldText);
        introRect = getMaxRectBorders(introTexts);
        productsRect = getMaxRectBorders(productsTexts);
        pricesRect = getMaxRectBorders(pricesTexts);
        conclusionRect = getMaxRectBorders(conclusionTexts);
    }

    /**
     * Remove all texts inside passed rect from all lists
     * @param rect rect containing texts to remove
     */
    public void removeText(RectF rect) {
        List<OcrText> tempList = new ArrayList<>(allTexts);
        for (OcrText text : tempList) {
            if (rect.contains(text.box())) {
                allTexts.remove(text);
                introTexts.remove(text);
                pricesTexts.remove(text);
                productsTexts.remove(text);
                conclusionTexts.remove(text);
            }
        }
        //fail silently
        introRect = getMaxRectBorders(introTexts);
        productsRect = getMaxRectBorders(productsTexts);
        pricesRect = getMaxRectBorders(pricesTexts);
        conclusionRect = getMaxRectBorders(conclusionTexts);
    }

    /**
     * Add text to lists
     * @param newText text to add according to its tag
     */
    public void addText(OcrText newText) {
        allTexts.add(newText);
        if (newText.getTags().contains(INTRODUCTION_TAG)) {
            introTexts.add(newText);
            introRect = getMaxRectBorders(introTexts);
        } else if (newText.getTags().contains(PRODUCTS_TAG)) {
            productsTexts.add(newText);
            productsRect = getMaxRectBorders(productsTexts);
        } else if (newText.getTags().contains(PRICES_TAG)) {
            pricesTexts.add(newText);
            pricesRect = getMaxRectBorders(pricesTexts);
        } else if (newText.getTags().contains(CONCLUSION_TAG)) {
            conclusionTexts.add(newText);
            conclusionRect = getMaxRectBorders(conclusionTexts);
        }
    }

    /**
     * Add tag to text according to its position (must call textFitter before)
     * @param text text to tag
     * @return text with added tag
     */
    public static OcrText mapText(OcrText text) {
        if (OcrManager.mainImage.getIntroRect().contains(text.box()))
            text.addTag(INTRODUCTION_TAG);
        else if (OcrManager.mainImage.getProductsRect().contains(text.box()))
            text.addTag(PRODUCTS_TAG);
        else if (OcrManager.mainImage.getPricesRect().contains(text.box()))
            text.addTag(PRICES_TAG);
        else if (OcrManager.mainImage.getConclusionRect().contains(text.box()))
            text.addTag(CONCLUSION_TAG);
        else {
            float productsTop = Math.min(OcrManager.mainImage.getProductsRect().top, OcrManager.mainImage.getPricesRect().top);
            float productsBottom = Math.max(OcrManager.mainImage.getProductsRect().bottom, OcrManager.mainImage.getPricesRect().bottom);
            RectF middleRect = new RectF(OcrManager.mainImage.getProductsRect().left, productsTop, OcrManager.mainImage.getPricesRect().right, productsBottom);
            //Check its y coordinate
            float centerY = text.box().centerY();
            if (centerY < productsTop)
                text.addTag(INTRODUCTION_TAG);
            else if (centerY > productsBottom)
                text.addTag(CONCLUSION_TAG);
            else {
                if (text.box().centerX() < middleRect.width()/2)
                    text.addTag(PRODUCTS_TAG);
                else
                    text.addTag(PRICES_TAG);
            }
        }
        return text;
    }
}
