package gruppo2.ocrcomponent;

import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Struttura contentente il testo non processato + metadati, estratti dall'algoritmo OCR.
 */
public class OcrResult {

    /**
     * Lista di TextBlock
     */
    public List<TextBlock> blockList = new ArrayList<>();
}
