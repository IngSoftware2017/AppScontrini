package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Log;

import com.google.android.gms.vision.text.TextBlock;
import com.ing.software.ocr.OcrObjects.RawImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Util class to manage rects, blocks, images
 * @author Michelon
 */

public class OcrUtils {

    /**
     * Crop image (values start from top left)
     * @param photo original photo not null
     * @param startX x coordinate of top left point, int >= 0
     * @param startY y coordinate of top left point, int >= 0
     * @param endX x coordinate of bottom right point, int > 0
     * @param endY y coordinate of bottom right point, int > 0
     * @return cropped image, null if invalid coordinates
     */
    static Bitmap cropImage(@NonNull Bitmap photo, int startX, int startY, int endX, int endY) {
        log("UtilsMain.cropImage","Received crop: left " + startX + " top: " + startY + " right: " + endX + " bottom: " + endY);
        if (endX < startX || endY < startY)
            return null;
        int width = Math.abs(endX - startX);
        int height = Math.abs(endY - startY);
        return Bitmap.createBitmap(photo, startX, startY, width, height);
    }

    /**
     * Get rect containing all blocks detected (Temporary method)
     * Note: counting starts from left and from top (not bottom)
     * @param orderedTextBlocks blocks detected
     * @param photo original photo
     * @return array of int where int[0] = left border, int[1] = top border, int[2] = right border, int[3] = bottom border
     */
    static int[] getRectBorders(List<TextBlock> orderedTextBlocks, RawImage photo) {
        int numberOfBorders = 4; //it's a rect
        int[] borders = new int[numberOfBorders];
        //Extreme borders for chosen photo (will be overwritten in foreach)
        int left = photo.getWidth();
        int right = 0;
        int top = photo.getHeight();
        int bottom = 0;
        for (TextBlock textBlock : orderedTextBlocks) {
            RectF rectF = new RectF(textBlock.getBoundingBox());
            if (rectF.left<left)
                left = Math.round(rectF.left);
            if (rectF.right>right)
                right = Math.round(rectF.right);
            if (rectF.bottom>bottom)
                bottom = Math.round(rectF.bottom);
            if (rectF.top<top)
                top = Math.round(rectF.top);
            log("UtilsMain.getRectBorder","Value: " + textBlock.getValue());
            log("UtilsMain.getRectBorder","Temp rect: (left, top, right, bottom): " + rectF.left + "; " + rectF.top + "; " + rectF.right + "; " + rectF.bottom);
        }
        borders[0] = left;
        borders[1] = top;
        borders[2] = right;
        borders[3] = bottom;
        log("UtilsMain.getRectBorder","New rect: (left, top, right, bottom): " + left + "; " + top + "; " + right + "; " + bottom);
        return borders;
    }

    /**
     * Get preferred grid according to height/width ratio
     * @param photo original photo
     * @return preferred ratio defined in ProbGrid, -1 if something went wrong
     */
     public static String getPreferredGrid(Bitmap photo) {
        double width = photo.getWidth();
        double heigth = photo.getHeight();
        String preferredRatio = "-1";
        if (width <= 0 || heigth <= 0)
            return preferredRatio;
        double ratio = heigth/width;
        List<Double> availableRatios = new ArrayList<>(ProbGrid.gridMap.keySet());
        double diff = Double.MAX_VALUE;
        for(Double testRatio : availableRatios) {
            if (Math.abs(testRatio-ratio)<diff) {
                diff = Math.abs(testRatio - ratio);
                preferredRatio = ProbGrid.gridMap.get(testRatio);
            }
        }
        log("UtilsMain.getPrefGrid","Ratio is: " + ratio + " Grid is: " + preferredRatio + " Diff is: " + diff);
        return preferredRatio;
    }

    /**
     * Order a list of TextBlock from top to bottom, left to right
     * @param textBlocks original list
     * @return ordered list
     */
    static List<TextBlock> orderBlocks(List<TextBlock> textBlocks) {
        Collections.sort(textBlocks, new Comparator<TextBlock>() {
            @Override
            public int compare(TextBlock block1, TextBlock block2) {
                int diffTops = block1.getBoundingBox().top - block2.getBoundingBox().top;
                int diffLefts = block1.getBoundingBox().left - block2.getBoundingBox().left;
                if (diffTops != 0) {
                    return diffTops;
                }
                return diffLefts;
            }
        });
        return textBlocks;
    }

    /**
     * Extends the width of a rect to the max allowed for chosen photo
     * @param rect source rect
     * @param photo source photo (to get max width)
     * @return rect with max width
     */
    static RectF getExtendedRect(RectF rect, RawImage photo) {
        float top = rect.top;
        float bottom = rect.bottom;
        float left = 0;
        float right = photo.getWidth();
        RectF rectF = new RectF(left, top, right, bottom);
        log("UtilsMain.getExtendRect","Extended rect: left " + rectF.left + " top: "
                + rectF.top + " right: " + rectF.right + " bottom: " + rectF.bottom);
        return rectF;
    }

    /**
     * Logs messages only if debug is enabled
     * @param tag tag of the message to log, must be less than 23 chars long
     * @param message message to log
     */
    public static void log(@Size(max = 23) String tag, String message) {
        if (OcrVars.ISDEBUGENABLED)
            Log.d(tag, message);
    }
}
