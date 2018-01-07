package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.junit.Test;
import static junit.framework.Assert.*;

import com.ing.software.ocr.OcrObjects.RawText;


/**
 *
 */

public class RawTextTest {

    private static String defaultString = "Value";
    private static Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    private static RawText rawText = RawTextFactory.getRawText(defaultString, new Rect(10,10,30,50), bitmap);
    private static RawText rawText2 = RawTextFactory.getRawText(defaultString, new Rect(5,5,60,30), bitmap);
    private static RawText rawText3 = RawTextFactory.getRawText(defaultString, new Rect(10,5,60,30), bitmap);

    @Test
    public void testrawTextValue() throws Exception {
        assertEquals("Value", rawText.getValue());
    }

    @Test
    public void testrawTextCompare() throws Exception {
        assertTrue(rawText.compareTo(rawText2) > 0);
    }

    @Test
    public void testrawTextCompare2() throws Exception {
        assertTrue(rawText2.compareTo(rawText) < 0);
    }

    @Test
    public void testrawTextCompare3() throws Exception {
        assertTrue(rawText.compareTo(rawText) == 0);
    }

    @Test
    public void testrawTextCompare4() throws Exception {
        assertTrue(rawText2.compareTo(rawText3) < 0);
    }
}
