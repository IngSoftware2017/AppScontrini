package com.ing.software.ticketapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ApplicationSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setTitle("Impostazioni");
        setContentView(R.layout.activity_application_settings);
    }
}
