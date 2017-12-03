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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.DataAnalyzer;
import com.ing.software.ocr.OcrUtils;
import com.ing.software.ocr.OnTicketReadyListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ing.software.ocrtestapp.StatusVars.*;


public class MainActivity extends AppCompatActivity implements OcrResultReceiver.Receiver {

    final OcrResultReceiver mReceiver = new OcrResultReceiver(new Handler());
    private static DataAnalyzer dataAnalyzer;
    private final String testFolder = "/TestOCR";
    private int counter = 0;
    private final PermissionsHandler permissionsHandler = new PermissionsHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!permissionsHandler.hasStoragePermission())
            permissionsHandler.requestStoragePermission();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dataAnalyzer = new DataAnalyzer();
        dataAnalyzer.initialize(this); //should check == 0
        mReceiver.setReceiver(this);
        OcrUtils.log(1, "MAIN", "STARTING: " + getDate());
        List<File> listFile = loadImage();
        final List<String> listNames = new ArrayList<>();
        for (File aFile : listFile)
            listNames.add(aFile.getAbsolutePath());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (counter == listNames.size())
                    dataAnalyzer.release();
                if (counter < listNames.size()) {
                    Intent intent = new Intent(MainActivity.this, TestService.class);
                    intent.putExtra("receiver", mReceiver);
                    OcrUtils.log(1, "OcrHandler", "ANALYZING: " + listNames.get(counter));
                    intent.putExtra("imagePath", listNames.get(counter));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case STATUS_RUNNING:
                Toast.makeText(this, "Starting img: " + resultData.getString(IMAGE_RECEIVED), Toast.LENGTH_LONG).show();
                break;
            case STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                Toast.makeText(this, "Done. \nAmount is: " + resultData.getString(AMOUNT_RECEIVED) +
                        "\nElapsed time is: " + resultData.getString(DURATION_RECEIVED) + " seconds", Toast.LENGTH_LONG).show();
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(ERROR_RECEIVED);
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private String getDate() {
        Calendar cal = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(cal.getTime());
    }

    private List<File> loadImage() {
        File appDir = new File(Environment.getExternalStorageDirectory().toString() + testFolder);
        if (appDir.isDirectory())
            Log.e("listFileInfolder", "File is directory. Path is: " + appDir.getPath());
        else
            Log.e("listFileInfolder", "File is not directory. Path is: " + appDir.getPath());
        return listFilesInFolder(appDir);
    }

    public List<File> listFilesInFolder(File folder) {
        List<File> fileInFolder = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            fileInFolder.add(fileEntry);
        }
        Log.e("listFileInfolder", "Files in directory are: " + fileInFolder.size());
        return fileInFolder;
    }



    public static class TestService extends IntentService {

        public TestService() {
            super("TestService");
        }

        public TestService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(final Intent workIntent) {
            final long startTime = System.nanoTime();
            OcrUtils.log(1, "TestService", "Entering service");
            final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
            final String testPic = workIntent.getExtras().getString("imagePath");
            Bitmap testBmp = getBitmapFromFile(getFileFromPath(testPic));
            //test = OcrAnalyzer.getCroppedPhoto(test, this);
            final Bundle bundle = new Bundle();
            if (testBmp != null) {
                bundle.putString(IMAGE_RECEIVED, testPic);
                receiver.send(STATUS_RUNNING, bundle);
                dataAnalyzer.getTicket(testBmp, new OnTicketReadyListener() {
                    @Override
                    public void onTicketReady(Ticket result) {
                        OcrUtils.log(1, "OcrHandler", "Detection complete");
                        long endTime = System.nanoTime();
                        double duration = ((double) (endTime - startTime)) / 1000000000;
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
                    }
                });
            } else {
                bundle.putString("ErrorMessage", "Error null image");
                receiver.send(STATUS_ERROR, bundle);
            }
            this.stopSelf();
        }

        private File getFileFromPath(String path) {
            return new File(path);
        }

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
