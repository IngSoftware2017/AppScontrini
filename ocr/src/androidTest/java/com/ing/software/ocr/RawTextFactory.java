package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.vision.text.Text;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.List;

/**
 * Class to create RawTexts
 */

class RawTextFactory {

    static RawText getRawText(final String value, final Rect rect, Bitmap image) {
    Text text = new Text() {
        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Rect getBoundingBox() {
            return rect;
        }

        @Override
        public Point[] getCornerPoints() {
            Point a = new Point(rect.left, rect.top);
            Point b = new Point(rect.right, rect.top);
            Point c = new Point(rect.left, rect.bottom);
            Point d = new Point(rect.right, rect.bottom);
            return new Point[]{a, b, c, d};
        }

        @Override
        public List<? extends Text> getComponents() {
            return null;
        }
    };
    RawImage rawImage = new RawImage(image);
    return new RawText(text, rawImage);
    }

    static RawText getRawText(final String value, final Rect rect) {
        Bitmap image = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        return getRawText(value, rect, image);
    }

    static RawText getRawText(final String value) {
        Bitmap image = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Rect rect = new Rect(10,10,30,50);
        return getRawText(value, rect, image);
    }

    static RawText getRawText(final String value, Bitmap image) {
        Rect rect = new Rect(10,10,30,50);
        return getRawText(value, rect, image);
    }
}
