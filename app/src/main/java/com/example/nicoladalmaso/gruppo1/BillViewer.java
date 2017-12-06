package com.example.nicoladalmaso.gruppo1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BillViewer extends AppCompatActivity {

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_bill_viewer);
        Intent intent = getIntent();
        String imgPath = intent.getExtras().getString("imagePath");
        String imgName = intent.getExtras().getString("imageName");
        String imgLastMod = intent.getExtras().getString("imgLastMod");
        String imgPrice = intent.getExtras().getString("imgPrice");
        setTitle(imgName);
        //Title
        TextView billLastMod = (TextView)findViewById(R.id.billLastMod);
        billLastMod.setText(imgLastMod);
        //ImageName
        TextView billName = (TextView)findViewById(R.id.billName);
        billName.setText(imgName);
        //Total price
        TextView billPrice = (TextView)findViewById(R.id.billTotal);
        billPrice.setText(imgPrice);
        //Full image view
        ImageView imgView = (ImageView)findViewById(R.id.billImage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,bmOptions);
        imgView.setImageBitmap(bitmap);

    }
    //Dal Maso
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
