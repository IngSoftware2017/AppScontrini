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
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        //lazzarin
        //check if we're setting start or end date
        boolean flag=Singleton.getInstance().getStartFlag();
        Log.d("flag letto da datePiker",flag+"");
        if(flag)
        //DatePicker of end Date..set min date
            {
            if(Singleton.getInstance().getStartDate()!=null) {
                Log.d("min data","impostata");
                long start=Singleton.getInstance().getStartDate().getTime();
                dialog.getDatePicker().setMinDate(start);

                }
                else Log.d("errore nella data","x");
            Log.d("flag by datePikerEnd",Singleton.getInstance().getStartFlag()+"");
            }
        else
            //DatePicker of startDate
        {

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
        if(!Singleton.getInstance().getStartFlag())
            {
            try{
            Singleton.getInstance().setStartDate(dateformat.parse(day + "/" + (month+1) + "/" + year));
                Singleton.getInstance().setStartFlag(true); }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //   missionID = Singleton.getInstance().getMissionID();
    }
}