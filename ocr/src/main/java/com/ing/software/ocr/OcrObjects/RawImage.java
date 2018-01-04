package com.ing.software.ocr.OcrObjects;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.ing.software.ocr.OcrUtils;

import java.util.List;

/**
 * Class to store only useful properties of source images
 * @author Michelon
 */

public class RawImage {

    private int height;
    private int width;
    private Rect extendedRect;
    private double averageRectHeight;

    /**
     * Constructor, initializes variables
     * @param bitmap source photo. Not null.
     */
    public RawImage(@NonNull Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setRects(List<RawText> texts) {
        extendedRect = OcrUtils.getMaxRectBorders(texts);
        averageRectHeight = checkAverageHeight(texts);
    }

    public Rect getExtendedRect() {
        return extendedRect;
    }

    public double getAverageRectHeight() {
        return averageRectHeight;
    }

    private double checkAverageHeight(List<RawText> texts) {
        double average = 0;
        for (RawText text : texts) {
            average += text.getBoundingBox().height();
        }
        return average/texts.size();
    }
}
