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
import com.ing.software.ocr.OperativeObjects.RawImage;
import com.ing.software.ocr.OperativeObjects.ScoreFunc;

import static com.ing.software.common.CommonUtils.rectFromSize;
import static com.ing.software.common.CommonUtils.size;
import static com.ing.software.ocr.OcrUtils.extendRect;
import static com.ing.software.ocr.OcrVars.*;

/**
 * Class used only to perform ocr-library related operations or Text search. No more no less.
 */
//ZAGLIA: Consider moving this into OperativeObjects
public class OcrAnalyzer {

    // ideal character width / height
    private static final double CHAR_ASPECT_RATIO = 5. / 8.;

    private TextRecognizer ocrEngine = null;
    private RawImage mainImage;

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

    /**
     * Sets mainImage for current analyzer. Must be called every time you change your source image
     * (not if you use strip search on an already initialized analyzer with the same source image)
     * @param mainImage image to set. Not null.
     */
    void setMainImage(RawImage mainImage) {
        this.mainImage = mainImage;
    }

    /**
     * Release internal resources.
     */
    void release() {
        ocrEngine.release();
    }

    /**
     * @author Michelon
     * @author Riccardo Zaglia
     *
     * Run the ocr detection on the given bitmap.
     * @param bm input bitmap
     * //@param ocrEngine TextRecognizer
     * @return list of OcrText
     */
    List<OcrText> analyze(@NonNull Bitmap bm){
        long startTime = 0;
        long endTime = 1;
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        SparseArray<TextBlock> tempArray = ocrEngine.detect(new Frame.Builder().setBitmap(bm).build());
        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "DETECTOR: ", "EXECUTION TIME: " + duration + " seconds");
        }
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        List<OcrText> rawTexts = getTexts(tempArray);
        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "OCR ANALYZER: ", "EXECUTION TIME: " + duration + " seconds");
        }
        return rawTexts;
    }

    /**
     * @author Michelon
     * @author Riccardo Zaglia
     * Adds TextBlock decoded by detector in a list of Texts
     * @param origTextBlocks detected texts. Not null.
     * @return list of Texts
     */
    private static List<OcrText> getTexts(@NonNull SparseArray<TextBlock> origTextBlocks) {
        List<OcrText> texts = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); ++i) {
            //for (Text currentText : origTextBlocks.valueAt(i).getComponents()) {
            //    texts.add(new OcrText(currentText));
            //}
            TextBlock block = origTextBlocks.valueAt(i);
            for (Text text : block.getComponents()) {
                OcrUtils.log(7, "GETTEXTS: ", "Text: " + text.getValue());
                texts.add(new OcrText(text));
            }
        }
        return texts;
    }

    /**
     * @author Michelon
     *
     * Get extended rect from half of source rect (x axis) to right of pic and with height extended by AMOUNT_RECT_HEIGHT_EXTENDER
     * @param amountText source text containing amount string
     * @param bmWidth source rawimage width
     * @return rect extended from source text
     */
    static RectF getAmountExtendedBox(OcrText amountText, float bmWidth) {
        float newTop = amountText.box().top - amountText.height()* AMOUNT_RECT_HEIGHT_EXTENDER;
        float newBottom = amountText.box().bottom + amountText.height()* AMOUNT_RECT_HEIGHT_EXTENDER;
        return new RectF(amountText.box().centerX(), newTop, bmWidth, newBottom);
    }

    /**
     * @author Zaglia
     *
     * Create a new bitmap optimized for amount price, from the chosen strip rect.
     * @param processor processor containing source image and valid corners
     * @param bmSize original bitmap size
     * @param amountStr amount string text
     * @param origStripRect strip rect in the original bitmap space
     * @return bitmap strip
     */
    private static Bitmap getAmountStrip(
            ImageProcessor processor, SizeF bmSize, OcrText amountStr, RectF origStripRect) {
        return processor.undistortedSubregion(bmSize, origStripRect,
                origStripRect.width() / origStripRect.height() * CHAR_ASPECT_RATIO
                        / (amountStr.charWidth() / amountStr.charHeight()));
    }

    /**
     * @author Michelon
     *
     * Replace texts in rawImage inside passed rect with new texts passed
     * @param texts new texts to add
     * @param rect rect containing texts to remove
     */
    private void replaceTexts(List<Scored<OcrText>> texts, RectF rect) {
        RectF extendedRect = extendRect(rect, 5, 5);
        List<OcrText> newTexts = Stream.of(texts)
                                    .map(text -> RawImage.mapText(text.obj(), mainImage))
                                    .toList();
        mainImage.removeText(extendedRect);
        for (OcrText text : newTexts)
            mainImage.addText(text);
        OcrUtils.log(3, "replaceTexts", "NEW REPLACED TEXTS");
        OcrUtils.listEverything(mainImage);
    }

    /**
     * @author Michelon
     * @author EDIT: Zaglia
     *
     * Get Texts in amount extended box
     * @param processor processor containing source image
     * @param origBmSize original bitmap size
     * @param amountStringText source text containing amount string
     * //@param stripRect strip bounding box in original bitmap space
     * @return list of scored texts containing decoded values
     */
    List<Scored<OcrText>> getTextsInStrip(ImageProcessor processor, SizeF origBmSize, OcrText amountStringText, RectF stripRect) {
        Bitmap strip = getAmountStrip(processor, origBmSize, amountStringText, stripRect);
        RectF undistortedStripRect = rectFromSize(size(strip));
        List<Scored<OcrText>> texts = Stream.of(analyze(strip))
                .map(text -> new OcrText(text, undistortedStripRect, stripRect))
                .map(text -> new Scored<>(ScoreFunc.getDistFromSourceScore(amountStringText, text), text))
                .sorted(Collections.reverseOrder())
                .toList();
        for (Scored<OcrText> tt : texts) {
            OcrUtils.log(3, "getTextsInStrip: " , "For tt: " + tt.obj().text() + " Score is: " + tt.getScore());
        }
        replaceTexts(texts, stripRect);
        return texts;
    }

    /**
     * @author Michelon
     *
     * Get original texts from extended amount rect
     * @param amountText text containing amount string
     * @param extendedRect rect where to find texts
     * @return list of scored texts containing possible price
     */
    List<Scored<OcrText>> getOrigTexts(OcrText amountText, RectF extendedRect){
        //copy texts that are inside extended rect.
        List<Scored<OcrText>> texts = Stream.of(mainImage.getAllTexts())
                .filter(text -> extendedRect.contains(text.box()))
                .map(text -> new Scored<>(ScoreFunc.getDistFromSourceScore(amountText, text), text))
                .sorted(Collections.reverseOrder())
                .toList();
        if (IS_DEBUG_ENABLED)
            for (Scored<OcrText> tt : texts) {
                OcrUtils.log(4, "getOrigTexts: " , "For tt: " + tt.obj().text() + " Score is: " + tt.getScore());
            }
        return texts;
    }

    /**
     * @author Michelon
     *
     * Get all texts on left of source text
     * @param source source product price rect
     * @return list of texts on left of source rect
     */
    List<OcrText> getTextsOnleft(OcrText source) {
        float newTop = source.box().top - source.height()* PRODUCT_RECT_HEIGHT_EXTENDER;
        float newBottom = source.box().bottom + source.height()* PRODUCT_RECT_HEIGHT_EXTENDER;
        RectF extendedRect = new RectF(0, newTop, source.box().centerX(), newBottom);
        return Stream.of(mainImage.getAllTexts())
                        .filter(text -> extendedRect.contains(text.box()))
                        .toList();
    }
}
