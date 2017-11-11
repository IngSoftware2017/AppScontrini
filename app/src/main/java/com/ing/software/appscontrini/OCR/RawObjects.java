package com.ing.software.appscontrini.OCR;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * WIP
 * WIP
 * WIP
 * WIP
 */
class RawBlock {

    private ArrayList<RawText> rawTexts = new ArrayList<>();
    private List<? extends Text> textComponents;
    private RectF rectF;
    private int imageWidth;
    private int imageHeigth;
    private String grid;

    RawBlock(TextBlock textBlock, Bitmap imageMod, String grid) {
        rectF = new RectF(textBlock.getBoundingBox());
        imageWidth = imageMod.getWidth();
        imageHeigth = imageMod.getHeight();
        textComponents = textBlock.getComponents();
        this.grid = grid;
        initialize();
    }

    private void initialize() {
        int index = 0;
        for (Text currentText : textComponents) {
            rawTexts.add(new RawText(currentText, index));
            ++index;
        }
    }

    RectF getRect() {
        return rectF;
    }

    ArrayList<RawText> getRawTexts() {
        return rawTexts;
    }

    /**
     * Loops throw Rawtexts checking if their rect is in a box where probability
     * to find amount is > 0
     * @param level number of results to ignore (used for deeper analysis)
     * @return string with detected amount, null if nothing is found
     */
    String findAmount(int level) {
        String amount = null;
        int i = 0;
        while (level > 0 && i < rawTexts.size()) {
            RawText rawText = rawTexts.get(i);
            amount = rawText.findAmount();
            if (amount!=null) {
                level--;
                if (level == 0)
                    return amount;
            }
        }
        return null;
    }

    /**
     * Search string in block, only first occurrence is returned (top -> bottom, left -> right)
     * @param string string to search
     * @return RawText containing the string, null if nothing found
     */
    RawText bruteSearch(String string) {
        for (RawText rawText : rawTexts) {
            if (rawText.bruteSearch(string))
                return rawText;
        }
        return null;
    }

    /**
     * Search string in blockall occurrecnces are returned returned (top -> bottom, left -> right)
     * @param string string to search
     * @return list of RawText containing the string, null if nothing found
     */
    List<RawText> bruteSearchContinuous(String string) {
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
     * Find all textblocks inside chosen rect with an error of percent (on width and height of chosen rect)
     * @param rect rect where you want to find texts
     * @param percent error accepted on chosen rect
     * @return list of RawText in chosen rect, null if nothing found
     */
    List<RawText> findByPosition(RectF rect, int percent) {
        List<RawText> rawTextList = new ArrayList<>();
        RectF newRect = extendRect(rect, percent);
        for (RawText rawText : rawTexts) {
            if (rawText.isInside(newRect))
                rawTextList.add(rawText);
        }
        if (rawTextList.size()>0)
            return rawTextList;
        else
            return null;
    }

    /**
     * Create a new rect extending source rect with chosen percentage (on width and height of chosen rect)
     * Min value for top and left is 0
     * @param rect source rect
     * @param percent chosen percentage
     * @return new rectangle extended
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
        float right = rect.right + extendedWidth/2;
        float bottom = rect.bottom + extendedHeight/2;
        Log.d("RawObjects.extendRect","Extended rect: left " + left + " top: " + top + " right: " + right + " bottom: " + bottom);
        return new RectF(left, top, right, bottom);
    }

    class RawText {

        private RectF rectText;
        private int index;
        private Text text;

        RawText(Text text, int position) {
            rectText = new RectF(text.getBoundingBox());
            this.index = position;
            this.text = text;
        }

        int getIndex() {
            return index;
        }

        String getDetection() {
            return text.getValue();
        }

        RectF getRect() {
            return rectText;
        }

        /**
         * Check if current textbox is in a probability region, if yes checks if amount is present
         * @return string with detected amount, null if nothing found
         */
        private String findAmount() {
            int[] gridBox = getGridBox();
            int probability = ProbGrid.amountMap.get(grid)[gridBox[1]][gridBox[0]];
            if (probability > 0)
                if (checkAmountPresent())
                    return getDetection();
                else
                    return null;
            else
                return null;
        }

        /**
         * Find box of grid contianing the center of the text rect
         * @return coordinates of the grid, where int[0] = column, int[1] = row
         */
        private int[] getGridBox() {
            Scanner gridder = new Scanner(grid);
            gridder.useDelimiter("x");
            int rows = Integer.parseInt(gridder.next());
            int columns = Integer.parseInt(gridder.next());
            gridder.close();
            double rowsHeight = imageHeigth/rows;
            double columnsWidth = imageWidth/columns;
            int gridX = (int) (rectText.centerX()/columnsWidth);
            int gridY = (int) (rectText.centerY()/rowsHeight);
            return new int[] {gridX, gridY};
        }

        /**
         * Checks if amount string is present
         * @return true if amount string is present
         */
        private boolean checkAmountPresent() {
            String amount = getDetection();
            if (amount.contains("TOTALE"))
                return true;
            else
                return false;
        }

        /**
         * Search string in text
         * @param string string to search
         * @return true if string is present
         */
        private boolean bruteSearch(String string) {
            if (text.getValue().contains(string))
                return true;
            else
                return false;
        }

        private boolean isInside(RectF rect) {
            return rect.contains(rectText);
        }
    }
}
