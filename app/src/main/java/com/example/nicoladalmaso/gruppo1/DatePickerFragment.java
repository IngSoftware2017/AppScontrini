package com.example.nicoladalmaso.gruppo1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.time.Year;
import java.util.Calendar;

/**
 * Created by Nicola on 22/12/2017.
 */

public class  DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    int id;

    public static DatePickerFragment newInstance(TextView textView) {
        DatePickerFragment f = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("textView", textView.getId());
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getArguments().getInt("textView");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d("TextInputEditTextID", "ID: "+id+", R.id: "+R.id.input_missionStart);
        TextView textView = (TextView) getActivity().findViewById(id);
        textView.setText(day + "/" + (month+1) + "/" + year);
    }
}