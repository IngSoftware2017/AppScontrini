package com.ing.software.ocr;

import android.util.Log;

import org.junit.Test;

import java.lang.reflect.Method;
import java.security.SecureRandom;

import static junit.framework.Assert.assertEquals;

/**
 *
 */

public class PerformanceTests {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static final String from = "totale";
    static SecureRandom rnd = new SecureRandom();


    @Test
    public void levTest() throws Exception {
        final long startTime = System.nanoTime();
        for (int i = 0; i<100; ++i) {
            Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
            method.setAccessible(true);
            int result = (int) method.invoke(null, from, randomString(10));
        }
        long endTime = System.nanoTime();
        double duration = ((double) (endTime - startTime)) / 1000000000;
        Log.d("EXECUTION TIME : ", duration + "");
        assertEquals(0, 0);
    }

    String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
