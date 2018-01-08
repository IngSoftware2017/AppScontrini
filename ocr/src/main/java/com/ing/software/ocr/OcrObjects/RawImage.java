package com.ing.software.ocr.OcrObjects;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ing.software.ocr.OcrUtils;

/**
 * Class to store only useful properties of source images
 * @author Michelon
 */

public class RawImage {

    private int height;
    private int width;
    private String grid;

    /**
     * Constructor, initializes variables
     * @param bitmap source photo. Not null.
     */
    public RawImage(@NonNull Bitmap bitmap) {
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        grid = OcrUtils.getPreferredGrid(bitmap);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getGrid() {
        return grid;
    }
}
