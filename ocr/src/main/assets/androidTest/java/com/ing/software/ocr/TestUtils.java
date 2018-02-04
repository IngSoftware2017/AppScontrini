package com.ing.software.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestUtils {

//    private static final String urlStr = "https://docs.google.com/uc?id=11bZwcT1EkxaVor0Ux8Houl9JFD8Tpkoc&export=download";
//
//    public static Bitmap getDummyBitmap() {
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            return BitmapFactory.decodeStream(input);
//        } catch (IOException e) {
//            // Log exception
//            return null;
//        }
//    }

    public static final String folder = "photos";

    public static int getTotImgs() throws Exception {
        AssetManager mgr = InstrumentationRegistry.getInstrumentation()
                .getContext().getResources().getAssets();
        return mgr.list(folder).length;
    }

    public static Bitmap getBitmap(int i) throws Exception {
        AssetManager mgr = InstrumentationRegistry.getInstrumentation()
                .getContext().getResources().getAssets();
        if (i < getTotImgs())
            return BitmapFactory.decodeStream(mgr.open(folder + "/" + String.valueOf(i) + ".jpg"));
        return null;
    }
}
