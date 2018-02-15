package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ing.software.common.Ticket;
import com.ing.software.ocr.ImageProcessor;
import com.ing.software.ocr.OcrManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import database.DataManager;
import database.TicketEntity;

/**
 * This class is fully developed by Nicola Dal Maso
 */

public class CheckPhotoActivity extends Activity {

    OcrManager ocrManager;
    String root;
    DataManager DB;
    Ticket OCR_result;
    EditText checkName;
    EditText checkPrice;
    EditText checkPeople;
    CheckBox checkRefundable;
    Bitmap finalBitmap;
    ProgressBar waitOCR;
    Button btnOK;
    Button btnRedo;
    ImageView checkPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_photo);
        //Activity operations
        initializeComponents();
        setFinalBitmap();
        setPhotoTaken();
        startOCRProcess();
    }

    /** Dal Maso
     * Initalize all components
     */
    private void initializeComponents(){
        root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        DB = new DataManager(this.getApplicationContext());
        //elements initialize
        checkPhotoView = (ImageView)findViewById(R.id.checkPhoto_image);
        checkPrice = (EditText)findViewById(R.id.input_checkTotal);
        checkPeople = (EditText)findViewById(R.id.input_numPeople);
        checkName = (EditText)findViewById(R.id.input_checkName);
        btnRedo = (Button)findViewById(R.id.btnCheck_retry);
        checkRefundable = (CheckBox)findViewById(R.id.check_Refundable);
        btnOK = (Button)findViewById(R.id.btnCheck_allow);
        waitOCR = (ProgressBar)findViewById(R.id.progressBarOCR);
        waitOCR.setVisibility(View.VISIBLE);
        //OCR initialize
        ocrManager = new OcrManager();
        while (ocrManager.initialize(this) != 0) { // 'this' is the context
            try {
                //On first run vision library will be downloaded
                Toast.makeText(this, "Downloading library...", Toast.LENGTH_LONG).show();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Redo photo button
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //Save photo button
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveThisTicket(finalBitmap);
                finish();
            }
        });
        //Waiting OCR
        btnOK.setClickable(false);
    }

    /** Dal Maso
     * set the bitmap to the imageview
     */
    private void setPhotoTaken(){
        Display display = getWindowManager().getDefaultDisplay();
        checkPhotoView.setImageBitmap(Bitmap.createScaledBitmap(finalBitmap, display.getWidth(), display.getHeight(), true));
    }

    /** Dal Maso
     * rotate the bitmap of 90 degrees
     */
    private void setFinalBitmap(){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap btm = AppUtilities.fromByteArrayToBitmap(Singleton.getInstance().getTakenPicture());
        finalBitmap = Bitmap.createBitmap(btm, 0, 0, btm.getWidth(), btm.getHeight(), matrix, true);
    }

    /** Dal Maso
     * OCR photo analyzing and values set
     */
    private void startOCRProcess(){
        // OCR asynchronous implementation:
        ImageProcessor imgProc = new ImageProcessor(finalBitmap);
        ocrManager.getTicket(imgProc, result -> {
            //Thread UI control reservation
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(result.amount != null) {
                        checkPrice.setText(result.amount.toString());
                    }
                    waitOCR.setVisibility(View.INVISIBLE);
                }
            });
            OCR_result = result;
            Log.d("SHAPE", result.rectangle.toString());
            //enable save button
            btnOK.setClickable(true);
        });
    }

    /** Dal Maso
     * Save the image and one original copy
     * @param imageToSave image to save
     */
    public void saveThisTicket(Bitmap imageToSave){
        //avoid multiple saves
        btnOK.setClickable(false);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String fname = imageFileName+".jpg";
        File file = new File(root, fname);
        File originalPhoto = new File(root,fname+"orig");
        final Uri uri=Uri.fromFile(file);
        if (file.exists())
            file.delete();
        if(originalPhoto.exists())
            originalPhoto.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            FileOutputStream outOriginal = new FileOutputStream(originalPhoto);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            TicketEntity thisTicket = new TicketEntity();

            if(OCR_result.date == null)
                thisTicket.setDate(Calendar.getInstance().getTime());
            else
                thisTicket.setDate(OCR_result.date);

            thisTicket.setTagPlaces(Short.parseShort(checkPeople.getText().toString()));

            if(checkRefundable.isChecked()){
                thisTicket.setRefundable(true);
            }
            else{
                thisTicket.setRefundable(false);
            }

            thisTicket.setFileUri(uri);
            try {
                thisTicket.setAmount(BigDecimal.valueOf(Double.parseDouble(checkPrice.getText().toString())));
            }
            catch (Exception e){
                thisTicket.setAmount(null);
            }
            thisTicket.setTitle(checkName.getText().toString());

            thisTicket.setMissionID(Singleton.getInstance().getMissionID());
            long id = DB.addTicket(thisTicket);
            Log.d("Aggiunto ticket", ""+id);

            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            imageToSave.compress(Bitmap.CompressFormat.JPEG,100, outOriginal);
            outOriginal.flush();
            outOriginal.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
