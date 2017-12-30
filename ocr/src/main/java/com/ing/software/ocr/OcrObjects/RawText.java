package com.ing.software.ocr.OcrObjects;


import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.google.android.gms.vision.text.Text;
import com.ing.software.ocr.*;

import java.util.List;
import java.util.Scanner;

import static com.ing.software.ocr.OcrUtils.log;

/**
 * Class to store texts detected.
 * Contains useful methods and variables that Text does not provide.
 * todo: make it extend Text
 * @author Michelon
 */

public class RawText implements Comparable<RawText> {

    private RectF rectText;
    private String detection;
    private RawImage rawImage;
    private RawTag tag = new RawTag();

    /**
     * Constructor
     * todo: use getCornerPoints() to ease process
     * @param text current Text inside TextBlock. Not null.
     * @param rawImage source image. Not null.
     */
    public RawText(@NonNull Text text, @NonNull RawImage rawImage) {
        rectText = new RectF(text.getBoundingBox());
        this.detection = text.getValue();
        this.rawImage = rawImage;
    }

    /**
     * @return string contained in this Text
     */
    public String getDetection() {
        return detection;
    }

    /**
     * @return rect of this Text
     */
    public RectF getRect() {
        return rectText;
    }

    /**
     * @return rawImage of this Text
     */
    public RawImage getRawImage() {
        return rawImage;
    }

    public List<String> getTags() {
        return tag.getTag();
    }

    public void addTag(String tag) {
        this.tag.addTag(tag);
    }

    public void removeTag(String tag) {
        this.tag.removeTag(tag);
    }

    /**
     * Retrieves probability that date is present in current text
     * @return probability that date is present
     */
    public int getDateProbability() {
        log(8,"Value is: ", getDetection());
        int[] gridBox = getGridBox();
        log(8,"Grid box is: ", " " + gridBox[1] + ":" + gridBox[0]);
        int probability = ProbGrid.dateMap.get(rawImage.getGrid())[gridBox[1]][gridBox[0]];
        log(8,"Date Probability is", " " +probability);
        return probability;
    }

    /**
     * Retrieves probability that amount is present in current text
     * @return probability that amount is present
     */
    public int getAmountProbability() {
        log(8,"Value is: ", getDetection());
        int[] gridBox = getGridBox();
        log(8,"Grid box is: ", " " + gridBox[1] + ":" + gridBox[0]);
        int probability = ProbGrid.amountMap.get(rawImage.getGrid())[gridBox[1]][gridBox[0]];
        log(8,"Amount Probability is", " " +probability);
        return probability;
    }

    /**
     * Find box of the grid containing the center of the text rect
     * todo: replace with point
     * @return coordinates of the grid, where int[0] = column, int[1] = row
     */
    public int[] getGridBox() {
        Scanner gridder = new Scanner(rawImage.getGrid());
        gridder.useDelimiter("x");
        int rows = Integer.parseInt(gridder.next());
        int columns = Integer.parseInt(gridder.next());
        gridder.close();
        double rowsHeight = rawImage.getHeight()/rows;
        double columnsWidth = rawImage.getWidth()/columns;
        int gridX = (int) (rectText.centerX()/columnsWidth);
        int gridY = (int) (rectText.centerY()/rowsHeight);
        return new int[] {gridX, gridY};
    }

    /**
     * Search string in text
     * @param string string to search. Length > 0.
     * @param maxDistance max distance (included) allowed for the target string. Int >= 0
     * @return RawStringResult containing the string with corresponding distance from target, null if nothing found
     */
    public RawStringResult findContinuous(@Size(min = 1) String string, @IntRange(from = 0) int maxDistance) {
        int distanceFromString = bruteSearch(string);
        if (distanceFromString <= maxDistance)
            return new RawStringResult(this, distanceFromString, string);
        else
            return null;
    }

    /**
     * Search string in text
     * @param string string to search. Length > 0.
     * @return int according to OcrUtils.findSubstring()
     */
    int bruteSearch(@Size(min = 1) String string) {
        return OcrUtils.findSubstring(getDetection(), string);
    }

    /**
     * Check if this text is inside chosen rect
     * @param rect target rect that could contain this text. Not null.
     * @return true if is inside
     */
    public boolean isInside(@NonNull RectF rect) {
        return rect.contains(rectText);
    }

    /**
     * Order: top to bottom, left to right
     * @param rawText target RawText
     * @return int > 0 if target comes before source (i.e. is above/on the left)
     */
    @Override
    public int compareTo(@NonNull RawText rawText) {
        RectF text2Rect = rawText.getRect();
        if (text2Rect.top != rectText.top)
            return Math.round(rectText.top - text2Rect.top);
        else if (text2Rect.left != rectText.left)
            return Math.round(rectText.left - text2Rect.left);
        else if (text2Rect.bottom != rectText.bottom)
            return Math.round(rectText.bottom - text2Rect.bottom);
        else
            return Math.round(rectText.right - text2Rect.right);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RawText))return false;
        RawText target = (RawText) other;
        return this.compareTo(target) == 0;
    }
}
