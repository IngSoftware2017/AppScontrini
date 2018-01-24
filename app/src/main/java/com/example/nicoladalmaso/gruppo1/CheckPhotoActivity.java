package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ing.software.common.Ticket;
import com.ing.software.ocr.ImageProcessor;
import com.ing.software.ocr.OcrManager;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import database.DataManager;
import database.TicketEntity;

public class CheckPhotoActivity extends Activity {

    OcrManager ocrManager;
    String root;
    DataManager DB;
    Ticket result;
    EditText checkName;
    EditText checkPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_photo);
        root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        DB = new DataManager(this.getApplicationContext());
        String filePath = getIntent().getStringExtra("path");
        File file = new File(filePath);
        ImageView checkPhotoView = (ImageView)findViewById(R.id.checkPhoto_image);
        //Ticket image bitmap set
        Glide.with(getApplicationContext())
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(checkPhotoView);

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

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        ImageProcessor imgProc = new ImageProcessor(bitmap);
        result = ocrManager.getTicket(imgProc);

        checkPrice = (EditText)findViewById(R.id.input_checkTotal);
        checkName = (EditText)findViewById(R.id.input_checkName);

        if(result.amount != null)
            checkPrice.setText(result.amount.toString());

        Button btnRedo = (Button)findViewById(R.id.btnCheck_retry);
        Button btnOK = (Button)findViewById(R.id.btnCheck_allow);

        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               saveThisTicket(bitmap);
               finish();
           }
        });
    }

    public void saveThisTicket(Bitmap imageToSave){
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
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            TicketEntity thisTicket = new TicketEntity();

            if(result.date == null)
                thisTicket.setDate(Calendar.getInstance().getTime());
            else
                thisTicket.setDate(result.date);

            thisTicket.setFileUri(uri);
            thisTicket.setAmount(new BigDecimal(checkPrice.getText().toString()));
            thisTicket.setTitle(checkName.getText().toString());

            thisTicket.setMissionID(Singleton.getInstance().getMissionID());
            DB.addTicket(thisTicket);

            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            FileOutputStream outOriginal = new FileOutputStream(originalPhoto);
            imageToSave.compress(Bitmap.CompressFormat.JPEG,90, outOriginal);
            outOriginal.flush();
            outOriginal.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
