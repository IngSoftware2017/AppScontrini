package com.ing.software.appscontrini.OCR;

import android.graphics.Bitmap;

import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Util class
 */

public class OCRUtils {

    /**
     * Crop image
     * @param photo original photo
     * @param startX x coordinate of top left point
     * @param startY y coordinate of top left point
     * @param endX x coordinate of bottom right point
     * @param endY y coordinate of bottom right point
     * @return cropped image
     */
    static Bitmap cropImage(Bitmap photo, int startX, int startY, int endX, int endY) {
        if (endX < startX || endY < startY)
            return null;
        int width = endX - startX;
        int height = endY - startY;
        return Bitmap.createBitmap(photo, startX, startY, width, height);
    }

    /**
     * Get rectangular containing all blocks detected (Temporary method)
     * @param orderedTextBlocks blocks detected
     * @param photo original photo
     * @return array of int where int[0] = left border, int[1] = top border, int[2] = right border, int[3] = bottom border
     */
    static int[] getRectBorders(List<TextBlock> orderedTextBlocks, Bitmap photo) {
        int[] borders = new int[4];
        int left = photo.getWidth();
        int right = 0;
        int top = 0;
        int bottom = photo.getHeight();
        for (TextBlock textBlock : orderedTextBlocks) {
            if (textBlock.getBoundingBox().left<left)
                left = textBlock.getBoundingBox().left;
            if (textBlock.getBoundingBox().right>right)
                right = textBlock.getBoundingBox().right;
            if (textBlock.getBoundingBox().bottom<bottom)
                bottom = textBlock.getBoundingBox().bottom;
            if (textBlock.getBoundingBox().top>top)
                top = textBlock.getBoundingBox().top;
        }
        borders[0] = left;
        borders[1] = top;
        borders[2] = right;
        borders[3] = bottom;
        return borders;
    }

    /**
     * Get preferred grid according to height/width ratio
     * @param photo original photo
     * @return preferred ratio defined in ProbGrid
     */
    static String getPreferredGrid(Bitmap photo) {
        int width = photo.getWidth();
        int heigth = photo.getHeight();
        double ratio = heigth/width;
        ratio = Math.floor(ratio * 100) / 100;
        List<Double> availableRatios = new ArrayList<>(ProbGrid.gridMap.keySet());
        String preferredRatio = "-1";
        double scarto = Double.MAX_VALUE;
        for(Double testratio : availableRatios) {
            if (Math.abs(testratio-ratio)<scarto) {
                scarto = Math.abs(testratio - ratio);
                preferredRatio = ProbGrid.gridMap.get(testratio);
            }
        }
        return preferredRatio;
    }

    /**
     * Order a list from top to bottom, left to right
     * @param textBlocks original list
     * @return ordered list
     */
    static List<TextBlock> orderBlocks(List<TextBlock> textBlocks) {
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
        return textBlocks;
    }
}
