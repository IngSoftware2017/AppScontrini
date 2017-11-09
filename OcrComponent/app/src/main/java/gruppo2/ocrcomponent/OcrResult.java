package gruppo2.ocrcomponent;

import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure containing the unprocessed text + metadata, extracted by OcrAnalyzer.
 */
public class OcrResult {

    /**
     * List of TextBlock.
     */
    public List<TextBlock> blockList = new ArrayList<>();
}
