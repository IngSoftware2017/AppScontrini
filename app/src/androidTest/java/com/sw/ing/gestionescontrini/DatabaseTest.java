package com.sw.ing.gestionescontrini;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
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

     Database database;
     DAO ticketDAO;
     TicketEntity testTicket;
     TicketEntity testTicketDelete;

    @Before
    public  void setDatabase() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, Database.class).build();

        Method method = Database.class.getDeclaredMethod("ticketDao");
        method.setAccessible(true);
        ticketDAO = (DAO) method.invoke(database, null);

        testTicket = new TicketEntity();
        testTicket.setAmount(new BigDecimal(12));
        testTicket.setDate(new Date(1996,10,12));
        testTicket.setShop("Decathlon");
        testTicket.setTitle("Football shoes");
        ticketDAO.addTicket(testTicket);
        testTicketDelete = new TicketEntity();
        ticketDAO.addTicket(testTicketDelete);
    }


    @Test
    public void insertTicketDateTest(){
        assert (ticketDAO.getAllTickets().get(0).getDate().equals(testTicket.getDate()));
    }

    @Test
    public void updateTicketTest(){

    }


    @Test
    public void deleteTicketTest(){

        int id = testTicketDelete.getID();
        assert (id == ticketDAO.deleteTicket(testTicketDelete.getID()));

        //TODO test with invalid ID, test with null ID (deleteTicket pre-conditions accept null)
    }

}
