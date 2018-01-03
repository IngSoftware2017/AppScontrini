package com.ing.software.ocr.OcrObjects;

import android.support.annotation.NonNull;

/**
 * Class to store results from grid search or for other searches
 * where you need only a RawText and an int.
 * This object is used only for comparing purposes.
 * @author Michelon
 */

public class RawGridResult implements Comparable<RawGridResult>{

    private int percentage;
    private RawText singleText;

    /**
     * Constructor
     * @param singleText source text
     * @param percentage percentage corresponding to this text
     */
    public RawGridResult(RawText singleText, int percentage) {
        this.percentage = percentage;
        this.singleText = singleText;
    }

    /**
     * @return percentage of this rawText
     */
    public int getPercentage() {
        return percentage;
    }

    /**
     * @return this rawText
     */
    public RawText getText() {
        return singleText;
    }

    @Override
    public int compareTo(@NonNull RawGridResult rawGridResult) {
        return  rawGridResult.getPercentage() - getPercentage();
    }

    @Override
    public boolean equals(@NonNull Object o) {
        if (o instanceof RawGridResult) {
            RawGridResult e2 = (RawGridResult) o;
            return singleText.getValue().equals(e2.getText().getValue()) &&
                    singleText.getBoundingBox().equals(e2.getText().getBoundingBox());
        }
        else
            return false;
    }
}
