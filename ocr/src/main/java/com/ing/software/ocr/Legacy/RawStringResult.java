package com.ing.software.ocr.Legacy;


import android.support.annotation.NonNull;


import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.ing.software.ocr.OcrVars.*;

/**
 * Class to store results from string search
 * @author Michelon
 */
@Deprecated
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
        double heightDiff = ((double)Math.abs(detectedText.getBoundingBox().height() - sourceText.getBoundingBox().height()))/sourceText.getBoundingBox().height();
        //order in RawGridResult is from higher to lower, so we invert the order, heightDiff is between 0 and 1
        OcrUtils.log(5, "addDetectedTexts", "Rect is: " + detectedText.getValue() + " height diff is: " + heightDiff);
        heightDiff = (1 - heightDiff)*HEIGHT_SOURCE_DIFF_MULTIPLIER;
        OcrUtils.log(5, "addDetectedTexts", "Evaluated height diff is: " + heightDiff);
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
        Collections.sort(detectedTexts);
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
