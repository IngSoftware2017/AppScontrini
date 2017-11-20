package com.sw.ing.gestionescontrini;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import database.Ticket;
import database.TicketEntity;

import static org.junit.Assert.*;

/**
 * Created by Step on 20/11/2017.
 */

public class TicketEntityTest {
    Ticket ticket1;
    //Ticket ticket2;
    TicketEntity ticketE1;
    TicketEntity ticketE2;
    Date date;
    BigDecimal amount;

    @Before
    public void beforeTest(){
        amount = new BigDecimal(100);
        date = new Date(2017,11,20);
        ticketE1=new TicketEntity(null,amount,"Shop",date,"Title");
        ticket1=ticketE1;
        ticketE2=new TicketEntity(null,amount,"Shop",date,"Title");
    }

    @Test
    public void getIDReturnTheID(){
        assertTrue(ticketE1.getID()==0);
    }



/*    @Test
    public void IDisIncremental(){
        assertTrue(ticketE2.getID()==1);
    }
*/
}
