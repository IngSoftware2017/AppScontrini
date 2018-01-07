package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by fedet on 07/01/2018.
 */

public class ExportSpinner extends AppCompatSpinner implements AdapterView.OnItemSelectedListener{

    public ExportSpinner(Context context) {
        super(context);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}