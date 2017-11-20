package com.ing.software.ticketapp.OCR;


import java.util.List;

import static com.ing.software.ticketapp.OCR.OcrUtils.log;

/**
 * Structure containing the unprocessed text + metadata, extracted by OcrAnalyzer.
 */
class OcrResult {

    private List<RawStringResult> amountResults;
    private List<RawGridResult> dateList;

    OcrResult() {
    }

    /**
     * Set the possible RawTexts where amount is present
     * @param amountResults possible RawTexts where amount is present
     */
    void setAmountResults(List<RawStringResult> amountResults) {
        this.amountResults = amountResults;
    }

    /**
     * Set the possible RawTexts where date is present
     * @param dateList possible RawTexts where date is present
     */
    void setDateList(List<RawGridResult> dateList) {
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
                List<RawBlock.RawText> rawTexts = result.getDetectedTexts();
                if (rawTexts != null) {
                    for (RawBlock.RawText text : rawTexts) {
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
                log("POSSIBLE DATE: ", result.getText().getDetection() + " with probability: " + probability);
                list.append("\n");
            }
        }
        return list.toString();
    }
}
