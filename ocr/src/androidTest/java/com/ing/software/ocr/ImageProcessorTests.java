package com.ing.software.ocr;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opencv.android.OpenCVLoader;

@RunWith(AndroidJUnit4.class)
public class ImageProcessorTests {

    // alias
    private final static Class<?> IP_CLASS = ImageProcessor.class;

    @Before
    public void initOpenCV() {
        OpenCVLoader.initDebug();
    }

    @Test
    public void findBiggestContourTest() throws Exception {
        //Context appContext = InstrumentationRegistry.getTargetContext();
        //ImageProcessor ip = new ImageProcessor(getBitmap(0));
        //Mat img = getField(ip, "srcImg");
        //invoke(IP_CLASS, "findBiggestContours", img);
    }
}
