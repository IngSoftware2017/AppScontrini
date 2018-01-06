package com.ing.software.ocr.OcrObjects;


import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Element;
import com.ing.software.ocr.*;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.ocr.OcrUtils.log;
import static com.ing.software.ocr.OcrVars.*;

/**
 * Class to store texts detected.
 * Contains useful methods and variables that Text does not provide.
 * todo: make it extend Text
 * @author Michelon
 */

public class RawText implements Comparable<RawText>, Text {

    private Rect rectText;
    private String detection;
    private RawImage rawImage;
    private List<String> tags = new ArrayList<>();
    private int position;
    private Line line;
    private Element word;

    /**
     * Constructor
     * todo: use getCornerPoints() to ease process
     * @param line current Text inside TextBlock. Not null.
     * @param rawImage source image. Not null.
     */
    public RawText(@NonNull Text line, @NonNull RawImage rawImage) {
        if (line instanceof Line)
            this.line = (Line)line;
        else if (line instanceof Element)
            this.word = (Element)line;
        rectText = line.getBoundingBox();
        detection = line.getValue();
        this.rawImage = rawImage;
    }

    /**
     * @return string contained in this Text
     */
    @Override
    public String getValue() {
        return detection;
    }

    /**
     * @return rect of this Text
     */
    @Override
    public Rect getBoundingBox() {
        return rectText;
    }

    /**
     * @return rawImage of this Text
     */
    public RawImage getRawImage() {
        return rawImage;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag))
            this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public void setTagPosition(double position) {
        this.position = (int)(position*10);
    }

    /**
     * Retrieves probability that date is present in current text
     * @return probability that date is present
     */
    public double getDateProbability() {
        int probability = 0;
        if (tags.contains(INTRODUCTION_TAG))
            probability = ProbGrid.dateBlockIntroduction[position];
        else if (tags.contains(PRODUCTS_TAG) || tags.contains(PRICES_TAG))
            probability = ProbGrid.dateBlockProducts[position];
        else if (tags.contains(CONCLUSION_TAG))
            probability = ProbGrid.dateBlockConclusion[position];
        log(8,"RawText.getDateProb", "Date Probability is " + probability);
        return probability;
    }

    /**
     * Retrieves probability that amount is present in current text
     * @return probability that amount is present
     */
    public double getAmountProbability() {
        double probability = 0;
        if (tags.contains(INTRODUCTION_TAG))
            probability = ProbGrid.amountBlockIntroduction[position];
        else if (tags.contains(PRODUCTS_TAG) || tags.contains(PRICES_TAG))
            probability = ProbGrid.amountBlockProducts[position];
        else if (tags.contains(CONCLUSION_TAG))
            probability = ProbGrid.amountBlockConclusion[position];
        log(8,"RawText.getAmountProb", "Amount Probability from grid is " + probability);
        probability += ProbGrid.getRectHeightScore(this);
        return probability;
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
        return OcrUtils.findSubstring(getValue(), string);
    }

    /**
     * Check if this text is inside chosen rect
     * @param rect target rect that could contain this text. Not null.
     * @return true if is inside
     */
    public boolean isInside(@NonNull Rect rect) {
        return rect.contains(rectText);
    }

    /**
     * Order: top to bottom, left to right
     * @param rawText target RawText
     * @return int > 0 if target comes before source (i.e. is above/on the left)
     */
    @Override
    public int compareTo(@NonNull RawText rawText) {
        Rect text2Rect = rawText.getBoundingBox();
        if (text2Rect.top != rectText.top)
            return rectText.top - text2Rect.top;
        else if (text2Rect.left != rectText.left)
            return rectText.left - text2Rect.left;
        else if (text2Rect.bottom != rectText.bottom)
            return rectText.bottom - text2Rect.bottom;
        else
            return rectText.right - text2Rect.right;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RawText))return false;
        RawText target = (RawText) other;
        return this.compareTo(target) == 0;
    }

    @Override
    public Point[] getCornerPoints() {
        return new Point[] {new Point(rectText.left, rectText.top), new Point(rectText.right, rectText.top),
        new Point(rectText.right, rectText.bottom), new Point(rectText.left, rectText.bottom)};
    }

    @Override
    public List<? extends Text> getComponents() {
        if (line != null)
            return line.getComponents();
        else
            return word.getComponents();
    }
}
