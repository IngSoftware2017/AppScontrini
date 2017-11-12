package com.ing.software.ticketapp.OCR;

/**
 * Callback interface used to get an OcrResult from OcrAnalyzer.
 */
public interface OnOcrResultReadyListener {

    /**
     * Get an OcrResult
     * @param result new OcrResult. Never null.
     */
    void onOcrResultReady(OcrResult result);
}
