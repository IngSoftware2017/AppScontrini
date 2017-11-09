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

public class OcrAnalyzer {

    private TextRecognizer ocrEngine = null;
    private OnOcrResultReadyListener ocrResultCb = null;

    public OcrAnalyzer(){}

    //todo: find a way to test private methods
    /**
     * Repackage a collection of TextBlock into an OcrResult.
     * @param textBlocks SparseArray<TextBlock> from Detector.Processor<TextBlock>
     * @return new OcrResult.
     */
    OcrResult textBlocksToOcrResult(SparseArray<TextBlock> textBlocks) {
        OcrResult newOcrResult = new OcrResult();

        //SparseArray to List
        List<TextBlock> list = new ArrayList<>(textBlocks.size());
        for (int i = 0; i < textBlocks.size(); i++)
            list.add(textBlocks.valueAt(i));

        newOcrResult.blockList = list;
        return newOcrResult;
    }

    /**
     * Initialize the component.
     * If this call returns -1, check if the device has enough free disk space.
     * If so, try to call this method again.
     * When this method returned 0, it will be possible to call getOcrResult.
     * @param ctx Android context.
     * @return 0 if successful, negative otherwise.
     */
    public int initialize(Context ctx) {
        ocrEngine = new TextRecognizer.Builder(ctx).build();
        ocrEngine.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                //check if getOcrResult has been called to assign ocrResultCb.
                if (ocrResultCb != null) {
                    OcrResult newOcrResult = textBlocksToOcrResult(detections.getDetectedItems());
                    ocrResultCb.onOcrResultReady(newOcrResult);
                }
            }
        });

        return ocrEngine.isOperational() ? 0 : -1;
        //failure causes: GSM package is not yet downloaded due to lack of time or lack of space.
    }

    /**
     * Get an OcrResult from a Bitmap
     * @param frame Bitmap from which to extract an OcrResult. Not null.
     * @param resultCb Callback to get an OcrResult. Not null.
     */
    public void getOcrResult(Bitmap frame, OnOcrResultReadyListener resultCb){
        ocrResultCb = resultCb;
        ocrEngine.receiveFrame(new Frame.Builder().setBitmap(frame).build());
    }

}
