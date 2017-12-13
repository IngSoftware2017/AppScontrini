package com.sw.ing.gestionescontrini;


import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import database.Ticket;

import static org.junit.Assert.*;

/**
 * Created by Step on 20/11/2017.
 */

public class TicketTest {
    Ticket ticket1;
    Ticket ticket2;
    Date date;
    BigDecimal amount;

    @Before
    public void beforeTest(){
        amount = new BigDecimal(100);
        date = new Date(2017,11,20);
        ticket1=new Ticket(null,amount,"Shop",date,"Title",1);

        ticket2=new Ticket();
    }

    @Test
    public void getDateReturnTheDate(){
        assertTrue(ticket1.getDate().equals(date));
    }

    @Test
    public void setDateSetTheDate(){
        ticket2.setDate(date);
        assertTrue(ticket2.getDate().equals(ticket1.getDate()));
    }

    @Test
    public void getAmountReturnTheAmount(){
        assertTrue(ticket1.getAmount().equals(amount));
    }

    @Test
    public void setAmountSetTheAmount(){
        ticket2.setAmount(amount);
        assertTrue(ticket2.getAmount().equals(ticket1.getAmount()));
    }

    @Test
    public void getShopReturnTheShop(){
        assertTrue(ticket1.getShop().equals("Shop"));
    }

    @Test
    public void setShopSetTheShop(){
        ticket2.setShop("Shop");
        assertTrue(ticket2.getShop().equals(ticket1.getShop()));
    }

    @Test
    public void getTitleReturnTheTitle(){
        assertTrue(ticket1.getTitle().equals("Title"));
    }

    @Test
    public void setTitleSetTheTitle(){
        ticket2.setTitle("Title");
        assertTrue(ticket2.getTitle().equals(ticket1.getTitle()));
    }

    @Test
    public void getMissionIDReturnTheMissionID(){
        assertTrue(ticket1.getMissionID()==1);
    }

    @Test
    public void setMissionIDSetTheMissionID(){
        ticket2.setMissionID(2);
        assertTrue(ticket2.getMissionID()==2);
    }

    @Test
    public void toStringReturnTheExactStringForm(){
        assertTrue(ticket1.toString().equals( ticket1.getShop()+"\nTotale: "+ticket1.getAmount()));
    }
//TODO getUri, setUri test
// TODO Needed a mock test to test ID access method (even MissionID needs mock test)
}