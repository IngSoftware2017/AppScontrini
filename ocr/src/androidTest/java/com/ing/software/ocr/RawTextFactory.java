package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.gms.vision.text.Text;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.List;

/**
 *
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
            Point b = new Point(rect.right, rect.bottom);
            return new Point[]{a, b};
        }

        @Override
        public List<? extends Text> getComponents() {
            return null;
        }
    };
    RawImage rawImage = new RawImage(image);
    return new RawText(text, rawImage);
    }
}
