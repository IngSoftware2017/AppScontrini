package com.ing.software.ticketapp.OCR;

import android.util.Log;
import java.util.List;

/**
 * Structure containing the unprocessed text + metadata, extracted by OcrAnalyzer.
 */
class OcrResult {

    private List<RawStringResult> amountResults;
    private List<RawGridResult> dateList;

    OcrResult() {
    }

    void setAmountResults(List<RawStringResult> amountResults) {
        this.amountResults = amountResults;
    }

    void setDateList(List<RawGridResult> dateList) {
        this.dateList = dateList;
    }

    List<RawStringResult> getAmountResults() {
        return amountResults;
    }

    List<RawGridResult> getDateList() {
        return dateList;
    }

    public String toString() {
        StringBuilder list = new StringBuilder();
        list.append("AMOUNT FOUND IN:");
        for (RawStringResult result : amountResults) {
            List<RawBlock.RawText> rawTexts = result.getDetectedTexts();
            if (rawTexts != null)
            {
                for (RawBlock.RawText text : rawTexts) {
                    list.append(text.getDetection()).append("\n");
                }
            }
        }
        list.append("\n");
        for (RawGridResult result : dateList) {
            int probability = result.getPercentage();
            list.append("POSSIBLE DATE: " + result.getText().getDetection() + " with probability: " + probability);
            Log.d("POSSIBLE DATE: ", result.getText().getDetection() + " with probability: " + probability);
            list.append("\n");
        }
        return list.toString();
    }
}
