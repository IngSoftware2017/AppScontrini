package com.ing.software.ocr;

/**
 * Callback interface used to get an OcrResult from OcrAnalyzer.
 */
interface OnOcrResultReadyListener {

    /**
     * Get an OcrResult
     * @param result new OcrResult. Never null.
     */
    void onOcrResultReady(OcrResult result);
}
