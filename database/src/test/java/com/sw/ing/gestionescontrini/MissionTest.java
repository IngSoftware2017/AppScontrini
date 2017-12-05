package com.sw.ing.gestionescontrini;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import database.Mission;

/**
 * Created by Step on 28/11/2017.
 */

public class MissionTest {
    Mission mission1;
    Mission mission2;
    Date dateI,dateF;

    @Before
    public void beforeTest(){
        dateI = new Date(2017,11,20);
        dateF = new Date(2017,11,30);
        mission1 = new Mission(dateI,dateF,"Venice",1);
        mission2 = new Mission();
    }

    @Test
    public void getStartMissionReturnTheDateOfBeginning(){
        assertTrue(mission1.getStartMission().equals(dateI));
    }

    @Test
    public void setStartMissionSetTheDateOfBeginning(){
        mission2.setStartMission(dateI);
        assertTrue(mission2.getStartMission().equals(mission1.getStartMission()));
    }
    @Test
    public void getEndMissionReturnTheDateOfEnding(){
        assertTrue(mission1.getEndMission().equals(dateF));
    }

    @Test
    public void setEndMissionSetTheDateOfEnding(){
        mission2.setEndMission(dateF);
        assertTrue(mission2.getEndMission().equals(mission1.getEndMission()));
    }

    @Test
    public void getLocationSetTheLocation(){
        assertTrue(mission1.getLocation().equals("Venice"));
    }

    @Test
    public void setLocationSetTheLocation(){
        mission2.setLocation("Venice");
        assertTrue(mission2.getLocation().equals(mission1.getLocation()));
    }

    @Test
    public void isRepayInitializedFalse(){
        assertTrue(!mission1.isRepay());
    }

    @Test
    public void setRepaySetIsRepayCondition(){
        mission2.setRepay(true);
        assertTrue(mission2.isRepay());
    }

    //TODO test excel URI with mock test

    @Test
    public void getPersonIDReturnThePersonID(){
        assertTrue(mission1.getPersonID()==1);
    }

    @Test
    public void setPersonIDReturnThePersonID(){
        mission2.setPersonID(1);
        assertTrue(mission2.getPersonID()==mission1.getPersonID());
    }

    //TODO test personID method with mock
}
