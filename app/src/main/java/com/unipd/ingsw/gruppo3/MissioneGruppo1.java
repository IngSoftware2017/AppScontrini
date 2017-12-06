package com.unipd.ingsw.gruppo3;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class MissioneGruppo1 {
    private int id;
    private String titolo;
    private Bitmap img;
    private String descrizione;
    private Date dataInizio, dataFine;
    private int personID;

    public MissioneGruppo1(){
    }

    public MissioneGruppo1(String titolo, String desc) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(Date dataInizio) {
        this.dataInizio = dataInizio;
    }

    public Date getDataFine() {
        return dataFine;
    }

    public void setDataFine(Date dataFine) {
        this.dataFine = dataFine;
    }

    public int getPersonID() {
        return personID;
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }
}