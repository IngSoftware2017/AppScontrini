package com.sw.ing.gestionescontrini;
import org.junit.Before;
import org.junit.Test;

import database.DataManager;
import database.Ticket;

import static org.junit.Assert.*;
/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DataManagerTest {

    DataManager dbm;
    @Before
    public void beforeTest(){
        dbm = new DataManager();
        DataManager dbm = new DataManager();
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
