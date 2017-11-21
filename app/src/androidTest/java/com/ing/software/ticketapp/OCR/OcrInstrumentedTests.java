/*
 * Insert here all tests for package private classes
 */

package com.ing.software.ticketapp.OCR;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OcrInstrumentedTests {

    @Test
    public void useAppContext() throws Exception {
        //There must be a test
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ing.software.ticketapp", appContext.getPackageName());
    }
}
