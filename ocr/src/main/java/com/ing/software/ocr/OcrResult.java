package com.ing.software.ocr;

import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawImage;
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
    private RawImage image;

    /**
     * Constructor
     * @param amountResults list of possible amounts. Not null.
     * @param dateList list of possible dates. Not null.
     * @param image image of current receipt. Not null.
     */
    OcrResult(@NonNull List<RawStringResult> amountResults, @NonNull List<RawGridResult> dateList, @NonNull RawImage image) {
        this.amountResults = amountResults;
        this.dateList = dateList;
        this.image = image;
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
     * @return possible RawTexts where a product price is present
     */
    RawImage getRawImage() {
        return image;
    }

    /**
     * @return String containing all results before processing by DataAnalyzer.
     * Only for debugging purposes.
     */
    @Override
    public String toString() {
        StringBuilder list = new StringBuilder();
        list.append("AMOUNT FOUND IN:");
        if (amountResults != null) {
            for (RawStringResult result : amountResults) {
                List<RawText> rawTexts = result.getTargetTexts();
                if (rawTexts != null) {
                    for (RawText text : rawTexts) {
                        list.append(text.getValue()).append("\n");
                    }
                }
            }
        }
        list.append("\n");
        if (dateList != null) {
            for (RawGridResult result : dateList) {
                double probability = result.getPercentage();
                list.append("POSSIBLE DATE: ").append(result.getText().getValue()).append(" with probability: ")
                        .append(probability);
                log(5,"POSSIBLE DATE: ", result.getText().getValue() + " with probability: "
                        + probability + " and distance: " + DataAnalyzer.findDate(result.getText().getValue()));
                list.append("\n");
            }
        }
        return list.toString();
    }
}
