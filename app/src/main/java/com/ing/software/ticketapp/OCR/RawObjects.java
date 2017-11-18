package com.ing.software.ticketapp.OCR;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class to store objects detected.
 * Contains useful methods and variables that TextBlock does not provide.
 * @author Michelon
 */
class RawBlock {

    private List<RawText> rawTexts = new ArrayList<>();
    private List<? extends Text> textComponents;
    private RectF rectF;
    private RawImage rawImage;

    /**
     * Constructor, parameters must not be null
     * @param textBlock source TextBlock
     * @param imageMod source image
     */
    RawBlock(TextBlock textBlock, RawImage imageMod) {
        rectF = new RectF(textBlock.getBoundingBox());
        textComponents = textBlock.getComponents();
        this.rawImage = imageMod;
        initialize();
    }

    /**
     * Populates this block with its Rawtexts
     */
    private void initialize() {
        for (Text currentText : textComponents) {
            rawTexts.add(new RawText(currentText));
        }
    }

    /**
     * Search string in block, only first occurrence is returned (top -> bottom, left -> right)
     * @param string string to search
     * @return RawText containing the string, null if nothing found
     */
    RawText findFirst(String string) {
        for (RawText rawText : rawTexts) {
            if (rawText.bruteSearch(string))
                return rawText;
        }
        return null;
    }

    /**
     * Search string in block, all occurrences are returned (top -> bottom, left -> right)
     * @param string string to search
     * @return list of RawText containing the string, null if nothing found
     */
    List<RawText> findContinuous(String string) {
        List<RawText> rawTextList = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            if (rawText.bruteSearch(string))
                rawTextList.add(rawText);
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Find all Rawtexts inside chosen rect with an error of 'percent' (on width and height of chosen rect)
     * @param rect rect where you want to find texts
     * @param percent error accepted on chosen rect
     * @return list of RawText in chosen rect, null if nothing found
     */
    List<RawText> findByPosition(RectF rect, int percent) {
        List<RawText> rawTextList = new ArrayList<>();
        RectF newRect = extendRect(rect, percent);
        for (RawText rawText : rawTexts) {
            if (rawText.isInside(newRect)) {
                rawTextList.add(rawText);
                Log.d("OcrAnalyzer", "Found target rect: " + rawText.getDetection());
            }
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Get a list of rawTexts with the probability they contain the date non ordered
     * @return list of texts + probability date is present
     */
    List<RawGridResult> getDateList() {
        List<RawGridResult> list = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            list.add(new RawGridResult(rawText, rawText.getDateProbability()));
        }
        Log.d("LIST_SIZE_IS", " " + list.size());
        return list;
    }

    /**
     * Create a new rect extending source rect with chosen percentage (on width and height of chosen rect)
     * Note: Min value for top and left is 0
     * @param rect source rect
     * @param percent chosen percentage
     * @return new extended rectangle
     */
    private RectF extendRect(RectF rect, int percent) {
        Log.d("RawObjects.extendRect","Source rect: left " + rect.left + " top: "
                + rect.top + " right: " + rect.right + " bottom: " + rect.bottom);
        float extendedHeight = rect.height()*percent/100;
        float extendedWidth = rect.width()*percent/100;
        float left = rect.left - extendedWidth/2;
        if (left<0)
            left = 0;
        float top = rect.top - extendedHeight/2;
        if (top < 0)
            top = 0;
        //Doesn't matter if bottom and right are outside the photo
        float right = rect.right + extendedWidth/2;
        float bottom = rect.bottom + extendedHeight/2;
        Log.d("RawObjects.extendRect","Extended rect: left " + left + " top: " + top
                + " right: " + right + " bottom: " + bottom);
        return new RectF(left, top, right, bottom);
    }

    class RawText implements Comparable<RawText>{

        private RectF rectText;
        private Text text;
        /**
         * Constructor
         * @param text current Text inside TextBlock
         */
        RawText(Text text) {
            rectText = new RectF(text.getBoundingBox());
            this.text = text;
        }

        /**
         * @return string contained in this Text
         */
        String getDetection() {
            return text.getValue();
        }

        /**
         * @return rect of this Text
         */
        RectF getRect() {
            return rectText;
        }

        /**
         * @return rawImage of this Text
         */
        RawImage getRawImage() {
            return rawImage;
        }

        /**
         * Retrieves probability that date is present in current text
         * @return probability that date is present
         */
        private int getDateProbability() {
            Log.d("Value is: ", getDetection());
            int[] gridBox = getGridBox();
            Log.d("Grid box is: ", " " + gridBox[1] + ":" + gridBox[0]);
            int probability = ProbGrid.dateMap.get(rawImage.getGrid())[gridBox[1]][gridBox[0]];
            Log.d("Probability is", " " +probability);
            return probability;
        }

        /**
         * Retrieves probability that amount is present in current text
         * @return probability that amount is present
         */
        private int getAmountProbability() {
            int[] gridBox = getGridBox();
            int probability = ProbGrid.amountMap.get(rawImage.getGrid())[gridBox[1]][gridBox[0]];
            return probability;
        }

        /**
         * Find box of the grid containing the center of the text rect
         * @return coordinates of the grid, where int[0] = column, int[1] = row
         */
        private int[] getGridBox() {
            Scanner gridder = new Scanner(rawImage.getGrid());
            gridder.useDelimiter("x");
            int rows = Integer.parseInt(gridder.next());
            int columns = Integer.parseInt(gridder.next());
            gridder.close();
            double rowsHeight = rawImage.getHeight()/rows;
            double columnsWidth = rawImage.getWidth()/columns;
            int gridX = (int) (rectText.centerX()/columnsWidth);
            int gridY = (int) (rectText.centerY()/rowsHeight);
            return new int[] {gridX, gridY};
        }

        /**
         * Search string in text
         * @param string string to search
         * @return true if string is present
         */
        private boolean bruteSearch(String string) {
            //Here Euristic search will be implemented
            if (getDetection().contains(string))
                return true;
            else
                return false;
        }

        /**
         * Check if this text is inside chosen rect
         * @param rect target rect that could contain this text
         * @return true if is inside
         */
        private boolean isInside(RectF rect) {
            return rect.contains(rectText);
        }

        @Override
        public int compareTo(@NonNull RawText rawText) {
            RectF text2Rect = rawText.getRect();
            if (text2Rect.top != rectF.top)
                return Math.round(text2Rect.top - rectF.top);
            else if (text2Rect.left != rectF.left)
                return Math.round(text2Rect.left - rectF.left);
            else if (text2Rect.bottom != rectF.bottom)
                return Math.round(text2Rect.bottom - rectF.bottom);
            else if (text2Rect.right != rectF.right)
                return Math.round(text2Rect.right - rectF.right);
            else
                return 0;
        }
    }
}

/**
 * Class to store only useful properties of source images
 */
class RawImage {

    private int height;
    private int width;
    private String grid;

    RawImage(Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        grid = OCRUtils.getPreferredGrid(bitmap);
    }

    int getHeight() {
        return height;
    }

    int getWidth() {
        return width;
    }

    String getGrid() {
        return grid;
    }
}

/**
 * Class to store results from string search
 */
class RawStringResult {

    private RawBlock.RawText sourceText;
    private List<RawBlock.RawText> detectedTexts = null;

    RawStringResult(RawBlock.RawText rawText) {
        this.sourceText = rawText;
    }

    void setDetectedTexts(List<RawBlock.RawText> detectedTexts) {
        this.detectedTexts = detectedTexts;
    }

    RawBlock.RawText getSourceText() {
        return sourceText;
    }

    List<RawBlock.RawText> getDetectedTexts() {
        return detectedTexts;
    }
}


/**
 * Class to store results from grid search
 */
class RawGridResult implements Comparable<RawGridResult>{
	
	private int percentage;
	private RawBlock.RawText singleText;
	
	RawGridResult(RawBlock.RawText singleText, int percentage) {
		this.percentage = percentage;
		this.singleText = singleText;
	}
	
	int getPercentage() {
		return percentage;
	}
	
	RawBlock.RawText getText() {
		return singleText;
	}

    @Override
    public int compareTo(@NonNull RawGridResult rawGridResult) {
        if (getPercentage() == rawGridResult.getPercentage())
            return getText().compareTo(rawGridResult.getText());
        else
            return rawGridResult.getPercentage() - getPercentage();
    }
}
