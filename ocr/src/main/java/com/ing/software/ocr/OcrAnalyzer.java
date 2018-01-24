package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.*;

import java.util.*;

import com.ing.software.ocr.OcrObjects.*;

import static com.ing.software.ocr.OcrUtils.log;
import static com.ing.software.ocr.OcrVars.*;

/**
 * Class used only to perform ocr-library related operations. No more no less.
 */
public class OcrAnalyzer {

    private TextRecognizer ocrEngine = null;


    /**
     * Initialize the component.
     * If this method returns -1, check if the device has enough free disk space.
     * In this case, try to call this method again.
     * When this method returns 0, it is possible to call getOcrResult.
     * @param ctx Android context.
     * @return 0 if successful, negative otherwise.
     */
    int initialize(Context ctx) {
        ocrEngine = new TextRecognizer.Builder(ctx).build();
        return ocrEngine.isOperational() ? 0 : -1;
        //failure causes: GSM package is not yet downloaded due to lack of time or lack of space.
    }

    void release() {
        ocrEngine.release();
    }

    /**
     * Get an OcrResult from a Bitmap
     * @param frame Bitmap used to create an OcrResult. Not null.
     * @return OcrResult containing raw data to be further analyzed.
     */
    List<TempText> analyze(@NonNull Bitmap frame){
        //ocrEngine analysis
        long startTime = 0;
        long endTime = 1;
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        SparseArray<TextBlock> tempArray = ocrEngine.detect(new Frame.Builder().setBitmap(frame).build());
        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "DETECTOR: ", "EXECUTION TIME: " + duration + " seconds");
        }
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        List<TempText> rawOrigTexts = getTexts(tempArray);
        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "OCR ANALYZER: ", "EXECUTION TIME: " + duration + " seconds");
        }
        return rawOrigTexts;
    }


    /**
     * @author Michelon
     * Orders TextBlock decoded by detector in a list of TempTexts
     * Order is from top to bottom, from left to right
     * @param origTextBlocks detected texts. Not null.
     * @return list of ordered RawTexts
     */
    private static List<TempText> getTexts(@NonNull SparseArray<TextBlock> origTextBlocks) {
        List<TextBlock> newOrderedTextBlocks = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); i++) {
            newOrderedTextBlocks.add(origTextBlocks.valueAt(i));
        }
        newOrderedTextBlocks = OcrUtils.orderTextBlocks(newOrderedTextBlocks);
        log(3,"OcrAnalyzer.analyzeST:" , "New Blocks ordered");
        List<TempText> rawTexts = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            for (Text currentText : textBlock.getComponents()) {
                rawTexts.add(new TempText(currentText));
            }
        }
        return rawTexts;
    }
}
