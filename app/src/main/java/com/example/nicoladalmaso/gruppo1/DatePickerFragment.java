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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Calendar;

/** This class is fully developed by Nicola Dal Maso
 * Datepicker management
 */

public class  DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    int id;
    boolean check;


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
        check=false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        /**
         * edit by Lazzarin
         * check if we are set Start or End date, and set min/max date
         */

        //variable check is used to communicate with OnDataSet method about the DatePicker chosen.
        check=false;
        int flag=Singleton.getInstance().getStartFlag();
        Log.d("flag read by datePicker",flag+"");
        switch(flag) {

            case 1:   //DatePicker of end Date..set min date
            {
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                if (Singleton.getInstance().getStartDate() != null)
                {

                    long start = Singleton.getInstance().getStartDate().getTime();
                    dialog.getDatePicker().setMinDate(start);
                }
                else
                    Log.d("error on StartDate", "date is null");
                break;
            }
            case 0: //DatePicker of startDate


            {
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                Singleton.getInstance().setStartFlag(1);
                check = true;   // tell onDateSet to write on startDate
                break;
            }
            case 2:
                //DatePicker of editTicket(2)
            {


                long start = Singleton.getInstance().getStartDate().getTime();
                dialog.getDatePicker().setMinDate(start);
                long end = Singleton.getInstance().getEndDate().getTime();
                dialog.getDatePicker().setMaxDate(end);
                break;
            }
            default: Log.d("error","value of flag unknown");
        }
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d("TextInputEditTextID", "ID: "+id+", R.id: "+R.id.input_missionStart);
        TextView textView = (TextView) getActivity().findViewById(id);
        textView.setText(day + "/" + (month+1) + "/" + year);
        //lazzarin
        SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        if(check)
            {
            try{
            Singleton.getInstance().setStartDate(dateformat.parse(day + "/" + (month+1) + "/" + year));
                 }
            catch (ParseException e) {
                e.printStackTrace();
            }
            check=false;
        }


    }
}