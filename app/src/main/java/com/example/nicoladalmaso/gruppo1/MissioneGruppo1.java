package com.example.nicoladalmaso.gruppo1;

import android.graphics.Bitmap;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class MissioneGruppo1 {

    private String titolo;
    private Bitmap img;
    private String descrizione;

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

}