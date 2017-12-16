package com.ing.software.ocr;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ing.software.common.Reflect;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.ing.software.ocr.TestUtils.*;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

@RunWith(AndroidJUnit4.class)
public class ImagePreprocessorTests {

    // alias
    private final static Class<?> IP_CLASS = ImagePreprocessor.class;

    @Before
    public void initOpenCV() {
        OpenCVLoader.initDebug();
    }

    @Test
    public void findBiggestContourTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ImagePreprocessor ip = new ImagePreprocessor(getBitmap(0));
        Mat img = Reflect.getField(ip, "srcImg");
        //invoke(IP_CLASS, "findBiggestContours", img);
    }
}
