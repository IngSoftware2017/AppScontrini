package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class CheckPhotoActivity extends Activity {

    OcrManager ocrManager;
    Ticket result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_photo);
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

        EditText checkPrice = (EditText)findViewById(R.id.input_checkTotal);
        if(result.amount != null)
            checkPrice.setText(result.amount.toString());

        Button btnRedo = (Button)findViewById(R.id.btnCheck_retry);
        Button btnOK = (Button)findViewById(R.id.btnCheck_allow);

        btnRedo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }
        );
    }
}
