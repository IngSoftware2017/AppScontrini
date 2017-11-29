package com.ing.software.ocr.OcrObjects;

import android.support.annotation.NonNull;

/**
 * Class to store results from grid search
 */

public class RawGridResult implements Comparable<RawGridResult>{

    private int percentage;
    private RawText singleText;

    RawGridResult(RawText singleText, int percentage) {
        this.percentage = percentage;
        this.singleText = singleText;
    }

    public int getPercentage() {
        return percentage;
    }

    public RawText getText() {
        return singleText;
    }

    @Override
    public int compareTo(@NonNull RawGridResult rawGridResult) {
        if (getPercentage() == rawGridResult.getPercentage())
            return getText().compareTo(rawGridResult.getText());
        else
            return rawGridResult.getPercentage() - getPercentage();
    }
}
