package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 *
 */

public class OcrUtilsTests {

    @Test
    public void getPreferredGrid16x9() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(90, 160, Bitmap.Config.ARGB_8888);
        String expectedGrid = "16x9";
        String resultGrid = OcrUtils.getPreferredGrid(bitmap);
        assertEquals(expectedGrid, resultGrid);
    }

    @Test
    public void extendRect() throws Exception {
        RectF source = new RectF(10,10,60,60);
        RectF target = OcrUtils.extendRect(source, 100, 100);
        RectF expected = new RectF(0, 0, 85, 85);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }

    @Test
    public void extendRectOnlyW() throws Exception {
        RectF source = new RectF(10,10,60,60);
        RectF target = OcrUtils.extendRect(source, 0, 100);
        RectF expected = new RectF(0, 10, 85, 60);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }

    @Test
    public void extendRectOnlyH() throws Exception {
        RectF source = new RectF(10,10,60,60);
        RectF target = OcrUtils.extendRect(source, 100, 0);
        RectF expected = new RectF(10, 0, 60, 85);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }

    @Test
    public void extendRect2() throws Exception {
        RectF source = new RectF(50,50,100,80);
        RectF target = OcrUtils.extendRect(source, 100, 100);
        RectF expected = new RectF(25, 35, 125, 95);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }
}
