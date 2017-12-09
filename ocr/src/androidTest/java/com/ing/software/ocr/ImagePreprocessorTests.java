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
import static org.opencv.core.CvType.CV_32SC2;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

@RunWith(AndroidJUnit4.class)
public class ImagePreprocessorTests {

    @Test
    public void findContoursTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        //invoke(ImagePreprocessor.class, "findBiggestContour", bitmapToMatBGR(getBitmap(0)));
    }

    @Test
    public void testOpencv4Android() throws Exception {
        org.opencv.core.Mat a = new Mat(2, 3, CV_32SC2);
        Mat b = a.clone();
    }
}
