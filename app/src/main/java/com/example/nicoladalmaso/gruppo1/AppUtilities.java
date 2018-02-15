package com.example.nicoladalmaso.gruppo1;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.example.nicoladalmaso.gruppo1.BillActivity.rotateImage;

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
            Date in = basicFormat.parse(start);
            Date out = basicFormat.parse(finish);

            String newIn = formatCheck.format(in);
            Log.d("dataModificataIn",newIn);
            String newOut = formatCheck.format(out);
            Log.d("dataModificataOut",newOut);
            int before = Integer.parseInt(newIn);
            int then = Integer.parseInt(newOut);
            if(before <= then)
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
    {        SimpleDateFormat basicFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat formatCheck = new SimpleDateFormat("yyyyMMdd");
                String newDate="00000000";
        try
         {
                Log.d("dateOrigin",date);
                Date in = basicFormat.parse(date);
                newDate = formatCheck.format(in);
                Log.d("formatoData",newDate);
                int temp = Integer.parseInt(newDate); // on this format, month is in the hundreds order
                Log.d("meseAggiunto",temp+"");
                newDate = temp+"";
                in = formatCheck.parse(newDate);
                newDate = basicFormat.format(in);
                Log.d("formatoDataRestituito",newDate);

        }
        catch(ParseException e){
            Log.d("Error","Wrong date format");
        }
        return newDate;
    }
        /**Lazzarin
        * check if insert date is between interval of mission
         *@param current date of start mission
         *@param finish date of end mission
         *@param  current date of ticket insert by user
        */
    public static  boolean checkIntervalDate(String start, String finish, String current)
    {
        if(checkDate(start,current) && checkDate(current,finish))
            return true;

     return false;
    }

    public static Bitmap fromByteArrayToBitmap (byte[] data){
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }

    /** DAL MASO
     * Check the image rotation and correct it
     * @param img image bitmap
     * @param path image path
     * @return correct bitmap
     */
    public static Bitmap checkImageOrientation(Bitmap img, String path){
        Bitmap rotatedBitmap = img;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(img, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(img, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(img, 270);
                    break;

                default:
                    rotatedBitmap = img;
            }
        } catch (IOException e){}
        return rotatedBitmap;
    }

    /** Dal Maso
     * Circular new activity reveal animation
     * @param v view to expand
     */
    public static void circularReveal(View v){
        // get the center for the clipping circle
        int cx = v.getWidth();
        int cy = v.getHeight();

        // get the final radius for the clipping circle
        float finalRadius = (float) Math.hypot(cx, cy);

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        v.setVisibility(View.VISIBLE);
        anim.start();
    }

    /** Dal Maso
     * Remove list item collapse animation
     * @param v view to animate
     * @param al animation manager
     */
    public static void collapse(final View v, Animation.AnimationListener al) {
        final int initialHeight = v.getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                }
                else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        if (al!=null) {
            anim.setAnimationListener(al);
        }
        anim.setDuration(500);
        v.startAnimation(anim);
    }
}



