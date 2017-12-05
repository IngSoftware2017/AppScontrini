package com.example.ingsw.gruppo3;

import android.graphics.Bitmap;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

//CLASSE SCONTRINO CHE VIENE UTILIZZATA PER LA RAPPRESENTAZIONE GRAFICA SULLE CARDVIEW
//Accetta titolo, descrizione e immagine che verranno prelevate dal db
//Dal Maso
public class ScontrinoGruppo1 {

    private String titolo;
    private String descrizione;
    private Bitmap img;

    public ScontrinoGruppo1(){
    }

    public ScontrinoGruppo1(String titolo, String descrizione, Bitmap img) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.img = img;
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