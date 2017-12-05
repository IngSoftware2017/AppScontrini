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
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * Created by Federico Taschin on 12/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

     static Database database;
     static DAO ticketDAO;
     static TicketEntity testTicketEntity1;
    // static TicketEntity testTicket2;
     static MissionEntity missionEntity;
     static PersonEntity personEntity;

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
        personEntity = new PersonEntity();
        personEntity.setName("Albert");

        personEntity.setID((int) ticketDAO.addPerson(personEntity));

        missionEntity = new MissionEntity();
        missionEntity.setPersonID(personEntity.getID());
        missionEntity.setID((int) ticketDAO.addMission(missionEntity));

        testTicketEntity1 = new TicketEntity();
        testTicketEntity1.setAmount(new BigDecimal(12));
        testTicketEntity1.setDate(new Date(1996,10,12));
        testTicketEntity1.setShop("Decathlon");
        testTicketEntity1.setTitle("Football shoes");
        testTicketEntity1.setMissionID(missionEntity.getID());

        /*testTicket2 = new TicketEntity();
        testTicket2.setAmount(new BigDecimal(12));
        testTicket2.setDate(new Date(1966,11,12));
        testTicket2.setShop("wwww");
        testTicket2.setTitle("wwwwwwwww shoes");
        testTicket2.setMissionID(missionEntity.getID());*/

        testTicketEntity1.setID((int) ticketDAO.addTicket(testTicketEntity1));
        //ticketDAO.addTicket(testTicket2);
    }

    @Test
    public void insertTicketDateTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getDate().equals(testTicketEntity1.getDate()));
    }
    @Test
    public void insertTicketTitleTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getTitle().equals(testTicketEntity1.getTitle()));
    }
    @Test
    public void insertTicketAmountTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getAmount().equals(testTicketEntity1.getAmount()));
    }
    @Test
    public void insertTicketShopTest(){
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getShop().equals(testTicketEntity1.getShop()));
    }
    @Test
    public void updateNotEmptyTicketTest(){
        testTicketEntity1.setAmount(new BigDecimal(12));
        ticketDAO.updateTicket(testTicketEntity1);
        Assert.assertTrue(ticketDAO.getAllTickets().get(0).getAmount().equals(testTicketEntity1.getAmount()));
    }

    @Test
    public void updateTicketShouldNullEmptyFields(){
        TicketEntity emptyTicketEntity = new TicketEntity();
        emptyTicketEntity.setID(testTicketEntity1.getID());
        emptyTicketEntity.setMissionID(testTicketEntity1.getMissionID());
        emptyTicketEntity.setTitle("modified value");
        ticketDAO.updateTicket(emptyTicketEntity);
        Assert.assertNull(emptyTicketEntity.getAmount());
    }

    @Test
    public void updateNullTicketIDShouldReturnZero(){
        TicketEntity ticketEntity = new TicketEntity();
        Assert.assertTrue(0 == ticketDAO.updateTicket(ticketEntity));
    }

    @Test
    public void updateInvalidTicketIDShouldReturnZero(){
        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setID(123);
        Assert.assertTrue(0 == ticketDAO.updateTicket(ticketEntity));
    }

    @Test
    public void deleteExistingTicketTest(){
        Assert.assertTrue(ticketDAO.deleteTicket(testTicketEntity1.getID())==1);
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
