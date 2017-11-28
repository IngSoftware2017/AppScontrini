package com.ing.software.ticketapp.OCR.OcrObjects;

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
        if (getPercentage() == rawGridResult.getPercentage())
            return getText().compareTo(rawGridResult.getText());
        else
            return  rawGridResult.getPercentage() - getPercentage();
    }
}
