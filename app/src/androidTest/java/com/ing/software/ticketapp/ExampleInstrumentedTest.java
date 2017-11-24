package com.ing.software.ticketapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ing.software.ticketapp.OCR.DataAnalyzer;
import com.ing.software.ticketapp.OCR.OnTicketReadyListener;
import com.ing.software.ticketapp.common.Ticket;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static junit.framework.Assert.*;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
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

    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        final Semaphore sem = new Semaphore(0);
        int imgsTot = getTotImgs();

        DataAnalyzer analyzer = new DataAnalyzer();
        while (analyzer.initialize(appContext) != 0)
            Thread.sleep(10);

        for (int i = 0; i < imgsTot; i++) {
            final Ticket target = null; //todo: initialize
            analyzer.getTicket(getBitmap(i), new OnTicketReadyListener() {
                @Override
                public void onTicketReady(Ticket ticket) {

                    //todo: compare Ticket to dataset
                    //assertEquals(target, ticket);

                    sem.release();
                }
            });
            sem.acquire();
        }
    }
}
