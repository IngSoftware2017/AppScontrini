package com.ing.software.ocr.OcrObjects;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.ing.software.ocr.Legacy.RawText;
import com.ing.software.ocr.RawTextFactory;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 *
 */

public class RawGridResultTests {

    private static String defaultString = "Value";
    private static Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    private static RawText rawText = RawTextFactory.getRawText(defaultString, new Rect(10,10,30,50), bitmap);
    private static RawText rawText2 = RawTextFactory.getRawText(defaultString, new Rect(5,5,60,30), bitmap);
    private static RawText rawText3 = RawTextFactory.getRawText(defaultString, new Rect(10,5,60,30), bitmap);
    private static RawGridResult rawGridResult = new RawGridResult(rawText, 100);
    private static RawGridResult rawGridResult2 = new RawGridResult(rawText2, 50);
    private static RawGridResult rawGridResult3 = new RawGridResult(rawText3, -20);

    @Test
    public void testRawDRPerc() throws Exception {
        assertEquals(100.0, rawGridResult.getPercentage());
    }

    @Test
    public void testRawDRPerc2() throws Exception {
        assertEquals(50.0, rawGridResult2.getPercentage());
    }

    @Test
    public void testRawDRPerc3() throws Exception {
        assertEquals(-20.0, rawGridResult3.getPercentage());
    }

    @Test
    public void testRawDRComp() throws Exception {
        assertTrue(rawGridResult.compareTo(rawGridResult2) < 0);
    }

    @Test
    public void testRawDRComp2() throws Exception {
        assertTrue(rawGridResult3.compareTo(rawGridResult2) > 0);
    }

    @Test
    public void testRawDRComp3() throws Exception {
        assertTrue(rawGridResult.compareTo(rawGridResult3) < 0);
    }

    @Test
    public void testRawDRComp4() throws Exception {
        assertTrue(rawGridResult.compareTo(rawGridResult) == 0);
    }
}
