package com.sw.ing.gestionescontrini;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;

import database.DataManager;
import database.TicketEntity;

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
        assertTrue(dbm.addTicket(new TicketEntity())==-1);
    }

    @Test
    public void TicketUpdateShouldReturnFalse(){
        assertFalse(dbm.updateTicket(new TicketEntity()));
    }

    @Test
    public void getAllTicketsShouldReturnEmptyList(){
        assertTrue(dbm.getAllTickets().size()==0);
    }
}