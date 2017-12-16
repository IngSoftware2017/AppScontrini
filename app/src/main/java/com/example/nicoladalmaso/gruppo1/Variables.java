package com.example.nicoladalmaso.gruppo1;

import android.app.Application;

/**
 * Created by nicoladalmaso on 01/12/17.
 */

public class Variables{
    private static Variables mInstance = null;
    private String currentMissionDir;

    private Variables(){
        currentMissionDir = "";
    }

    public static Variables getInstance(){
        if(mInstance == null)
        {
            mInstance = new Variables();
        }
        return mInstance;
    }

    public String getCurrentMissionDir() {
        return this.currentMissionDir;
    }

    public void setCurrentMissionDir(String str) {
        currentMissionDir = str;
    }
}
