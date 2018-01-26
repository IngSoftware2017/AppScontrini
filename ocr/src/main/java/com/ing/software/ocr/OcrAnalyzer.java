package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.SizeF;
import android.util.SparseArray;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.*;

import java.util.*;
import com.annimon.stream.Stream;

import com.ing.software.common.Scored;
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
     * @author Riccardo Zaglia
     * Adds TextBlock decoded by detector in a list of TempTexts
     * @param origTextBlocks detected texts. Not null.
     * @return list of Texts
     */
    private static List<TempText> getTexts(@NonNull SparseArray<TextBlock> origTextBlocks) {
        List<TempText> texts = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); ++i) {
            for (Text currentText : origTextBlocks.valueAt(i).getComponents()) {
                texts.add(new TempText(currentText));
            }
        }
        return texts;
    }

    /**
     * Get texts inside rect
     * @param processor
     * @param boundingBox
     * @return
     */
    private List<TempText> getStripTexts(ImageProcessor processor, RectF boundingBox) {
        Bitmap region = processor.undistortedSubregion(new SizeF(OcrManager.mainImage.getWidth(), OcrManager.mainImage.getHeight()),
                boundingBox, boundingBox.width()/boundingBox.height()); //original aspect ratio
        SparseArray<TextBlock> tempArray = ocrEngine.detect(new Frame.Builder().setBitmap(region).build());
        List<TempText> distortedTexts = getTexts(tempArray);
        return Stream.of(distortedTexts)
                .map(text -> new TempText(text, text.box(), boundingBox))
                .toList();
    }

    /**
     * Get extended rect from half of source rect (x axis) to right of pic and with height extended by RECT_HEIGHT_EXTENDER
     * @param amountText
     * @return
     */
    private static RectF getAmountExtendedBox(TempText amountText) {
        float newTop = amountText.box().top - amountText.height()*RECT_HEIGHT_EXTENDER;
        float newBottom = amountText.box().bottom + amountText.height()*RECT_HEIGHT_EXTENDER;
        return new RectF(amountText.box().centerX(), newTop,
                OcrManager.mainImage.getWidth(), newBottom);
    }

    /**
     * Get Texts in amount extended box
     * @param processor
     * @param amountText
     * @return
     */
    List<Scored<TempText>> getAmountStripTexts(ImageProcessor processor, TempText amountText) {
        return Stream.of(getStripTexts(processor, getAmountExtendedBox(amountText)))
                .map(text -> new Scored<>(ScoreFunc.getDistFromSourceScore(amountText, text), text))
                .toList();
    }

    /**
     * Get original texts from extended amount rect
     * @param amountText
     * @return
     */
    static List<Scored<TempText>> getAmountOrigTexts(TempText amountText) {
        RectF extendedRect = getAmountExtendedBox(amountText);
        extendedRect.set(amountText.box().left, extendedRect.top, extendedRect.right, extendedRect.bottom);
        //copy texts that are inside extended rect. todo Check if it'a a copy or if it modifies original list
        return Stream.of(OcrManager.mainImage.getAllTexts())
                                            .filter(text -> extendedRect.contains(text.box()))
                                            .map(text -> new Scored<>(ScoreFunc.getDistFromSourceScore(amountText, text), text))
                                            .toList();
    }
}
