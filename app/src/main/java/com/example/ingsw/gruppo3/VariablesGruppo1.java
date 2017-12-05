package com.example.ingsw.gruppo3;

/**
 * Created by nicoladalmaso on 01/12/17.
 */

public class VariablesGruppo1 {
    private static VariablesGruppo1 mInstance = null;
    private String currentMissionDir;

    private VariablesGruppo1(){
        currentMissionDir = "";
    }

    public static VariablesGruppo1 getInstance(){
        if(mInstance == null)
        {
            mInstance = new VariablesGruppo1();
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
