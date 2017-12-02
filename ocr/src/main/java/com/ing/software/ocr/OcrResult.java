package com.ing.software.ocr;

import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawStringResult;
import com.ing.software.ocr.OcrObjects.RawText;
import android.support.annotation.NonNull;

import java.util.List;

import static com.ing.software.ocr.OcrUtils.log;

/**
 * Structure containing the unprocessed text + metadata, extracted by OcrAnalyzer.
 */
class OcrResult {

    private List<RawStringResult> amountResults;
    private List<RawGridResult> dateList;

    /**
     * Constructor
     * @param amountResults list of possible amounts. Not null.
     * @param dateList list of possible dates. Not null.
     */
    OcrResult(@NonNull List<RawStringResult> amountResults, @NonNull List<RawGridResult> dateList) {
        this.amountResults = amountResults;
        this.dateList = dateList;
    }

    /**
     * @return possible RawTexts where amount is present
     */
    List<RawStringResult> getAmountResults() {
        return amountResults;
    }

    /**
     * @return possible RawTexts where date is present
     */
    List<RawGridResult> getDateList() {
        return dateList;
    }

    /**
     * @return String containing all results before processing by DataAnalyzer.
     * Only for debugging purposes.
     */
    public String toString() {
        StringBuilder list = new StringBuilder();
        list.append("AMOUNT FOUND IN:");
        if (amountResults != null) {
            for (RawStringResult result : amountResults) {
                List<RawText> rawTexts = result.getDetectedTexts();
                if (rawTexts != null) {
                    for (RawText text : rawTexts) {
                        list.append(text.getDetection()).append("\n");
                    }
                }
            }
        }
        list.append("\n");
        if (dateList != null) {
            for (RawGridResult result : dateList) {
                int probability = result.getPercentage();
                list.append("POSSIBLE DATE: " + result.getText().getDetection() + " with probability: " + probability);
                log(2,"POSSIBLE DATE: ", result.getText().getDetection() + " with probability: " + probability);
                list.append("\n");
            }
        }
        return list.toString();
    }
}
