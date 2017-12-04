package com.ing.software.ocr.OcrObjects;


import android.support.annotation.NonNull;


import java.util.List;

/**
 * Class to store results from string search
 * @author Michelon
 */

public class RawStringResult implements Comparable<RawStringResult>{

    private RawText sourceText;
    private String sourceString;
    private int distanceFromTarget;
    private List<RawText> detectedTexts = null;

    /**
     * Constructor. Set source rawText and its distance from target string
     * @param rawText source RawText
     * @param distanceFromTarget distance from target String
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
        if (this.detectedTexts == null)
            this.detectedTexts = detectedTexts;
        else
            this.detectedTexts.addAll(detectedTexts);
    }

    public RawText getSourceText() {
        return sourceText;
    }

    public int getDistanceFromTarget() {
        return distanceFromTarget;
    }

    public List<RawText> getDetectedTexts() {
        return detectedTexts;
    }

    public String getSourceString() {
        return sourceString;
    }

    @Override
    public int compareTo(@NonNull RawStringResult rawStringResult) {
        if (distanceFromTarget != rawStringResult.getDistanceFromTarget())
            return distanceFromTarget - rawStringResult.getDistanceFromTarget();
        else
            return -1; //Follow order from AMOUNT_STRINGS array
    }
}
