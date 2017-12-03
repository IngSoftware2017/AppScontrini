package com.sw.ing.iteratorlibrary.iterator;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Federico Taschin on 02/12/2017.
 */

public class ImageLoader {
    private static final String IMAGE_DIR = "photos";

    /**
     * Created by Federico Taschin
     * @param ticketInfo the ticket of which the bitmap is needed
     * @return the corresponding Bitmap
     * @throws IOException if no bitmap for the ticketInfo exists
     */
    public static Bitmap getBitmap(TicketInfo ticketInfo) throws IOException {
        AssetManager mgr = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets();
        InputStream is = mgr.open(IMAGE_DIR + "/" + String.valueOf(ticketInfo.getID()) + ".jpg");
        Bitmap bm = BitmapFactory.decodeStream(is);
        return bm;
    }
}
