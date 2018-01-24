package com.example.nicoladalmaso.gruppo1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class CheckPhotoActivity extends AppCompatActivity {

    byte[] photo;
    Bitmap btmPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_photo);
        Intent intent = getIntent();
        photo = intent.getExtras().getByteArray("photo");
        btmPhoto = AppUtilities.fromByteArrayToBitmap(photo);
    }
}
