package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ing.software.ocr.OcrObjects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ing.software.ocr.OcrUtils.log;
import static com.ing.software.ocr.OcrVars.*;

/**
 * Class containing different methods to analyze a picture
 * @author Michelon
 * @author Zaglia
 */
class OcrAnalyzer {

    private TextRecognizer ocrEngine = null;
    private RawImage mainImage;
    private final int targetPrecision = 100; //Should be passed with image, or calculated with
        //resolution of source image


    /**
     * @author Michelon
     * @author Zaglia
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
     * @author Michelon
     * @author Zaglia
     * Get an OcrResult from a Bitmap
     * @param frame Bitmap used to create an OcrResult. Not null.
     * @return OcrResult containing raw data to be further analyzed.
     */
    OcrResult analyze(@NonNull Bitmap frame){
        //cropping must be used somewhere else (if used with textRecognizer). Can be used here is using opencv
        //frame = getCroppedPhoto(frame, context);
        mainImage = new RawImage(frame);

        //ocrEngine analysis
        SparseArray<TextBlock> tempArray = ocrEngine.detect(new Frame.Builder().setBitmap(frame).build());

        List<RawBlock> rawBlocks = orderBlocks(mainImage, tempArray);
        List<RawStringResult> valuedTexts = new ArrayList<>();
        for (String amountString : AMOUNT_STRINGS) {
        	valuedTexts.addAll(searchContinuousString(rawBlocks, amountString));
        }
        valuedTexts = searchContinuousStringExtended(rawBlocks, valuedTexts, targetPrecision);
        List<RawGridResult> dateList = getDateList(rawBlocks);
        return new OcrResult(valuedTexts, dateList);
    }

    /**
     * @author Michelon
     * Orders TextBlock decoded by detector in a list of RawBlocks
     * Order is from top to bottom, from left to right
     * @param photo RawImage of the photo. Not null.
     * @param origTextBlocks detected blocks. Not null.
     * @return list of ordered RawBlocks
     */
    private static List<RawBlock> orderBlocks(@NonNull RawImage photo, @NonNull SparseArray<TextBlock> origTextBlocks) {
        log(2,"OcrAnalyzer.analyzeST:" , "Preferred grid is: " + photo.getGrid());
        List<TextBlock> newOrderedTextBlocks = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); i++) {
            newOrderedTextBlocks.add(origTextBlocks.valueAt(i));
        }
        newOrderedTextBlocks = OcrUtils.orderTextBlocks(newOrderedTextBlocks);
        log(2,"OcrAnalyzer.analyzeST:" , "New Blocks ordered");
        List<RawBlock> rawBlocks = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            rawBlocks.add(new RawBlock(textBlock, photo));
        }
        return rawBlocks;
    }

    /**
     * @author Michelon
     * Find first (RawTexts in RawBlocks are ordered from top to bottom, left to right)
     * occurrence (exact match) of chosen string in a list of RawBlocks
     * @param rawBlocks list of RawBlocks. Not null.
     * @param testString string to find. Length > 0.
     * @return First RawText in first RawBlock with target string
     */
    private static RawText searchFirstExactString(@NonNull List<RawBlock> rawBlocks, @Size(min = 1) String testString) {
        RawText targetText = null;
        for (RawBlock rawBlock : rawBlocks) {
            targetText = rawBlock.findFirstExact(testString);
            if (targetText != null)
                break;
        }
        if (targetText != null) {
            log(3,"OcrAnalyzer.analyzeBFS", "Found first target string: "+ testString + " \nat: " + targetText.getDetection());
            log(3,"OcrAnalyzer.analyzeBFS", "Target text is at (left, top, right, bottom): "+ targetText.getRect().left + "; "
                    + targetText.getRect().top + "; " + targetText.getRect().right + "; "+ targetText.getRect().bottom + ".");
        }
        return targetText;
    }

    /**
     * @author Michelon
     * Search for all occurrences of target string in detected (and ordered) RawBlocks according to OcrVars.MAX_STRING_DISTANCE
     * @param rawBlocks list of RawBlocks. Not null.
     * @param testString string to find. Length > 0.
     * @return list of RawStringResult containing only source RawText where string is present. Note: this list is not ordered.
     */
    private static List<RawStringResult> searchContinuousString(@NonNull List<RawBlock> rawBlocks, @Size(min = 1) String testString) {
        List<RawStringResult> targetTextList = new ArrayList<>();
        for (RawBlock rawBlock : rawBlocks) {
            List<RawStringResult> tempTextList = rawBlock.findContinuous(testString, MAX_STRING_DISTANCE);
            if (tempTextList != null) {
                targetTextList.addAll(tempTextList);
            }
        }
        if (targetTextList.size() >0 && IS_DEBUG_ENABLED) {
            for (RawStringResult stringText : targetTextList) {
                RawText text = stringText.getSourceText();
                log(3,"OcrAnalyzer", "Found target string: " + testString + " \nat: " + text.getDetection()
                        + " with distance: " + stringText.getDistanceFromTarget());
                log(3,"OcrAnalyzer", "Target text is at (left, top, right, bottom): " + text.getRect().left
                        + "; " + text.getRect().top + "; " + text.getRect().right + "; " + text.getRect().bottom + ".");
            }
        }
        return targetTextList;
    }

    /**
     * @author Michelon
     * From a list of RawTexts, retrieves also RawTexts with similar distance from top and bottom of the photo.
     * 'Similar' is defined by precision. See {@link RawBlock findByPosition()} for details.
     * @param rawBlocks list of RawBlocks from original photo. Not null.
     * @param targetStringList list of target RawTexts. Not null.
     * @param precision precision to extend rect. See RawBlock.RawText.extendRect()
     * @return list of RawStringResults. Adds to the @param targetStringList objects the detected RawTexts
     * in proximity of source RawTexts. Note: this list is not ordered.
     */
    /*How search works::
    - Take a block
    - search in extended rect of the source of a stringResult if it contains one or more text of this block
    - if yes, adds these texts to the detected texts of the stringResults
    - repeat from 2 until you have no more stringResults left
    - repeat from 1 until you have no more blocks left
    */
    private static List<RawStringResult> searchContinuousStringExtended(@NonNull List<RawBlock> rawBlocks, @NonNull List<RawStringResult> targetStringList, int precision) {
        List<RawStringResult> results = targetStringList;
        log(2,"OcrAnalyzer.SCSE", "StringResult list size is: " + results.size());
        for (RawBlock rawBlock : rawBlocks) {
            for (RawStringResult singleResult : results) {
                RawText rawText = singleResult.getSourceText();
                log(3,"OcrAnalyzer.SCSE", "Extending rect: " + rawText.getDetection());
                List<RawText> tempResultList = rawBlock.findByPosition(OcrUtils.getExtendedRect(rawText.getRect(), rawText.getRawImage()), precision);
                if (tempResultList != null) {
                    singleResult.addDetectedTexts(tempResultList);
                    log(3,"OcrAnalyzer", "Found target string: " + singleResult.getSourceString() + "\nfrom extended: " + rawText.getDetection() + "\nin " + tempResultList.size() + " blocks.");
                }
                else
                    log(3,"OcrAnalyzer.SCSE", "Nothing found"); //Nothing in this block
            }
        }
        if (results.size() == 0) {
            log(2,"OcrAnalyzer", "Nothing found ");
        }
        else if (IS_DEBUG_ENABLED){
            log(2,"OcrAnalyzer", "Final list: " + results.size());
            for (RawStringResult rawStringResult : results) {
                List<RawText> textList = rawStringResult.getDetectedTexts();
                if (textList == null)
                    log(2,"OcrAnalyzer.SCSE", "Value not found.");
                else {
                    for (RawText rawText : textList) {
                        log(2,"OcrAnalyzer.SCSE", "Value: " + rawText.getDetection());
                        log(2,"OcrAnalyzer.SCSE", "Source: " + rawStringResult.getSourceText().getDetection());
                    }
                }
            }
        }
        return results;
    }

    /**
     * @author Michelon
     * Merges Lists with RawTexts + probability to find date from all blocks.
     * And orders it according to they probability (fallback is position).
     * @param rawBlocks blocks from which retrieve lists. Not null.
     * @return List of RawGridResults containing RawTexts + probability
     */
    private List<RawGridResult> getDateList(@NonNull List<RawBlock> rawBlocks) {
        List<RawGridResult> fullList = new ArrayList<>();
        for (RawBlock rawBlock : rawBlocks) {
            fullList.addAll(rawBlock.getDateList());
        }
        log(2,"FINAL_LIST_SIZE_IS", " " + fullList.size());
        Collections.sort(fullList);
        return fullList;
    }

    /**
     * @author Michelon
     * Performs a quick detection on chosen photo and returns blocks detected
     * @param photo photo to analyze. Not null.
     * @param context context to run analyzer
     * @return list of all blocks found (ordered from top to bottom, left to right)
     */
    private static List<TextBlock> quickAnalysis(@NonNull Bitmap photo, Context context) {
        SparseArray<TextBlock> origTextBlocks;
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        try {
            Frame frame = new Frame.Builder().setBitmap(photo).build();
            origTextBlocks = textRecognizer.detect(frame);
        }
        finally {
            textRecognizer.release();
        }
        List<TextBlock> orderedTextBlocks = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); i++) {
            orderedTextBlocks.add(origTextBlocks.valueAt(i));
        }
        return orderedTextBlocks;
    }

    /**
     * NOTE: This is a temporary method.
     * @author Michelon
     * Crops photo using the smallest rect that contains all TextBlock in source photo
     * @param photo source photo. Not null.
     * @param context context to run first analyzer
     * @return smallest cropped photo containing all TextBlocks
     */
    public static Bitmap getCroppedPhoto(@NonNull Bitmap photo, Context context) {
        List<TextBlock> orderedTextBlocks = quickAnalysis(photo, context);
        log(2,"OcrAnalyzer.analyze:" , "Blocks detected");
        orderedTextBlocks = OcrUtils.orderTextBlocks(orderedTextBlocks);
        log(2,"OcrAnalyzer.analyze:" , "Blocks ordered");
        int[] borders = OcrUtils.getRectBorders(orderedTextBlocks, new RawImage(photo));
        int left = borders[0];
        int right = borders[2];
        int top = borders[1];
        int bottom = borders[3];
        return OcrUtils.cropImage(photo, left, top, right, bottom);
    }
}
