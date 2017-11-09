package gruppo2.ocrcomponent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.res.ResourcesCompat;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
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
    }
}
