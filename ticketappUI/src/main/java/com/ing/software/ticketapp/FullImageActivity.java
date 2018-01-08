package com.ing.software.ticketapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import database.DataManager;
import database.TicketEntity;


/**
 * @author Matteo Salvagno
 */


public class FullImageActivity extends AppCompatActivity {

    ImageView fullImage;

    DataManager DB = DataManager.getInstance(this);

    TicketEntity te = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long id = getIntent().getLongExtra("ID", 0);
        if(id != 0)
        {
            te = DB.getTicket(id);
            fullImage = findViewById(R.id.full_image);
            //Glide.with(this).load(te.getFileUri()).into(fullImage);
        }
        setContentView(R.layout.activity_full_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

}
