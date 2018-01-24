package com.ing.software.ocr.Legacy;

import android.support.annotation.NonNull;

/**
 * Class to store results from grid search or for other searches
 * where you need only a RawText and an int.
 * This object is used only for comparing purposes.
 * @author Michelon
 */

public class RawGridResult implements Comparable<RawGridResult>{

    private double percentage;
    private RawText singleText;

    /**
     * Constructor
     * @param singleText source text
     * @param percentage percentage corresponding to this text
     */
    public RawGridResult(RawText singleText, double percentage) {
        this.percentage = percentage;
        this.singleText = singleText;
    }

    /**
     * @return percentage of this rawText
     */
    public double getPercentage() {
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
        double diff = rawGridResult.getPercentage() - getPercentage();
        if (diff > 0)
            return 1;
        else if (diff < 0)
            return -1;
        else
            return (int) diff;
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
