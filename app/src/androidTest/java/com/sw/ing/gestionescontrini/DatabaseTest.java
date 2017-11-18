package com.sw.ing.gestionescontrini;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;

import database.DAO;
import database.Database;
import database.TicketEntity;

/**
 * Created by Federico Taschin on 12/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

     static Database database;
     static DAO ticketDAO;
     static TicketEntity testTicket1, testTicket2;

    @Before
    public static void setDatabase() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, Database.class).build();

        Method method = Database.class.getDeclaredMethod("ticketDao");
        method.setAccessible(true);
        ticketDAO = (DAO) method.invoke(database, null);
    }

    @Before
    public void setTicket(){
        testTicket1 = new TicketEntity();
        testTicket1.setAmount(new BigDecimal(12));
        testTicket1.setDate(new Date(1996,10,12));
        testTicket1.setShop("Decathlon");
        testTicket1.setTitle("Football shoes");

        testTicket2 = new TicketEntity();
        testTicket2.setAmount(new BigDecimal(12));
        testTicket2.setDate(new Date(1966,11,12));
        testTicket2.setShop("wwww");
        testTicket2.setTitle("wwwwwwwww shoes");

        ticketDAO.addTicket(testTicket1);
        ticketDAO.addTicket(testTicket2);
    }

    @Test
    public void insertTicketDateTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getDate().equals(testTicket1.getDate()));
    }
    @Test
    public void insertTicketTitleTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getTitle().equals(testTicket1.getTitle()));
    }
    @Test
    public void insertTicketAmountTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getAmount().equals(testTicket1.getAmount()));
    }
    @Test
    public void insertTicketShopTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getShop().equals(testTicket1.getShop()));
    }
    @Test
    public void updateTicketTest(){

    }


    @Test
    public void deleteTicketTest(){
        /*
        TicketEntity testTicketDelete = new TicketEntity();
        int id = testTicketDelete.getID();
        ticketDAO.addTicket(testTicketDelete);
        assert (id == ticketDAO.deleteTicket(testTicketDelete.getID()));
        */
        //TODO test with invalid ID, test with null ID (deleteTicket pre-conditions accept null)
    }

}
