package com.sw.ing.gestionescontrini;
import org.junit.Before;
import org.junit.Test;

import database.DatabaseManager;
import database.Ticket;

import static org.junit.Assert.*;
/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DatabaseManagerTest {

    DatabaseManager dbm;
    @Before
    public void beforeTest(){
        dbm = new DatabaseManager();
        DatabaseManager dbm = new DatabaseManager();
    }

    @Test
    public void TicketCreationShouldReturnAFailure(){
        assertTrue(dbm.addTicket(new Ticket())==-1);
    }

    @Test
    public void TicketUpdateShouldReturnFalse(){
        assertFalse(dbm.updateTicket(new Ticket()));
    }

    @Test
    public void getAllTicketsShouldReturnEmptyList(){
        assertTrue(dbm.getAllTickets().size()==0);
    }
}
