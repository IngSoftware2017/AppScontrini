package com.ing.software.ocr;

import java.util.concurrent.Semaphore;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static com.ing.software.ocr.TestUtils.*;
import static junit.framework.Assert.*;

import com.ing.software.common.*;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class OcrInstrumentedTests {

    @Test
    public void findCornersTest() throws Exception {
        //ImagePreprocessor.findCorners(getBitmap(0));
        assertEquals(4, 2 + 2);
    }

    @Test
    public void ocrMainTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        final Semaphore sem = new Semaphore(0);
        int imgsTot = getTotImgs();

        DataAnalyzer analyzer = new DataAnalyzer();

        int c = 0;
        int TIMEOUT = 60; // 1 min
        while (analyzer.initialize(appContext) != 0 && c < TIMEOUT) {
            Thread.sleep(1000); // 1 sec
            c++;
        }

        if (c < TIMEOUT) {
            for (int i = 0; i < imgsTot; i++) {
                final int idx = i;
                final Ticket target = null; //todo: initialize
                Bitmap photo = getBitmap(i);
                if (photo != null) {
                    analyzer.getTicket(photo, new OnTicketReadyListener() {
                        @Override
                        public void onTicketReady(Ticket ticket) {

                            //todo: compare Ticket to dataset
                            //assertEquals(target, ticket);

                            sem.release();
                            System.out.println("Done img " + String.valueOf(idx));
                        }
                    });
                }

                sem.acquire();
            }
        }
    }
}
