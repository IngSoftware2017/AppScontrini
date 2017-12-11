package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import org.junit.Test;
import static junit.framework.Assert.*;

import com.google.android.gms.vision.text.Text;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.List;

/**
 *
 */

public class RawTextTest {

    private static Text text = new Text() {
        @Override
        public String getValue() {
            return "Value";
        }

        @Override
        public Rect getBoundingBox() {
            return new Rect(10,10,30,50);
        }

        @Override
        public Point[] getCornerPoints() {
            Point a = new Point(10, 10);
            Point b = new Point(30, 50);
            return new Point[]{a,b};
        }

        @Override
        public List<? extends Text> getComponents() {
            return null;
        }
    };
    private static Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    private static RawImage rawImage = new RawImage(bitmap);
    private static RawText rawText = new RawText(text, rawImage);
    private static Text text2 = new Text() {
        @Override
        public String getValue() {
            return "Value2";
        }

        @Override
        public Rect getBoundingBox() {
            return new Rect(5,5,60,30);
        }

        @Override
        public Point[] getCornerPoints() {
            Point a = new Point(5, 5);
            Point b = new Point(60, 30);
            return new Point[]{a,b};
        }

        @Override
        public List<? extends Text> getComponents() {
            return null;
        }
    };
    private static RawText rawText2 = new RawText(text2, rawImage);
    private static Text text3 = new Text() {
        @Override
        public String getValue() {
            return "Value2";
        }

        @Override
        public Rect getBoundingBox() {
            return new Rect(10,5,60,30);
        }

        @Override
        public Point[] getCornerPoints() {
            Point a = new Point(10, 5);
            Point b = new Point(60, 30);
            return new Point[]{a,b};
        }

        @Override
        public List<? extends Text> getComponents() {
            return null;
        }
    };
    private static RawText rawText3 = new RawText(text3, rawImage);

    @Test
    public void testrawTextValue() throws Exception {
        assertEquals("Value", rawText.getDetection());
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
