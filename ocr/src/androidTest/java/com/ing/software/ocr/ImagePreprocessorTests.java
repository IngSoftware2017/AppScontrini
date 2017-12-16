package com.ing.software.ocr;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ing.software.common.Ticket;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.ing.software.common.Reflect.*;
import static com.ing.software.ocr.TestUtils.*;
import static com.ing.software.ocr.ImagePreprocessor.*;

@RunWith(AndroidJUnit4.class)
public class ImagePreprocessorTests {

    @Test
    public void findContoursTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        invoke(ImagePreprocessor.class, "findBiggestContour", bitmapToMatBGR(getBitmap(0)));
    }
}
