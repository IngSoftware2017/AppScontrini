package com.sw.ing.iteratorlibrary.iterator;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

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

    /**
     * Created by Federico Taschin on 03/12/2017.
     */

    @RunWith(AndroidJUnit4.class)
    public static class SampleIteratorTestUse {

        public final String XML_NAME = "DATASET v2.xml";
        /* Created by Federico Taschin
         * This class shows how to use the Iterator.
         */
        @Test
        public void useIterator() {
            try {
                Iterator<Bundle> iterator = new TicketsIterator.IteratorBuilder().setXML(XML_NAME).build(); //Build the iterator

                // use the iterator
                while(iterator.hasNext()){
                    Bundle bundle = iterator.next();
                    TicketInfo info = bundle.getTicketInfo();
                    Bitmap bitmap = bundle.getBitmap();
                }
            //exceptions generated if an error in the reading of the xml occurs
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}
