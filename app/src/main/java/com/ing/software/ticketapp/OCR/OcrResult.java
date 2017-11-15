package com.ing.software.ticketapp.OCR;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Structure containing the unprocessed text + metadata, extracted by OcrAnalyzer.
 */
public class OcrResult {

    private List<RawResult> amountResults;
    private HashMap<RawBlock.RawText, Integer> dateMap;

    OcrResult() {
    }

    void setAmountResults(List<RawResult> amountResults) {
        this.amountResults = amountResults;
    }

    void setDateMap(HashMap<RawBlock.RawText, Integer> dateMap) {
        this.dateMap = dateMap;
    }

    List<RawResult> getAmountResults() {
        return amountResults;
    }

    HashMap<RawBlock.RawText, Integer> getDateMap() {
        return dateMap;
    }

    public String toString() {
        StringBuilder list = new StringBuilder();
        list.append("AMOUNT FOUND IN:");
        for (RawResult result : amountResults) {
            List<RawBlock.RawText> rawTexts = result.getDetectedTexts();
            if (rawTexts != null)
            {
                for (RawBlock.RawText text : rawTexts) {
                    list.append(text.getDetection()).append("\n");
                }
            }
        }
        list.append("\n");
        /*
        List<RawBlock.RawText> datesTexts = new ArrayList<>(dateMap.keySet());
        List<Integer> probList = new ArrayList<>(dateMap.values());
        for (int i = 0; i < dateMap.size(); ++i) {
            RawBlock.RawText text = datesTexts.get(i);
            int probability = probList.get(i);
            list.append("POSSIBLE DATE: " + text.getDetection() + " with probability: " + probability);
            Log.d("POSSIBLE DATE: ", text.getDetection() + " with probability: " + probability);
            list.append("\n");
        }
        */
        List<RawBlock.RawText> datesTexts = new ArrayList<>(dateMap.keySet());
        for (RawBlock.RawText text : datesTexts) {
            int probability = dateMap.get(text);
            list.append("POSSIBLE DATE: " + text.getDetection() + " with probability: " + probability);
            Log.d("POSSIBLE DATE: ", text.getDetection() + " with probability: " + probability);
            list.append("\n");
        }
        return list.toString();
    }
}
