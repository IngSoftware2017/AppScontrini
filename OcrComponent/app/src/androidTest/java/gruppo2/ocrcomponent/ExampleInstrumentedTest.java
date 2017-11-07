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

        ImageProcessor imgProc = new ImageProcessor();
        while (imgProc.initialize(appContext) != 0)
            Thread.sleep(10);
        final DataAnalyzer dataAnalyzer = new DataAnalyzer();


        // numero di foto di scontrini
        int n_imgs = 1;
        // oggetto usato per bloccare il thread principale finche' tutte le foto sono state processate
        final CountDownLatch cdl = new CountDownLatch(n_imgs);


        // todo: assegnare la bitmap con una foto di uno scontrino.
        // cancellare questa riga e assegnare una bitmap valida.
        Bitmap bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);


        imgProc.getOcrResult(bitmap, new OnOcrResultReadyListener() {
            @Override
            public void onOcrResultReady(OcrResult result) {
                dataAnalyzer.getTicket(result, new OnTicketReadyListener() {
                    @Override
                    public void onTicketReady(Ticket ticket) {


                        //todo: controllare che i dati contenuti in ticket sono corretti.


                        //decrementa il contatore
                        cdl.countDown();
                    }
                });
            }
        });

        //Blocca il thread. Se il contatore arriva a zero, il thread si sblocca e il test si conclude
        cdl.await();
    }
}
