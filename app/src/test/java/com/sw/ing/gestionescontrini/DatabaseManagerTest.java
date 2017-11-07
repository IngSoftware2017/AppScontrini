package com.sw.ing.gestionescontrini;
import org.junit.Test;

import database.DatabaseManager;
import database.Ticket;

import static org.junit.Assert.*;
/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DatabaseManagerTest {

    @Test
    public void TicketCreationShouldReturnAFailure(){
        DatabaseManager dbm = new DatabaseManager();
        assertTrue(dbm.addTicket(new Ticket())==-1);
    }

    @Test
    public void TicketUpdateShouldReturnFalse(){
        DatabaseManager dbm = new DatabaseManager();
        assertFalse(dbm.updateTicket(new Ticket()));
    }

    @Test
    public void getAllTicketsShouldReturnEmptyList(){
        DatabaseManager dbm = new DatabaseManager();
        assertTrue(dbm.getAllTickets().size()==0);
    }
}
