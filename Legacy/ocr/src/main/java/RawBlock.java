package com.ing.software.ocr.Legacy;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.ing.software.ocr.OperativeObjects.RawImage;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.ocr.OcrUtils.log;

/**
 * Class to store blocks detected.
 * Contains useful methods and variables that TextBlock does not provide.
 * @author Michelon
 */
@Deprecated
public class RawBlock implements Comparable<RawBlock> {

    private List<RawText> rawTexts = new ArrayList<>();
    private Rect rectF;
    private RawImage rawImage;

    /**
     * Constructor, parameters must not be null
     * @param textBlock source TextBlock. Not null.
     * @param imageMod source image. Not null.
     */
    public RawBlock(@NonNull TextBlock textBlock, @NonNull RawImage imageMod) {
        rectF = textBlock.getBoundingBox();
        this.rawImage = imageMod;
        initialize(textBlock);
    }

    /**
     * @return rect containing this block
     */
    public Rect getRectF() {
        return rectF;
    }

    /**
     * @return image containing this block
     */
    public RawImage getRawImage() {
        return rawImage;
    }

    /**
     * @return list of rawTexts contained
     */
    public List<RawText> getTexts() {
        return rawTexts;
    }

    /**
     * Populates this block with its RawTexts
     */
    private void initialize(TextBlock textBlock) {
        for (Line currentText : (List<Line>)textBlock.getComponents()) {
            rawTexts.add(new RawText(currentText, rawImage));
        }
    }

    /**
     * Search string in block, only first occurrence is returned (top -> bottom, left -> right)
     * @param string string to search. Length > 0.
     * @return RawText containing the string, null if nothing found
     */
    public RawText findFirstExact(@Size(min = 1) String string) {
        for (RawText rawText : rawTexts) {
            if (rawText.bruteSearch(string) == 0)
                return rawText;
        }
        return null;
    }

    /**
     * Search string in block, all occurrences are returned ordered(top -> bottom, left -> right)
     * @param string string to search. Length > 0.
     * @param maxDistance max distance (included) allowed for the target string. Int >= 0
     * @return list of RawStringResult containing the string with corresponding distance from target, null if nothing found
     */
    public List<RawStringResult> findContinuous(@Size(min = 1) String string, @IntRange(from = 0) int maxDistance) {
        List<RawStringResult> rawTextList = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            int distanceFromString = rawText.bruteSearch(string);
            if (distanceFromString <= maxDistance)
                rawTextList.add(new RawStringResult(rawText, distanceFromString, string));
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Find all RawTexts inside chosen rect with an error of 'percent' (on width and height of chosen rect)
     * @param rect rect where you want to find texts
     * @param percent error accepted on chosen rect. Int >= 0
     * @return list of RawTexts in chosen rect, null if nothing found
     */
    public List<RawText> findByPosition(Rect rect, @IntRange(from = 0) int percent) {
        List<RawText> rawTextList = new ArrayList<>();
        Rect newRect = extendRect(rect, percent);
        for (RawText rawText : rawTexts) {
            if (rawText.isInside(newRect)) {
                rawTextList.add(rawText);
                log(3,"OcrAnalyzer", "Found target rect: " + rawText.getValue());
            }
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Get a list of RawTexts with the probability they contain the date, non ordered
     * @return list of RawGridResult (texts + probability date is present)
     */
    /*
    public List<RawGridResult> getDateList() {
        List<RawGridResult> list = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            list.add(new RawGridResult(rawText, rawText.getDateProbability()));
        }
        log(2,"LIST_SIZE_IS", " " + list.size());
        return list;
    }
    */

    /**
     * Create a new rect extending source rect with chosen percentage (on width and height of chosen rect)
     * Note: Min value for top and left is 0
     * @param rect source rect. Not null
     * @param percent chosen percentage. Int >= 0
     * @return new extended rectangle
     */
    private Rect extendRect(@NonNull Rect rect, @IntRange(from = 0) int percent) {
        log(4, "RawObjects.extendRect","Source rect: left " + rect.left + " top: "
                + rect.top + " right: " + rect.right + " bottom: " + rect.bottom);
        int extendedHeight = rect.height()*percent/100;
        int extendedWidth = rect.width()*percent/100;
        int left = rect.left - extendedWidth/2;
        if (left<0)
            left = 0;
        int top = rect.top - extendedHeight/2;
        if (top < 0)
            top = 0;
        //Doesn't matter if bottom and right are outside the photo
        int right = rect.right + extendedWidth/2;
        int bottom = rect.bottom + extendedHeight/2;
        log(4, "RawObjects.extendRect","Extended rect: left " + left + " top: " + top
                + " right: " + right + " bottom: " + bottom);
        return new Rect(left, top, right, bottom);
    }

    @Override
    public int compareTo(@NonNull RawBlock rawBlock) {
        Rect block2Rect = rawBlock.getRectF();
        if (block2Rect.top != rectF.top)
            return Math.round(rectF.top - block2Rect.top);
        else if (block2Rect.left != rectF.left)
            return Math.round(rectF.left - block2Rect.left);
        else if (block2Rect.bottom != rectF.bottom)
            return Math.round(rectF.bottom - block2Rect.bottom);
        else
            return Math.round(rectF.right - block2Rect.right);
    }
}
