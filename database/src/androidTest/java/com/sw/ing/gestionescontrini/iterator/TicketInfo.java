package com.sw.ing.gestionescontrini.iterator;

import android.arch.persistence.room.ColumnInfo;
import android.net.Uri;

import java.math.BigDecimal;
import java.util.Date;

import database.Constants;

/**
 * Created by Federico Taschin on 02/12/2017.
 */

public class TicketInfo {
    private int ID;
    private BigDecimal amount;
    private String shop;
    private Date date;
    private String features;
    private int[] positionAmount;
    private int[] positionDate;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int[] getPositionAmount() {
        return positionAmount;
    }

    public void setPositionAmount(int[] positionAmount) {
        this.positionAmount = positionAmount;
    }

    public int[] getPositionDate() {
        return positionDate;
    }

    public void setPositionDate(int[] positionDate) {
        this.positionDate = positionDate;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }
}
