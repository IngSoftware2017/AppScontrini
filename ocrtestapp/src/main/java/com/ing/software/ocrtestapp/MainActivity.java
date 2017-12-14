package com.ing.software.ocrtestapp;


import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.software.common.Ref;
import com.ing.software.common.Ticket;
import com.ing.software.common.TicketError;
import com.ing.software.ocr.ImagePreprocessor;
import com.ing.software.ocr.OcrManager;
import com.ing.software.ocr.OcrUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.ing.software.ocrtestapp.StatusVars.*;

/**
 * This class analyze all pics in folder 'sdcard/TestOCR' for now it does not handle errors
 * (missing folder, invalid files, subdirectories etc).
 * When floating button is clicked, a new background service is created, with the path of a single
 * file.
 */
public class MainActivity extends AppCompatActivity implements OcrResultReceiver.Receiver {

    final OcrResultReceiver mReceiver = new OcrResultReceiver(new Handler());
    private static OcrManager ocrAnalyzer;
    static private final String testFolder = "/TestOCR";
    private int counter = 0;
    private final PermissionsHandler permissionsHandler = new PermissionsHandler(this);
    private static final Semaphore sem = new Semaphore(0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!permissionsHandler.hasStoragePermission())
            permissionsHandler.requestStoragePermission();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ocrAnalyzer = new OcrManager();
        while (ocrAnalyzer.initialize(this) != 0) {
            try {
                Toast.makeText(this, "Downloading library...", Toast.LENGTH_LONG).show();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mReceiver.setReceiver(this);
        OcrUtils.log(1, "MAIN", "STARTING: " + getDate());
        List<File> listFile = loadImage(testFolder);
        final List<String> listNames = new ArrayList<>();
        for (File aFile : listFile)
            listNames.add(aFile.getAbsolutePath());

        findViewById(R.id.run_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (counter == listNames.size()) {
                    ocrAnalyzer.release();
                    ++counter;
                    Snackbar.make(view, "DataAnalyzer released", Snackbar.LENGTH_LONG)
                            .setAction("Service", null).show();
                } else if (counter < listNames.size()) {
                    Intent intent = new Intent(MainActivity.this, TestService.class);
                    intent.putExtra("receiver", mReceiver);
                    ++counter;
                    startService(intent);
                    Snackbar.make(view, "Service started", Snackbar.LENGTH_LONG)
                            .setAction("Service", null).show();
                } else {
                    Snackbar.make(view, "Nothing to analyze", Snackbar.LENGTH_LONG)
                            .setAction("Service", null).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!permissionsHandler.hasStoragePermission())
            permissionsHandler.requestStoragePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!permissionsHandler.resultGranted(requestCode, permissions, grantResults))
        {
            if (requestCode == PermissionsHandler.REQUEST_STORAGE_CODE)
            {
                String text = "No storage permission" ;
                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Shows results from service in a scrollview
     * @param resultCode code received
     * @param resultData bundle associated with code
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        //ScrollView scrollView = (ScrollView) findViewById(R.id.scroller);
        TextView tv = (TextView) findViewById(R.id.scrollerText);;
        String s = "";
        switch (resultCode) {
            case STATUS_RUNNING:
                //Toast.makeText(this, "Starting img: " + resultData.getString(IMAGE_RECEIVED), Toast.LENGTH_LONG).show();
                tv.append("\n");
                s = "\nStarting img: " + resultData.getString(IMAGE_RECEIVED);
                break;
            case STATUS_FINISHED:
                //Toast.makeText(this, "Done. \nAmount is: " + resultData.getString(AMOUNT_RECEIVED) +
                //        "\nElapsed time is: " + resultData.getString(DURATION_RECEIVED) + " seconds", Toast.LENGTH_LONG).show();
                s = "\nRectangle: " + resultData.getString(RECTANGLE_RECEIVED) +
                        "\nAmount is: " + resultData.getString(AMOUNT_RECEIVED) +
                        "\nElapsed time is: " + resultData.getString(DURATION_RECEIVED) + " seconds";
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(ERROR_RECEIVED);
                //Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                s = "\nError: " + error;
                break;
            case STATUS_AVERAGE:
                s = "\n\nAVERAGE TIME: " + resultData.getString(DURATION_RECEIVED) + " seconds\n";
                break;
        }
        tv.append(s);
        //scrollView.addView(tv);
    }

    /**
     * Get current date, for logging purposes
     * @return current date as string
     */
    private String getDate() {
        Calendar cal = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(cal.getTime());
    }

    /**
     * Retrieves list of files in dir
     * @param dir directory containing files. Must be a non empty directory with only files
     * @return list of files in current dir
     */
    static private List<File> loadImage(String dir) {
        File appDir = new File(Environment.getExternalStorageDirectory().toString() + dir);
        if (appDir.isDirectory())
            Log.e("listFileInfolder", "File is directory. Path is: " + appDir.getPath());
        else
            Log.e("listFileInfolder", "File is not directory. Path is: " + appDir.getPath());
        return listFilesInFolder(appDir);
    }

    static public List<File> listFilesInFolder(File folder) {
        List<File> fileInFolder = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            fileInFolder.add(fileEntry);
        }
        Log.e("listFileInfolder", "Files in directory are: " + fileInFolder.size());
        return fileInFolder;
    }


    /**
     * Service to manage requests to analyze tickets
     * When ticket is ready, send message to the receiver.
     */
    public static class TestService extends IntentService {

        public TestService() {
            super("TestService");
        }

        public TestService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(final Intent workIntent) {
            OcrUtils.log(1, "TestService", "Entering service");
            final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
            final Semaphore sem = new Semaphore(0);
            final Ref<Double> durationSum = new Ref<>(0.0);
            int validBitmaps = 0;
            List<File> listFile = loadImage(MainActivity.testFolder);
            for (File aFile : listFile) {
                Bitmap testBmp = getBitmapFromFile(aFile);
                //test = OcrAnalyzer.getCroppedPhoto(test, this);
                final long startTime = System.nanoTime();
                final Bundle bundle = new Bundle();
                if (testBmp != null) {
                    validBitmaps++;
                    OcrUtils.log(1, "OcrHandler", "____________________________________________________");
                    OcrUtils.log(1, "OcrHandler", "");
                    OcrUtils.log(1, "OcrHandler", "ANALYZING: " + aFile.getName());
                    OcrUtils.log(1, "OcrHandler", "_____________________________________________________");
                    bundle.putString(IMAGE_RECEIVED, aFile.getName());
                    receiver.send(STATUS_RUNNING, bundle);

                    ImagePreprocessor preproc = new ImagePreprocessor(testBmp);
                    preproc.findTicket(false, err -> {
                        String rectString = (err == TicketError.NONE ? "found" : "not found");
                        OcrUtils.log(1, "OcrHandler", "Rectangle: " + rectString);
                        bundle.putString(RECTANGLE_RECEIVED, rectString);
                        ocrAnalyzer.getTicket(preproc, result -> {
                            OcrUtils.log(1, "OcrHandler", "Detection complete");
                            long endTime = System.nanoTime();
                            double duration = ((double) (endTime - startTime)) / 1000000000;
                            durationSum.value += duration;
                            if (result.amount != null) {
                                OcrUtils.log(1, "OcrHandler", "Amount: " + result.amount);
                                bundle.putString(AMOUNT_RECEIVED, result.amount.toString());
                                bundle.putString(DURATION_RECEIVED, duration + "");
                                receiver.send(STATUS_FINISHED, bundle);
                            } else {
                                OcrUtils.log(1, "OcrHandler", "No amount found");
                                bundle.putString(AMOUNT_RECEIVED, "Not found.");
                                bundle.putString(DURATION_RECEIVED, duration + "");
                                receiver.send(STATUS_FINISHED, bundle);
                            }
                            sem.release();
                        });
                    });
                    try {
                        sem.acquire();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    bundle.putString("ErrorMessage", "Error null image");
                    receiver.send(STATUS_ERROR, bundle);
                }
            }
            final Bundle bundle = new Bundle();
            bundle.putString(DURATION_RECEIVED, String.valueOf(durationSum.value / validBitmaps));
            receiver.send(STATUS_AVERAGE, bundle);
            this.stopSelf();
        }

        private File getFileFromPath(String path) {
            return new File(path);
        }

        /**
         * Decode bitmap from file
         * @param file not null and must be an image
         * @return bitmap from file
         */
        private Bitmap getBitmapFromFile(File file) {
            FileInputStream fis = null;
            try {
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return BitmapFactory.decodeStream(fis);
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
