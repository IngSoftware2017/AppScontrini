package com.ing.software.ocr.OcrObjects;


import android.support.annotation.NonNull;


import java.util.ArrayList;
import java.util.List;

/**
 * Class to store results from string search
 * @author Michelon
 */

public class RawStringResult implements Comparable<RawStringResult>{

    private RawText sourceText;
    private String sourceString;
    private int distanceFromTarget;
    private List<RawText> targetTexts = new ArrayList<>();
    private List<RawGridResult> detectedTexts = new ArrayList<>();

    /**
     * Constructor. Set source rawText and its distance from target string
     * @param rawText source RawText
     * @param distanceFromTarget distance from target String
     * @param sourceString source string
     */
    RawStringResult(RawText rawText, int distanceFromTarget, String sourceString) {
        this.sourceString = sourceString;
        this.sourceText = rawText;
        this.distanceFromTarget = distanceFromTarget;
    }

    /**
     * Adds rawTexts found in extended rect
     * @param detectedTexts list of rawTexts detected. Not null.
     */
    public void addDetectedTexts(@NonNull List<RawText> detectedTexts) {
        targetTexts.addAll(detectedTexts);
        for (RawText text : detectedTexts)
            addDetectedTexts(text);
    }

    /**
     * Adds rawText found in extended rect
     * @param detectedText rawText detected. Not null.
     */
    public void addDetectedTexts(@NonNull RawText detectedText) {
        targetTexts.add(detectedText);
        double heightDiff = Math.abs(detectedText.getBoundingBox().height() - sourceText.getBoundingBox().height())/sourceText.getBoundingBox().height();
        //order in RawGridResult is from higher to lower, so we invert the order, heightDiff is between 0 and 1
        heightDiff = (1 - heightDiff)*100;
        RawGridResult singleResult = new RawGridResult(detectedText, heightDiff);
        this.detectedTexts.add(singleResult);
    }

    public List<RawText> getTargetTexts() {
        return targetTexts;
    }

    public RawText getSourceText() {
        return sourceText;
    }

    public int getDistanceFromTarget() {
        return distanceFromTarget;
    }

    public List<RawGridResult> getDetectedTexts() {
        return detectedTexts;
    }

    public String getSourceString() {
        return sourceString;
    }

    @Override
    public int compareTo(@NonNull RawStringResult rawStringResult) {
        return distanceFromTarget - rawStringResult.getDistanceFromTarget();
    }
}
