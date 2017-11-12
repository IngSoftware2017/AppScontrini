package com.ing.software.ticketapp.OCR;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing different methods to analyze a picture
 * @author Michelon
 * @author Zaglia
 */
public class OcrAnalyzer {

    private TextRecognizer ocrEngine = null;
    private OnOcrResultReadyListener ocrResultCb = null;

    //todo: find a way to test private methods
    /**
     * Repackage a collection of TextBlock into an OcrResult.
     * @param textBlocks SparseArray<TextBlock> from Detector.Processor<TextBlock>
     * @return new OcrResult.
     */
    OcrResult textBlocksToOcrResult(SparseArray<TextBlock> textBlocks) {
        OcrResult newOcrResult = new OcrResult();

        //SparseArray to List
        List<TextBlock> list = new ArrayList<>(textBlocks.size());
        for (int i = 0; i < textBlocks.size(); i++)
            list.add(textBlocks.valueAt(i));

        newOcrResult.blockList = list;
        return newOcrResult;
    }

    /**
     * Initialize the component.
     * If this call returns -1, check if the device has enough free disk space.
     * If so, try to call this method again.
     * When this method returned 0, it will be possible to call getOcrResult.
     * @param ctx Android context.
     * @return 0 if successful, negative otherwise.
     */
    public int initialize(Context ctx) {
        ocrEngine = new TextRecognizer.Builder(ctx).build();
        ocrEngine.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                //check if getOcrResult has been called to assign ocrResultCb.
                if (ocrResultCb != null) {
                    OcrResult newOcrResult = textBlocksToOcrResult(detections.getDetectedItems());
                    ocrResultCb.onOcrResultReady(newOcrResult);
                }
            }
        });

        return ocrEngine.isOperational() ? 0 : -1;
        //failure causes: GSM package is not yet downloaded due to lack of time or lack of space.
    }

    /**
     * Get an OcrResult from a Bitmap
     * @param frame Bitmap from which to extract an OcrResult. Not null.
     * @param resultCb Callback to get an OcrResult. Not null.
     */
    public void getOcrResult(Bitmap frame, OnOcrResultReadyListener resultCb){
        ocrResultCb = resultCb;
        ocrEngine.receiveFrame(new Frame.Builder().setBitmap(frame).build());
    }
    
    /**
     * Test method for analysis
     * @param photo source photo to analyze
     * @param context main context for the detector to run
     */
    static void execute(Bitmap photo, Context context) {
        Log.d("OcrAnalyzer.execute:" , "Starting analyzing" );
        analyzeSingleText(photo, context);
        String testString = "TOTALE";
        int testPrecision = 100;
        analyzeBruteFirstString(photo, context, testString);
        analyzeBruteContinuousString(photo, context, testString);
        analyzeBruteContHorizValue(photo, context, testString, testPrecision);
    }

    /**
     * Analyze a photo and returns everything it could decode
     * @param photo photo to analyze
     * @param context main context for the detector to run
     * @return string containing everything it found
     */
    static String analyzeSingleText(Bitmap photo, Context context) {
        Bitmap croppedPhoto = getCroppedPhoto(photo, context);
        String grid = OCRUtils.getPreferredGrid(croppedPhoto);
        Log.d("OcrAnalyzer.analyzeST:" , "Photo cropped");
        List<TextBlock> newOrderedTextBlocks = initAnalysis(croppedPhoto, context);
        newOrderedTextBlocks = OCRUtils.orderBlocks(newOrderedTextBlocks);
        Log.d("OcrAnalyzer.analyzeST:" , "New Blocks ordered");
        List<RawBlock> rawBlocks = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            rawBlocks.add(new RawBlock(textBlock, croppedPhoto, grid));
        }
        StringBuilder detectionList = new StringBuilder();
        for (RawBlock rawBlock : rawBlocks) {
            List<RawBlock.RawText> rawTexts = rawBlock.getRawTexts();
            for (RawBlock.RawText rawText : rawTexts) {
                detectionList.append(rawText.getDetection())
                        .append("\n");
            }
        }
        Log.d("OcrAnalyzer.analyzeST", "detected: "+ detectionList);
        return detectionList.toString();
    }

    /**
     * Search for first occurrence of a string in chosen photo. Order is top->bottom, left->right
     * @param photo photo to analyze
     * @param context main context for the detector to run
     * @param testString string to search
     * @return string containing first RawBlock.RawText where it found the string
     */
    static String analyzeBruteFirstString(Bitmap photo, Context context, String testString) {
        Bitmap croppedPhoto = getCroppedPhoto(photo, context);
        String grid = OCRUtils.getPreferredGrid(croppedPhoto);
        Log.d("OcrAnalyzer.analyzeBFS:" , "Photo cropped");
        List<TextBlock> newOrderedTextBlocks = initAnalysis(croppedPhoto, context);
        newOrderedTextBlocks = OCRUtils.orderBlocks(newOrderedTextBlocks);
        Log.d("OcrAnalyzer.analyzeBFS:" , "New Blocks ordered");
        List<RawBlock> rawBlocks = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            rawBlocks.add(new RawBlock(textBlock, croppedPhoto, grid));
        }
        RawBlock.RawText targetText = null;
        for (RawBlock rawBlock : rawBlocks) {
            targetText = rawBlock.bruteSearch(testString);
            if (targetText != null)
                break;
            }
        if (targetText != null) {
            Log.d("OcrAnalyzer.analyzeBFS", "Found first target string: "+ testString + " \nat: " + targetText.getDetection());
            Log.d("OcrAnalyzer.analyzeBFS", "Target text is at (left, top, right, bottom): "+ targetText.getRect().left + "; "
                    + targetText.getRect().top + "; " + targetText.getRect().right + "; "+ targetText.getRect().bottom + ".");
        }
        return targetText.getDetection();
    }

    /**
     * Search for all occurrences of a string in chosen photo. Order is top->bottom, left->right
     * @param photo photo to analyze
     * @param context main context for the detector to run
     * @param testString string to search
     * @return list of all RawBlock.RawText where it found the string
     */
    static List<RawBlock.RawText> analyzeBruteContinuousString(Bitmap photo, Context context, String testString) {
        Bitmap croppedPhoto = getCroppedPhoto(photo, context);
        String grid = OCRUtils.getPreferredGrid(croppedPhoto);
        Log.d("OcrAnalyzer.analyze:" , "Photo cropped");
        List<TextBlock> newOrderedTextBlocks = initAnalysis(croppedPhoto, context);
        newOrderedTextBlocks = OCRUtils.orderBlocks(newOrderedTextBlocks);
        Log.d("OcrAnalyzer.analyze:" , "New Blocks ordered");
        List<RawBlock> rawBlocks = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            rawBlocks.add(new RawBlock(textBlock, croppedPhoto, grid));
        }
        List<RawBlock.RawText> targetTextList = new ArrayList<>();
        for (RawBlock rawBlock : rawBlocks) {
            List<RawBlock.RawText> tempTextList;
            tempTextList = rawBlock.bruteSearchContinuous(testString);
            if (tempTextList != null) {
                for (int i = 0; i < tempTextList.size(); i++) {
                    targetTextList.add(tempTextList.get(i));
                }
            }
        }
        if (targetTextList.size() >0) {
            for (RawBlock.RawText text : targetTextList) {
                Log.d("OcrAnalyzer", "Found target string: " + testString + " \nat: " + text.getDetection());
                Log.d("OcrAnalyzer", "Target text is at (left, top, right, bottom): " + text.getRect().left
                        + "; " + text.getRect().top + "; " + text.getRect().right + "; " + text.getRect().bottom + ".");
            }
        }
        return targetTextList;
    }

    /**
     * Searches for all occurrences of a string in chosen photo, then retrieves also text with similar
     * distance from top and bottom. Max difference from string detection is defined by precision. Order is top->bottom, left->right
     * @param photo photo to analyze
     * @param context main context for the detector to run
     * @param testString string to search
     * @param precision int > 0 percentage of the height of detected RawText to extend. See RawBlock.extendRect().
     * @return list of all RawBlock.RawText where it found the string and with similar distance from top and bottom.
     */
    static List<RawBlock.RawText> analyzeBruteContHorizValue(Bitmap photo, Context context, String testString, int precision) {
        Bitmap croppedPhoto = getCroppedPhoto(photo, context);
        String grid = OCRUtils.getPreferredGrid(croppedPhoto);
        Log.d("OcrAnalyzer.analyze:" , "Photo cropped");
        List<TextBlock> newOrderedTextBlocks = initAnalysis(croppedPhoto, context);
        newOrderedTextBlocks = OCRUtils.orderBlocks(newOrderedTextBlocks);
        Log.d("OcrAnalyzer.analyze:" , "New Blocks ordered");
        List<RawBlock> rawBlocks = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            rawBlocks.add(new RawBlock(textBlock, croppedPhoto, grid));
        }
        List<RawBlock.RawText> targetTextList = new ArrayList<>();
        for (RawBlock rawBlock : rawBlocks) {
            List<RawBlock.RawText> tempTextList;
            tempTextList = rawBlock.bruteSearchContinuous(testString);
            if (tempTextList != null) {
                for (int i = 0; i < tempTextList.size(); i++) {
                    targetTextList.add(tempTextList.get(i));
                }
            }
        }
        if (targetTextList.size() >0) {
            for (RawBlock.RawText text : targetTextList) {
                Log.d("OcrAnalyzer", "Found target string: " + testString + " \nat: " + text.getDetection());
                Log.d("OcrAnalyzer", "Target text is at (left, top, right, bottom): " + text.getRect().left + "; "
                        + text.getRect().top + "; " + text.getRect().right + "; " + text.getRect().bottom + ".");
            }
        }
        List<RawBlock.RawText> resultTexts = new ArrayList<>();
        for (RawBlock rawBlock : rawBlocks) {
            for (RawBlock.RawText rawText : targetTextList) {
                List<RawBlock.RawText> tempResultList = rawBlock.findByPosition(OCRUtils.getExtendedRect(rawText.getRect(), croppedPhoto), precision);
                if (tempResultList != null) {
                    for (int j = 0; j < tempResultList.size(); j++) {
                        resultTexts.add(tempResultList.get(j));
                        Log.d("OcrAnalyzer", "Found target string in: " + rawText.getDetection() + "\nwith value: " + tempResultList.get(j).getDetection());
                    }
                }
            }
        }
        resultTexts = OCRUtils.orderRawTexts(resultTexts);
        if (resultTexts.size() ==0) {
            Log.d("OcrAnalyzer", "Nothing found ");
        }
        else {
            Log.d("OcrAnalyzer", "Final list: ");
            for (RawBlock.RawText rawText : resultTexts) {
                Log.d("OcrAnalyzer", "Value: " + rawText.getDetection());
            }
        }
        return resultTexts;
    }

    /**
     * Starts and closes a TextRecognizer on chosen photo
     * @param photo photo to analyze
     * @param context main context for the detector to run
     * @return list of all blocks found
     */
    private static List<TextBlock> initAnalysis(Bitmap photo, Context context) {
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
     * Crops photo using the smallest rect that contains all TextBlock in source photo
     * @param photo source photo
     * @param context main context for the detector to run
     * @return smallest cropped photo containing all TextBlocks
     */
    private static Bitmap getCroppedPhoto(Bitmap photo, Context context) {
        List<TextBlock> orderedTextBlocks = initAnalysis(photo, context);
        Log.d("OcrAnalyzer.analyze:" , "Blocks detected");
        orderedTextBlocks = OCRUtils.orderBlocks(orderedTextBlocks);
        Log.d("OcrAnalyzer.analyze:" , "Blocks ordered");
        int[] borders = OCRUtils.getRectBorders(orderedTextBlocks, photo);
        int left = borders[0];
        int right = borders[2];
        int top = borders[1];
        int bottom = borders[3];
        return OCRUtils.cropImage(photo, left, top, right, bottom);
    }
}
