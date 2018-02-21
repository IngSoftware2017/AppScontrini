package com.example.nicoladalmaso.gruppo1;

import android.util.Log;

import java.util.Currency;
import java.util.Date;

import database.DataManager;

/**
 * Created by Nicola on 24/01/2018.
 */

public class Singleton {
    private static Singleton mInstance = null;


    private byte[] pictureTaken; //it saves the system from another picture save (-2 sec in photo taking process)
    private String currency;
    private int flag;
    private boolean flagStart;
    private Singleton(){
        pictureTaken = null;
        flag = 0;
        currency = "";
    }

    public static synchronized Singleton getInstance(){
        if (mInstance == null) {
            mInstance = new Singleton();
            Log.d("Nuovo singleton", "OK");
        }
        return mInstance;
    }


    public byte[] getTakenPicture(){
        return pictureTaken;
    }

    public void setTakenPicure(byte[] value){
        pictureTaken = value;
    }


    //lazzarin

    public void setStartFlag(int value){flag = value;}

    public int getStartFlag(){return flag;}

    public void setCurrency(String curr){
        switch (curr){
            case ("EUR"):
                currency = "€";
                break;
            case ("USD"):
                currency = "$";
                break;
            case ("GBP"):
                currency = "£";
                break;
            default:
                currency = "€";
                break;
        }
    }

    public String getCurrency() {return currency;}

}