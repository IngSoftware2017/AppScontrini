package gruppo2.ocrcomponent;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageProcessor {

    private TextRecognizer ocrEngine = null;
    private OnOcrResultReadyListener ocrResultCb = null;

    public ImageProcessor(){}

    /**
     * Inizializza il componente.
     * Se questa chiamata ritorna -1, controllare se il dispositivo dispone di spazio libero sufficiente.
     * In caso positivo, riprovare a chiamare il metodo.
     * Dopo che questo metodo ha ritornato 0, sara' possibile chiamare getOcrResult.
     * @param ctx Contesto Android.
     * @return 0 se e' andato a buon fine, negativo altrimenti.
     */
    public int initialize(Context ctx) {
        //crea l'ocrEngine
        ocrEngine = new TextRecognizer.Builder(ctx).build();
        ocrEngine.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                //controlla che il callback sia stato assegnato
                if (ocrResultCb != null) {
                    SparseArray<TextBlock> sparseArr = detections.getDetectedItems();

                    //ottieni List da SparseArray
                    List<TextBlock> list = new ArrayList<>(sparseArr.size());
                    for (int i = 0; i < sparseArr.size(); i++)
                        list.add(sparseArr.valueAt(i));

                    //crea un OcrResult e chiama il callback
                    OcrResult newOcrResult = new OcrResult();
                    newOcrResult.blockList = list;
                    ocrResultCb.onOcrResultReady(newOcrResult);
                }
            }
        });

        return ocrEngine.isOperational() ? 0 : -1;
        //cause fallimento: il pacchetto GSM non e' stato scaricato per mancanza di tempo o di spazio.
    }

    /**
     * Ottieni l'oggetto OcrResult a partire da un Bitmap
     * a questa chiamata e sucessivi all'ultima chiamata di getOcrResults.
     * @param frame Bitmap da cui estrarre l'OcrResult. Non nullo.
     * @param resultCb Callback per ottenere l'OcrResult. Non nullo.
     */
    public void getOcrResult(Bitmap frame, OnOcrResultReadyListener resultCb){
        ocrResultCb = resultCb;
        ocrEngine.receiveFrame(new Frame.Builder().setBitmap(frame).build());
    }

}
