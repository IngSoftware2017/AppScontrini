package com.ing.software.appscontrini2.OCR;

import android.graphics.Bitmap;
import android.graphics.RectF;

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

        /**
         * Check if in current textbox is in a probability region, if yes checks if amount is present
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
    }
}
