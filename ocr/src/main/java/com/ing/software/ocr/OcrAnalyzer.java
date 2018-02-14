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

import static com.ing.software.ocr.OcrUtils.extendRect;
import static com.ing.software.ocr.OcrVars.*;

/**
 * Class used only to perform ocr-library related operations or Text search. No more no less.
 */
//ZAGLIA: Consider moving this into OperativeObjects
public class OcrAnalyzer {

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
     * Get an OcrResult from a Bitmap
     * @param frame Bitmap used to create an OcrResult. Not null.
     * @return OcrResult containing raw data to be further analyzed.
     */
    List<OcrText> analyze(@NonNull Bitmap frame){
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
        List<OcrText> rawOrigTexts = getTexts(tempArray);
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
     * Get texts inside rect
     * @param processor processor containing source image
     * @param boundingBox rect to analyze
     * @return list of detected texts
     */
    private List<OcrText> getStripTexts(ImageProcessor processor, RectF boundingBox) {
        Bitmap region = processor.undistortedSubregion(new SizeF(mainImage.getWidth(), mainImage.getHeight()),
                boundingBox, boundingBox.width()/boundingBox.height()); //original aspect ratio
        SparseArray<TextBlock> tempArray = ocrEngine.detect(new Frame.Builder().setBitmap(region).build());
        List<OcrText> distortedTexts = getTexts(tempArray);
        return Stream.of(distortedTexts)
                .map(text -> new OcrText(text, new RectF(0,0,region.getWidth(), region.getHeight()), boundingBox))
                .toList();
    }

    /**
     * Get extended rect from half of source rect (x axis) to right of pic and with height extended by AMOUNT_RECT_HEIGHT_EXTENDER
     * @param amountText source text containing amount string
     * @return rect extended from source text
     */
    private static RectF getAmountExtendedBox(OcrText amountText, RawImage mainImage) {
        float newTop = amountText.box().top - amountText.height()* AMOUNT_RECT_HEIGHT_EXTENDER;
        float newBottom = amountText.box().bottom + amountText.height()* AMOUNT_RECT_HEIGHT_EXTENDER;
        return new RectF(amountText.box().centerX(), newTop,
                mainImage.getWidth(), newBottom);
    }

    /**
     * Replace texts in rawImage inside passed rect with new texts passed
     * @param texts new texts to add
     * @param rect rect containing texts to remove
     */
    private void replaceTexts(List<Scored<OcrText>> texts, RectF rect) {
        RectF extendedRect = extendRect(rect, 5, 5);
        List<OcrText> newTexts = Stream.of(texts)
                                    .map(text -> mapText(text.obj()))
                                    .toList();
        mainImage.removeText(extendedRect);
        for (OcrText text : newTexts)
            mainImage.addText(text);
        OcrUtils.log(3, "replaceTexts", "NEW REPLACED TEXTS");
        OcrUtils.listEverything(mainImage.getAllTexts());
    }

    /**
     * Get Texts in amount extended box
     * @param processor processor containing source image
     * @param amountText source text containing amount string
     * @return list of scored texts containing decoded values
     */
    List<Scored<OcrText>> getAmountStripTexts(ImageProcessor processor, OcrText amountText) {
        List<Scored<OcrText>> texts = Stream.of(getStripTexts(processor, getAmountExtendedBox(amountText, mainImage)))
                .map(text -> new Scored<>(ScoreFunc.getDistFromSourceScore(amountText, text), text))
                .sorted(Collections.reverseOrder())
                .toList();
        //Collections.sort(texts, Collections.reverseOrder());
        for (Scored<OcrText> tt : texts) {
            OcrUtils.log(3, "getAmountStripTexts: " , "For tt: " + tt.obj().text() + " Score is: " + tt.getScore());
        }
        replaceTexts(texts, getAmountExtendedBox(amountText, mainImage));
        return texts;
    }

    /**
     * Get original texts from extended amount rect
     * @param amountText text containing amount string
     * @return list of scored texts containing possible price
     */
    List<Scored<OcrText>> getAmountOrigTexts(OcrText amountText) {
        RectF extendedRect = getAmountExtendedBox(amountText, mainImage);
        extendedRect.set(amountText.box().left, extendedRect.top, extendedRect.right, extendedRect.bottom);
        //copy texts that are inside extended rect.
        List<Scored<OcrText>> texts = Stream.of(mainImage.getAllTexts())
                                            .filter(text -> extendedRect.contains(text.box()))
                                            .map(text -> new Scored<>(ScoreFunc.getDistFromSourceScore(amountText, text), text))
                                            .sorted(Collections.reverseOrder())
                                            .toList();
        //Collections.sort(texts, Collections.reverseOrder());
        if (IS_DEBUG_ENABLED)
            for (Scored<OcrText> tt : texts) {
                OcrUtils.log(4, "getAmountOrigTexts: " , "For tt: " + tt.obj().text() + " Score is: " + tt.getScore());
            }
        return texts;
    }

    /**
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

    /**
     * Add tag to text according to its position (must call textFitter before)
     * @param text text to tag
     * @return text with added tag
     */
    private OcrText mapText(OcrText text) {
        if (mainImage.getIntroRect().contains(text.box()))
            text.addTag(INTRODUCTION_TAG);
        else if (mainImage.getProductsRect().contains(text.box()))
            text.addTag(PRODUCTS_TAG);
        else if (mainImage.getPricesRect().contains(text.box()))
            text.addTag(PRICES_TAG);
        else if (mainImage.getConclusionRect().contains(text.box()))
            text.addTag(CONCLUSION_TAG);
        else {
            float productsTop = Math.min(mainImage.getProductsRect().top, mainImage.getPricesRect().top);
            float productsBottom = Math.max(mainImage.getProductsRect().bottom, mainImage.getPricesRect().bottom);
            RectF middleRect = new RectF(mainImage.getProductsRect().left, productsTop, mainImage.getPricesRect().right, productsBottom);
            //Check its y coordinate
            float centerY = text.box().centerY();
            if (centerY < productsTop)
                text.addTag(INTRODUCTION_TAG);
            else if (centerY > productsBottom)
                text.addTag(CONCLUSION_TAG);
            else {
                if (text.box().centerX() < middleRect.width()/2)
                    text.addTag(PRODUCTS_TAG);
                else
                    text.addTag(PRICES_TAG);
            }
        }
        return text;
    }
}
