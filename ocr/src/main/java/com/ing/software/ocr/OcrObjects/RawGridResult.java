package com.ing.software.ocr.OcrObjects;

import android.support.annotation.NonNull;

/**
 * Class to store results from grid search
 * This object is used only for comparing purposes
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
        //if (getPercentage() == rawGridResult.getPercentage())
            //return getText().compareTo(rawGridResult.getText());
        //    return -1;
       // else
            return  rawGridResult.getPercentage() - getPercentage();
    }

    @Override
    public boolean equals(@NonNull Object o) {
        if (o instanceof RawGridResult) {
            RawGridResult e2 = (RawGridResult) o;
            return singleText.getDetection().equals(e2.getText().getDetection()) &&
                    singleText.getRect().equals(e2.getText().getRect());
        }
        else
            return false;
    }
}
