package gruppo2.ocrcomponent;

/**
 * Interfaccia callback utilizzata per ottenere un OcrResult dalla classe ImageProcessor
 */
public interface OnOcrResultReadyListener {

    /**
     * Ottieni un OcrResult
     * @param result il nuovo OcrResult. Mai null.
     */
    void onOcrResultReady(OcrResult result);
}
