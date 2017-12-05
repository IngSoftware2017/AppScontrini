package com.sw.ing.gestionescontrini;


import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import database.TicketEntity;

import static org.junit.Assert.*;

/**
 * Created by Step on 20/11/2017.
 */

public class TicketEntityTest {
    TicketEntity ticketEntity1;
    TicketEntity ticketEntity2;
    Date date;
    BigDecimal amount;

    @Before
    public void beforeTest(){
        amount = new BigDecimal(100);
        date = new Date(2017,11,20);
        ticketEntity1 =new TicketEntity(null,amount,"Shop",date,"Title",1);

        ticketEntity2 =new TicketEntity();
    }

    @Test
    public void getDateReturnTheDate(){
        assertTrue(ticketEntity1.getDate().equals(date));
    }

    @Test
    public void setDateSetTheDate(){
        ticketEntity2.setDate(date);
        assertTrue(ticketEntity2.getDate().equals(ticketEntity1.getDate()));
    }

    @Test
    public void getAmountReturnTheAmount(){
        assertTrue(ticketEntity1.getAmount().equals(amount));
    }

    @Test
    public void setAmountSetTheAmount(){
        ticketEntity2.setAmount(amount);
        assertTrue(ticketEntity2.getAmount().equals(ticketEntity1.getAmount()));
    }

    @Test
    public void getShopReturnTheShop(){
        assertTrue(ticketEntity1.getShop().equals("Shop"));
    }

    @Test
    public void setShopSetTheShop(){
        ticketEntity2.setShop("Shop");
        assertTrue(ticketEntity2.getShop().equals(ticketEntity1.getShop()));
    }

    @Test
    public void getTitleReturnTheTitle(){
        assertTrue(ticketEntity1.getTitle().equals("Title"));
    }

    @Test
    public void setTitleSetTheTitle(){
        ticketEntity2.setTitle("Title");
        assertTrue(ticketEntity2.getTitle().equals(ticketEntity1.getTitle()));
    }

    @Test
    public void getMissionIDReturnTheMissionID(){
        assertTrue(ticketEntity1.getMissionID()==1);
    }

    @Test
    public void setMissionIDSetTheMissionID(){
        ticketEntity2.setMissionID(2);
        assertTrue(ticketEntity2.getMissionID()==2);
    }

    @Test
    public void toStringReturnTheExactStringForm(){
        assertTrue(ticketEntity1.toString().equals( ticketEntity1.getShop()+"\nTotale: "+ ticketEntity1.getAmount()));
    }
//TODO getUri, setUri test
// TODO Needed a mock test to test ID access method (even MissionID needs mock test)
}