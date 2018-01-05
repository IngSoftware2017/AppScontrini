package com.example.nicoladalmaso.gruppo1;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Cristian on 02/01/2018.
 */

public class AppUtilities {

    /**Lazzarin
     * check if start date is before/equals of finish date.
     * @param start date on format gg/MM/yyyy
     * @param finish date on finish gg/MM/yyyy
     * @return true if finish is a date later than start, false otherwise
     */
    public static boolean checkDate(String start, String finish)
    {
        SimpleDateFormat basicFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatCheck = new SimpleDateFormat("yyyyMMdd");
        try {


            Log.d("dateOriginStart",start);

            Log.d("dateOriginFinish",finish);
            Date in=basicFormat.parse(start);
            Date out=basicFormat.parse(finish);

            String newIn=formatCheck.format(in);
            Log.d("dataModificataIn",newIn);
            String newOut=formatCheck.format(out);
            Log.d("dataModificataOut",newOut);
            int before=Integer.parseInt(newIn);
            int then=Integer.parseInt(newOut);
            if(before<=then)
                return true;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sets the right date format
     * @param input date to change format
     * @return string date in format dd/mm/yyyy
     *
     * @author matteo.mascotto
     */
    public String dateToStandardFormat(Date input) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(input);

        int year = calendar.get(calendar.YEAR);
        int month = 1 + calendar.get(calendar.MONTH);
        int day = calendar.get(calendar.DATE);

        return day + "/" + month + "/" + year;
    }
}
