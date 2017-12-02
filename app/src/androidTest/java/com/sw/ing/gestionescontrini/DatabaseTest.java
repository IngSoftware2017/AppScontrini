package com.sw.ing.gestionescontrini;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;

import database.DAO;
import database.Database;
import database.Mission;
import database.Person;
import database.Ticket;

/**
 * Created by Federico Taschin on 12/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

     static Database database;
     static DAO ticketDAO;
     static Ticket testTicket1;
    // static Ticket testTicket2;
     static Mission mission;
     static Person person;

    @Before
    public void setDatabaseAndCreateTickets() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Context context = InstrumentationRegistry.getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, Database.class).build();

        Method method = Database.class.getDeclaredMethod("ticketDao");
        method.setAccessible(true);
        ticketDAO = (DAO) method.invoke(database, null);
        createTicket1AndInsert();
    }


    public void createTicket1AndInsert(){
        person = new Person();
        person.setName("Albert");

        person.setID((int) ticketDAO.addPerson(person));

        mission = new Mission();
        mission.setPersonID(person.getID());
        mission.setID((int) ticketDAO.addMission(mission));

        testTicket1 = new Ticket();
        testTicket1.setAmount(new BigDecimal(12));
        testTicket1.setDate(new Date(1996,10,12));
        testTicket1.setShop("Decathlon");
        testTicket1.setTitle("Football shoes");
        testTicket1.setMissionID(mission.getID());

        /*testTicket2 = new Ticket();
        testTicket2.setAmount(new BigDecimal(12));
        testTicket2.setDate(new Date(1966,11,12));
        testTicket2.setShop("wwww");
        testTicket2.setTitle("wwwwwwwww shoes");
        testTicket2.setMissionID(mission.getID());*/

        testTicket1.setID((int) ticketDAO.addTicket(testTicket1));
        //ticketDAO.addTicket(testTicket2);
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
    public void updateNotEmptyTicketTest(){
        testTicket1.setAmount(new BigDecimal(12));
        ticketDAO.updateTicket(testTicket1);
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getAmount().equals(testTicket1.getAmount()));
    }

    @Test
    public void updateTicketShouldNullEmptyFields(){
        Ticket emptyTicket = new Ticket();
        emptyTicket.setID(testTicket1.getID());
        emptyTicket.setMissionID(testTicket1.getMissionID());
        emptyTicket.setTitle("modified value");
        ticketDAO.updateTicket(emptyTicket);
        Assert.assertNull(emptyTicket.getAmount());
    }

    @Test
    public void updateNullTicketIDShouldReturnZero(){
        Ticket ticket = new Ticket();
        Assert.assertTrue(0 == ticketDAO.updateTicket(ticket));
    }

    @Test
    public void updateInvalidTicketIDShouldReturnZero(){
        Ticket ticket = new Ticket();
        ticket.setID(123);
        Assert.assertTrue(0 == ticketDAO.updateTicket(ticket));
    }

    @Test
    public void deleteExistingTicketTest(){
        Assert.assertTrue(ticketDAO.deleteTicket(testTicket1.getID())==1);
    }
    //TODO test with invalid ID, test with null ID (deleteTicket pre-conditions accept null)


    @Test
    public void insertMissionTest(){}

    @Test
    public void updateMissionTest(){}

    @Test
    public void deleteMissionTest(){}

    @Test
    public void insertPersonTest(){}

    @Test
    public void updatePersonTest(){}

    @Test
    public void deletePersonTest(){}

}
