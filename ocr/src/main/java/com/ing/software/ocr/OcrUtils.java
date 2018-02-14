package com.ing.software.ocr;

import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Log;

import com.ing.software.ocr.OcrObjects.OcrText;

import java.util.List;

import static com.ing.software.ocr.OcrVars.*;

/**
 * Utility class
 */
//ZAGLIA: we could move extendRect elsewhere and use this class only for debugging purposes.
public class OcrUtils {

    /**
     * @author Michelon
     * Logs messages only if debug is enabled
     * @param level int >= 0, only messages with level <= OcrVars.LOG_LEVEL are logged
     * @param tag tag of the message to log, must be less than 24 chars long
     * @param message message to log
     */
    // ZAGLIA: Consider moving this inside CommonUtils
    public static void log(@IntRange(from = 0) int level, @Size(max = 23) String tag, String message) {
        if (OcrVars.IS_DEBUG_ENABLED && level <= LOG_LEVEL)
            Log.d(tag, message);
    }

    /**
     * List in debug log blocks parsed (detection + grid)
     * @param texts List of texts. Not null.
     */
    static void listEverything(@NonNull List<OcrText> texts) {
            /*
            for (RawText text : texts) {
                OcrUtils.log(2, "FULL LIST: ", text.getValue());
            }
            */
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nINTRODUCTION");
            for (OcrText text : texts) {
                if (text.getTags().contains(INTRODUCTION_TAG))
                    OcrUtils.log(2, "introduction", text.text());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nPRODUCTS");
            for (OcrText text : texts) {
                if (text.getTags().contains(PRODUCTS_TAG))
                    OcrUtils.log(2, "products", text.text());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nPRICES");
            for (OcrText text : texts) {
                if (text.getTags().contains(PRICES_TAG))
                    OcrUtils.log(2, "prices", text.text());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nCONCLUSION");
            for (OcrText text : texts) {
                if (text.getTags().contains(CONCLUSION_TAG))
                    OcrUtils.log(2, "conclusion", text.text());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################");
        }

    /**
     * @author Michelon
     * Create a new rect extending source rect with chosen percentage (on width and height of chosen rect)
     * Or extending of chosen pixels on both sides (if param is negative)
     * Note: Min value for top and left is 0
     * @param rect source rect. Not null
     * @param percentHeight chosen percentage for height. \pixels if negative
     * @param percentWidth chosen percentage for width. \pixels if negative
     * @return new extended rectangle
     */
    public static RectF extendRect(@NonNull RectF rect, int percentHeight, int percentWidth) {
        float top;
        float bottom;
        float right;
        float left;
        if (percentHeight > 0) {
            float extendedHeight = rect.height() * percentHeight / 100;
            top = rect.top - extendedHeight / 2;
            bottom = rect.bottom + extendedHeight/2;
        } else {
            top = rect.top - Math.abs(percentHeight);
            bottom = rect.bottom + Math.abs(percentHeight);
        }
        if (percentWidth > 0) {
            float extendedWidth = rect.width() * percentWidth / 100;
            left = rect.left - extendedWidth / 2;
            right = rect.right + extendedWidth/2;
        } else {
            left = rect.left - Math.abs(percentWidth);
            right = rect.right + Math.abs(percentWidth);
        }
        if (left<0)
            left = 0;
        if (top < 0)
            top = 0;
        //Doesn't matter if bottom and right are outside the photo
        return new RectF(left, top, right, bottom);
    }
}
