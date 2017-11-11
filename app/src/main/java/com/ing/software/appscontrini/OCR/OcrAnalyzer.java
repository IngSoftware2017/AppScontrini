package com.ing.software.appscontrini.OCR;

import android.app.Service;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */

public class OcrAnalyzer {

    static void execute(Bitmap photo, Service service) {
        //inspect(photo, service);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("OcrAnalyzer.execute:" , "Starting analyzing" );
        analyze(photo, service);
    }

    //Brute force, cerca e mostra tutto a caso
    static void inspect(Bitmap photo, Service service) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(service).build();
        try {
            Frame frame = new Frame.Builder().setBitmap(photo).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }
            Log.d("analyze:" , " E qui... " + origTextBlocks.size() + " ordered: " + textBlocks.size());
            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedText = new StringBuilder();
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText.append(textBlock.getValue());
                    detectedText.append("\n");
                    Log.e("MYAPP", "detected: "+ textBlock.getValue());
                }
                Toast toast = Toast.makeText(service, detectedText, Toast.LENGTH_LONG);
                toast.show();
            }
        }
        finally {
            textRecognizer.release();
        }
    }

    //Cerca stringhe particolari, con regioni di probabilità custom (WIP)
    static void analyze(Bitmap photo, Service service) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(service).build();
        try {
            Frame frame = new Frame.Builder().setBitmap(photo).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> orderedTextBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                orderedTextBlocks.add(origTextBlocks.valueAt(i));
            }
            Log.d("OcrAnalyzer.analyze:" , "Blocks detected");
            orderedTextBlocks = OCRUtils.orderBlocks(orderedTextBlocks);
            Log.d("OcrAnalyzer.analyze:" , "Blocks ordered");
            int[] borders = OCRUtils.getRectBorders(orderedTextBlocks, photo);
            int left = borders[0];
            int right = borders[2];
            int top = borders[1];
            int bottom = borders[3];
            Bitmap croppedPhoto = OCRUtils.cropImage(photo, left, top, right, bottom);
            String grid = OCRUtils.getPreferredGrid(croppedPhoto);
            Log.d("OcrAnalyzer.analyze:" , "Photo cropped");

            frame = new Frame.Builder().setBitmap(croppedPhoto).build();
            SparseArray<TextBlock> newTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> newOrderedTextBlocks = new ArrayList<>();
            for (int i = 0; i < newTextBlocks.size(); i++) {
                newOrderedTextBlocks.add(newTextBlocks.valueAt(i));
            }
            newOrderedTextBlocks = OCRUtils.orderBlocks(newOrderedTextBlocks);
            Log.d("OcrAnalyzer.analyze:" , "New Blocks ordered");
            List<RawBlock> rawBlocks = new ArrayList<>();
            for (TextBlock textBlock : newOrderedTextBlocks) {
                rawBlocks.add(new RawBlock(textBlock, croppedPhoto, grid));
            }
            //NOT IMPLEMENTED CUSTOM SEARCH YET
            StringBuilder detectionList = new StringBuilder();
            for (RawBlock rawBlock : rawBlocks) {
                List<RawBlock.RawText> rawTexts = rawBlock.getRawTexts();
                for (RawBlock.RawText rawText : rawTexts) {
                    detectionList.append(rawText.getDetection())
                            .append("\n");
                }
            }
            Log.d("OcrAnalyzer", "detected: "+ detectionList);
            Toast toast = Toast.makeText(service, detectionList, Toast.LENGTH_LONG);
            toast.show();

            //BRUTE SEARCH TEST
            String testString = "TOTALE";
            //String testString = "24";
            RawBlock.RawText targetText = null;
            for (RawBlock rawBlock : rawBlocks) {
                targetText = rawBlock.bruteSearch(testString);
                if (targetText != null)
                    break;
            }
            if (targetText != null) {
                Log.d("OcrAnalyzer", "Found first target string: "+ testString + " \nat: " + targetText.getDetection());
                Log.d("OcrAnalyzer", "Target text is at (left, top, right, bottom): "+ targetText.getRect().left
                + "; " + targetText.getRect().top + "; " + targetText.getRect().right + "; "+ targetText.getRect().bottom + ".");
            }

            //BRUTE SEARCH TEST CONTINUOUS
            List<RawBlock.RawText> targetTextList = new ArrayList<>();
            for (RawBlock rawBlock : rawBlocks) {
                List<RawBlock.RawText> tempTextList = new ArrayList<>();
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

            //BRUTE SEARCH TEST CONTINUOUS WITH AMOUNT
            //usiamo il targetTextList di BRUTE SEARCH TEST CONTINUOUS
            List<RawBlock.RawText> resultTexts = new ArrayList<>();
            int testpercentage = 100;
            for (RawBlock rawBlock : rawBlocks) {
                for (RawBlock.RawText rawText : targetTextList) {
                    List<RawBlock.RawText> tempResultList = rawBlock.findByPosition(OCRUtils.getExtendedRect(rawText.getRect(), croppedPhoto), testpercentage);
                    if (tempResultList != null) {
                        for (int j = 0; j < tempResultList.size(); j++) {
                            resultTexts.add(tempResultList.get(j));
                            Log.d("OcrAnalyzer", "Found target string in: " + rawText.getDetection() + "\nwith value: " + tempResultList.get(j).getDetection());
                        }
                    }
                }
            }
            if (resultTexts.size() ==0) {
                Log.d("OcrAnalyzer", "Nothing found ");
            }
        }
        finally {
            textRecognizer.release();
        }
    }
}
