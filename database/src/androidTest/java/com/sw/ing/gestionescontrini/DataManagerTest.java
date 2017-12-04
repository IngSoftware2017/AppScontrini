package com.sw.ing.gestionescontrini;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import database.DataManager;
import database.Ticket;

import static org.junit.Assert.*;
/**
 * Created by Federico Taschin on 07/11/2017.
 */

public class DataManagerTest {

    static Context context;
    DataManager dbm;
    @Before
    public void beforeTest(){
        context = InstrumentationRegistry.getTargetContext();
        dbm = new DataManager(context);
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
