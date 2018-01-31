package com.example.nicoladalmaso.gruppo1;

import android.util.Log;

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

    private Singleton(){
        personID = 0;
        missionID = 0;
        ticketID = 0;
        pictureTaken = null;
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
}