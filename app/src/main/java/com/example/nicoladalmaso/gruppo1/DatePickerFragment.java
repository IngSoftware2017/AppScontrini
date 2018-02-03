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

        /*TO DO:
        implementare il tutto a ritroso per max Data(data odierna)
        */
        //variable check is used to communicate with OnDataSet method about the DatePicker chosen.
        check=false;
        int flag=Singleton.getInstance().getStartFlag();
        Log.d("flag read by datePicker",flag+"");
        if(flag==1)
            //DatePicker of end Date..set min date
            {
            if(Singleton.getInstance().getStartDate()!=null) {

                long start=Singleton.getInstance().getStartDate().getTime();
                dialog.getDatePicker().setMinDate(start);
                Log.d("min date","set");
                }
            else
                Log.d("error on StartDate","date is null");
            Log.d("flag by datePikerEnd",Singleton.getInstance().getStartFlag()+"");

            }
        else
            //DatePicker of startDate
            {
                Singleton.getInstance().setStartFlag(1);
                check=true;   //per dire al OndateSet che deve aggiornare la data Minima
                Log.d("flag by datePikerStart", Singleton.getInstance().getStartFlag() + "");
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
        }

        //   missionID = Singleton.getInstance().getMissionID();
    }
}