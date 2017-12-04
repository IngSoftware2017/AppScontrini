package com.sw.ing.gestionescontrini;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import database.DataManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataManager dbm = new DataManager(this);
    }
}
