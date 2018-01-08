package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 *
 */

public class OcrUtilsTests {


    @Test
    public void extendRect() throws Exception {
        Rect source = new Rect(10,10,60,60);
        Rect target = OcrUtils.extendRect(source, 100, 100);
        Rect expected = new Rect(0, 0, 85, 85);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }

    @Test
    public void extendRectOnlyW() throws Exception {
        Rect source = new Rect(10,10,60,60);
        Rect target = OcrUtils.extendRect(source, 0, 100);
        Rect expected = new Rect(0, 10, 85, 60);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }

    @Test
    public void extendRectOnlyH() throws Exception {
        Rect source = new Rect(10,10,60,60);
        Rect target = OcrUtils.extendRect(source, 100, 0);
        Rect expected = new Rect(10, 0, 60, 85);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }

    @Test
    public void extendRect2() throws Exception {
        Rect source = new Rect(50,50,100,80);
        Rect target = OcrUtils.extendRect(source, 100, 100);
        Rect expected = new Rect(25, 35, 125, 95);
        assertEquals(expected.left, target.left);
        assertEquals(expected.top, target.top);
        assertEquals(expected.right, target.right);
        assertEquals(expected.bottom, target.bottom);
    }
}
