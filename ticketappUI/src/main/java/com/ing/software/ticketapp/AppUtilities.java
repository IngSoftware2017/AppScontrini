package com.ing.software.ticketapp;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        SimpleDateFormat basicFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat formatCheck = new SimpleDateFormat("yyyyMMdd", Locale.US);
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
          * Lazzarin
          * Method written to fix a bug of Android's DatePicker that save the date with month before the selected month.(When this bug will be eliminated, simply we'll
          * don't use this method)
          * @param date on format dd/MM/yyyy
          * @return date with +1 about Month, on format dd/MM/yyyy
          */
    public static String addMonth(String date)
    {        SimpleDateFormat basicFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                SimpleDateFormat formatCheck = new SimpleDateFormat("yyyyMMdd", Locale.US);
                String newDate="00000000";
        try
         {
                Log.d("dateOrigin",date);
                Date in=basicFormat.parse(date);
                newDate=formatCheck.format(in);
                Log.d("formatoData",newDate);
                int temp=Integer.parseInt(newDate);
                Log.d("meseAggiunto",temp+"");
                newDate=temp+"";
                in=formatCheck.parse(newDate);
                newDate=basicFormat.format(in);
                Log.d("formatoDataRestituito",newDate);

        }
        catch(ParseException e){
            Log.d("Error","Wrong date format");
        }
        return newDate;
    }


}
