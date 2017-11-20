package com.ing.software.ticketapp.OCR.OcrObjects;


import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.google.android.gms.vision.text.Text;
import com.ing.software.ticketapp.OCR.*;

import java.util.Scanner;

import static com.ing.software.ticketapp.OCR.OcrUtils.log;

/**
 * Class to store texts detected.
 * Contains useful methods and variables that Text does not provide.
 * @author Michelon
 */

public class RawText implements Comparable<RawText>{

    private RectF rectText;
    private Text text;
    private RawImage rawImage;

    /**
     * Constructor
     * @param text current Text inside TextBlock
     */
    RawText(Text text, RawImage rawImage) {
        rectText = new RectF(text.getBoundingBox());
        this.text = text;
        this.rawImage = rawImage;
    }

    /**
     * @return string contained in this Text
     */
    public String getDetection() {
        return text.getValue();
    }

    /**
     * @return rect of this Text
     */
    public RectF getRect() {
        return rectText;
    }

    /**
     * @return rawImage of this Text
     */
    public RawImage getRawImage() {
        return rawImage;
    }

    /**
     * Retrieves probability that date is present in current text
     * @return probability that date is present
     */
    int getDateProbability() {
        log("Value is: ", getDetection());
        int[] gridBox = getGridBox();
        log("Grid box is: ", " " + gridBox[1] + ":" + gridBox[0]);
        int probability = ProbGrid.dateMap.get(rawImage.getGrid())[gridBox[1]][gridBox[0]];
        log("Probability is", " " +probability);
        return probability;
    }

    /**
     * Retrieves probability that amount is present in current text
     * @return probability that amount is present
     */
    int getAmountProbability() {
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
    boolean bruteSearch(String string) {
        //Here Euristic search will be implemented
        return getDetection().contains(string);
    }

    /**
     * Check if this text is inside chosen rect
     * @param rect target rect that could contain this text
     * @return true if is inside
     */
    boolean isInside(RectF rect) {
        return rect.contains(rectText);
    }

    @Override
    public int compareTo(@NonNull RawText rawText) {
        RectF text2Rect = rawText.getRect();
        if (text2Rect.top != rectText.top)
            return Math.round(text2Rect.top - rectText.top);
        else if (text2Rect.left != rectText.left)
            return Math.round(text2Rect.left - rectText.left);
        else if (text2Rect.bottom != rectText.bottom)
            return Math.round(text2Rect.bottom - rectText.bottom);
        else if (text2Rect.right != rectText.right)
            return Math.round(text2Rect.right - rectText.right);
        else
            return 0;
    }
}
