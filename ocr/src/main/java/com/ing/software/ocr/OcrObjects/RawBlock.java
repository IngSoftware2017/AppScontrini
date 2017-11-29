package com.ing.software.ocr.OcrObjects;

import android.graphics.RectF;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.ocr.OcrUtils.log;

/**
 * Class to store blocks detected.
 * Contains useful methods and variables that TextBlock does not provide.
 * @author Michelon
 */

public class RawBlock {

    private List<RawText> rawTexts = new ArrayList<>();
    private List<? extends Text> textComponents;
    private RectF rectF;
    private RawImage rawImage;

    /**
     * Constructor, parameters must not be null
     * @param textBlock source TextBlock
     * @param imageMod source image
     */
    public RawBlock(TextBlock textBlock, RawImage imageMod) {
        rectF = new RectF(textBlock.getBoundingBox());
        textComponents = textBlock.getComponents();
        this.rawImage = imageMod;
        initialize();
    }

    /**
     * Populates this block with its RawTexts
     */
    private void initialize() {
        for (Text currentText : textComponents) {
            rawTexts.add(new RawText(currentText, rawImage));
        }
    }

    /**
     * Search string in block, only first occurrence is returned (top -> bottom, left -> right)
     * @param string string to search
     * @return RawText containing the string, null if nothing found
     */
    public RawText findFirst(String string) {
        for (RawText rawText : rawTexts) {
            if (rawText.bruteSearch(string))
                return rawText;
        }
        return null;
    }

    /**
     * Search string in block, all occurrences are returned (top -> bottom, left -> right)
     * @param string string to search
     * @return list of RawText containing the string, null if nothing found
     */
    public List<RawText> findContinuous(String string) {
        List<RawText> rawTextList = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            if (rawText.bruteSearch(string))
                rawTextList.add(rawText);
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Find all RawTexts inside chosen rect with an error of 'percent' (on width and height of chosen rect)
     * @param rect rect where you want to find texts
     * @param percent error accepted on chosen rect
     * @return list of RawTexts in chosen rect, null if nothing found
     */
    public List<RawText> findByPosition(RectF rect, int percent) {
        List<RawText> rawTextList = new ArrayList<>();
        RectF newRect = extendRect(rect, percent);
        for (RawText rawText : rawTexts) {
            if (rawText.isInside(newRect)) {
                rawTextList.add(rawText);
                log("OcrAnalyzer", "Found target rect: " + rawText.getDetection());
            }
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Get a list of RawTexts with the probability they contain the date, non ordered
     * @return list of texts + probability date is present
     */
    public List<RawGridResult> getDateList() {
        List<RawGridResult> list = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            list.add(new RawGridResult(rawText, rawText.getDateProbability()));
        }
        log("LIST_SIZE_IS", " " + list.size());
        return list;
    }

    /**
     * Create a new rect extending source rect with chosen percentage (on width and height of chosen rect)
     * Note: Min value for top and left is 0
     * @param rect source rect
     * @param percent chosen percentage
     * @return new extended rectangle
     */
    private RectF extendRect(RectF rect, int percent) {
        log("RawObjects.extendRect","Source rect: left " + rect.left + " top: "
                + rect.top + " right: " + rect.right + " bottom: " + rect.bottom);
        float extendedHeight = rect.height()*percent/100;
        float extendedWidth = rect.width()*percent/100;
        float left = rect.left - extendedWidth/2;
        if (left<0)
            left = 0;
        float top = rect.top - extendedHeight/2;
        if (top < 0)
            top = 0;
        //Doesn't matter if bottom and right are outside the photo
        float right = rect.right + extendedWidth/2;
        float bottom = rect.bottom + extendedHeight/2;
        log("RawObjects.extendRect","Extended rect: left " + left + " top: " + top
                + " right: " + right + " bottom: " + bottom);
        return new RectF(left, top, right, bottom);
    }
}
