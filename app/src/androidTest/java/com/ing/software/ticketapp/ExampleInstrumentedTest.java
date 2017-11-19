package com.ing.software.ticketapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        /* Commented waiting for a proper test, this one hangs indefinitely
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        OcrAnalyzer ocrAnalyzer = new OcrAnalyzer();
        while (ocrAnalyzer.initialize(appContext) != 0)
            Thread.sleep(10);
        final DataAnalyzer dataAnalyzer = new DataAnalyzer();


        // number of Tickets
        int imgsTot = 1;
        // Object used to lock current thread until all photos are processed.
        final CountDownLatch cdl = new CountDownLatch(imgsTot);


        // todo: assegnare la bitmap con una foto di uno scontrino.
        // cancellare questa riga e assegnare una bitmap valida.
        Bitmap bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);


        ocrAnalyzer.getOcrResult(bitmap, new OnOcrResultReadyListener() {
            @Override
            public void onOcrResultReady(OcrResult result) {
                dataAnalyzer.getTicket(result, new OnTicketReadyListener() {
                    @Override
                    public void onTicketReady(Ticket ticket) {


                        //todo: controllare che i dati contenuti in ticket sono corretti.


                        //decreases the counter
                        cdl.countDown();
                    }
                });
            }
        });

        //Thread lock. If the counter reaches zero, the thread unlocks and the test ends.
        cdl.await();
        */
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ing.software.ticketapp", appContext.getPackageName());
    }
}
