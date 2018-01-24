package com.example.nicoladalmaso.gruppo1;

import android.util.Log;

/**
 * Created by Nicola on 24/01/2018.
 */

public class Singleton {
    private static Singleton mInstance = null;

    private int personID;
    private int missionID;
    private int ticketID;

    private Singleton(){
        personID = 0;
        missionID = 0;
        ticketID = 0;
    }

    public static synchronized Singleton getInstance(){
        if (mInstance == null) {
            mInstance = new Singleton();
            Log.d("Nuovo singleton", "OK");
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

    public void setMissionID(int value){
        missionID = value;
    }

    public void setPersonID(int value){
        personID = value;
    }

    public void setTicketID(int value){
        ticketID = value;
    }
}