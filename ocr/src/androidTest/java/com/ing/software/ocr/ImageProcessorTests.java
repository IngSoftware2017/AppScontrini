package com.ing.software.ocr;

import android.graphics.RectF;
import android.support.test.runner.AndroidJUnit4;
import android.util.SizeF;

import com.ing.software.common.ExceptionHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;

import static com.ing.software.ocr.ImageProcessor.*;
import static com.ing.software.common.Reflect.*;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ImageProcessorTests {
    ExceptionHandler excHdlr;

    float OCR_MARGIN_MUL;

    @Before
    public void init() {
        excHdlr = new ExceptionHandler(e -> System.out.println(e.getMessage()));
        excHdlr.tryRun(() -> {
            //cast chaining is needed to avoid ClassCastException
            OCR_MARGIN_MUL = (float)((double)((Double)getField(ImageProcessor.class, "OCR_MARGIN_MUL")));
        });
        OpenCVLoader.initDebug();
    }

    @Test
    public void expandCoordinatesTestNoMargin() {
        RectF rect = expandRectCoordinates(new RectF(0.25f,0.25f,0.75f,0.75f),
                new SizeF(10, 20), 0.5);
        assertEquals(new RectF(2.5f, 5f, 7.5f, 15f), rect);
    }

    @Test
    public void expandCoordinatesTestWIthMargin() {
        RectF rect = expandRectCoordinates(new RectF(0.25f,0.25f,0.75f,0.75f),
                new SizeF(15, 25), 0.25);
        assertEquals(new RectF(5f, 7.5f, 10f, 17.5f), rect);
    }


    @Test
    public void normalizeCoordinatesTest() {
        float origWidth = 10, origHeight = 20;
        float margin = origWidth * OCR_MARGIN_MUL;
        SizeF imgSize = new SizeF(origWidth + 2 * margin, origHeight + 2 * margin);
        //NB: intentional use of width for both right and bottom parameters, as width is the shortest of the two dimensions.
        RectF rect = normalizeCoordinates(new RectF(0,0, margin, margin), imgSize);
        assertEquals(new RectF(-OCR_MARGIN_MUL, -OCR_MARGIN_MUL / 2, 0, 0), rect);
    }
}
