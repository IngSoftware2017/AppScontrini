package com.unipd.ingsw.gruppo3;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class Missione {

    private String titolo;
    private Bitmap img;
    private String descrizione;
    private int ID;
    private Date startMission;
    private Date endMission;
    private String location;
    private boolean isRepay;
    private Uri excel;
    private String name;
    private int personID;


    public Missione(){
    }

    public Missione(String titolo, String desc) {
        this.titolo = titolo;
        this.descrizione = desc;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Bitmap getImg() { return img; }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Date getStartMission() {
        return startMission;
    }

    public void setStartMission(Date startMission) {
        this.startMission = startMission;
    }

    public Date getEndMission() {
        return endMission;
    }

    public void setEndMission(Date endMission) {
        this.endMission = endMission;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRepay() {
        return isRepay;
    }

    public void setRepay(boolean repay) {
        isRepay = repay;
    }

    public Uri getExcel() {
        return excel;
    }

    public void setExcel(Uri excel) {
        this.excel = excel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPersonID() {
        return personID;
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }
}