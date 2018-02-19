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

    private int personID; //current person id
    private int missionID; //current mission id
    private int ticketID; //current ticket id
    private byte[] pictureTaken; //it saves the system from another picture save (-2 sec in photo taking process)
    private Date startDate;
    private Date endDate;
    private String currency;
    private int flag;
    private boolean flagStart;
    private Singleton(){
        personID = 0;
        missionID = 0;
        ticketID = 0;
        pictureTaken = null;
        startDate = null;
        flag = 0;
        currency = "";
    }

    public static synchronized Singleton getInstance(){
        if (mInstance == null) {
            mInstance = new Singleton();
        }
        return mInstance;
    }

    public int getMissionID(){
        return missionID;
    }

    public int getPersonID(){
        return personID;
    }

    public int getTicketID(){
        return ticketID;
    }



    public byte[] getTakenPicture(){
        return pictureTaken;
    }

    public void setMissionID(int value){
        missionID = value;
    }

    public void setPersonID(int value){
        personID = value;
    }

    public void setTicketID(int value){
        ticketID = value;
    }

    public void setTakenPicure(byte[] value){
        pictureTaken = value;
    }


    //lazzarin
    public Date getStartDate(){return startDate;}

    public void setStartDate(Date start){startDate = start;}

    public void setStartFlag(int value){flag = value;}

    public int getStartFlag(){return flag;}

    public Date getEndDate(){return endDate;}

    public void setEndDate(Date end){endDate = end;}

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