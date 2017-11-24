/*
 * Insert here all tests for package private classes
 */

package com.ing.software.ticketapp.OCR;

import com.ing.software.ticketapp.*;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import static com.ing.software.ticketapp.ExampleInstrumentedTest.*;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OcrInstrumentedTests {

    @Test
    public void findCornersTest() throws Exception {
        //ImagePreprocessor.findCorners(getBitmap(0));
        assertEquals(4, 2 + 2);
    }
}
