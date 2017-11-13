package com.ing.software.ticketapp.OCR;


import java.util.List;

/**
 * Structure containing the unprocessed text + metadata, extracted by OcrAnalyzer.
 */
public class OcrResult {

    private List<RawBlock.RawText> rawTexts;

    public OcrResult(List<RawBlock.RawText> detectedTexts) {
        this.rawTexts = detectedTexts;
    }

    public List<RawBlock.RawText> getRawTexts() {
        return rawTexts;
    }

    public String toString() {
        StringBuilder list = new StringBuilder();
        for (RawBlock.RawText text : rawTexts) {
            list.append(text.getDetection());
        }
        return list.toString();
    }
}
