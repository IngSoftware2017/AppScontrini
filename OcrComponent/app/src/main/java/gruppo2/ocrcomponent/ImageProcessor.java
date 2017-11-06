package gruppo2.ocrcomponent;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageProcessor {

    //Una queue thread-safe (puo' essere usata contemporaneamente dal thread principale e dal thread interno)
    private Queue<Bitmap> frameQueue = new ConcurrentLinkedQueue<>();
    //private Thread frameThread = null;
    private OnOcrResultReadyListener ocrResultCb = null;

//    class AsyncRunnable implements Runnable {
//
//        @Override
//        public void run() {
//            //todo scrivere codice asincrono
//            while (ocrResultCb == null) {
//
//            }
//            ocrResultCb.onOcrResultReady(new OcrResult());
//        }
//    }

    public ImageProcessor(Context ctx){
    }

    /**
     * Aggiungi un frame alla queue per l'elaborazione.
     * @param frame Bitmap da aggiungere. Non nullo.
     */
    public void addFrame(Bitmap frame) {
//        if (frameThread == null) {
//            //fai partire il thread
//            new Thread(new AsyncRunnable()).start();
//        }
        frameQueue.add(frame);
    }

    /**
     * Ottieni l'oggetto OcrResult a partire da tutti i frame aggiunti precedentemente
     * a questa chiamata e sucessivi all'ultima chiamata di getOcrResults.
     * @param resultCb Callback per ottenere l'OcrResult. Non nullo.
     */
    public void getOcrResult(OnOcrResultReadyListener resultCb){
        ocrResultCb = resultCb;
    }

}
