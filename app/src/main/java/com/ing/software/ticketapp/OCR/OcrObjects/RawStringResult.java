package com.ing.software.ticketapp.OCR.OcrObjects;


import java.util.List;

/**
 * Class to store results from string search
 */

public class RawStringResult {

    private RawText sourceText;
    private List<RawText> detectedTexts = null;

    public RawStringResult(RawText rawText) {
        this.sourceText = rawText;
    }

    public void setDetectedTexts(List<RawText> detectedTexts) {
        this.detectedTexts = detectedTexts;
    }

    public RawText getSourceText() {
        return sourceText;
    }

    public List<RawText> getDetectedTexts() {
        return detectedTexts;
    }
}
