package com.sw.ing.gestionescontrini;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import database.MissionEntity;

/**
 * Created by Step on 28/11/2017.
 */

public class MissionEntityTest {
    MissionEntity missionEntity1;
    MissionEntity missionEntity2;
    Date dateI,dateF;

    @Before
    public void beforeTest(){
        dateI = new Date(2017,11,20);
        dateF = new Date(2017,11,30);
        missionEntity1 = new MissionEntity("MissionName",dateI,dateF,"Venice",1);
        missionEntity2 = new MissionEntity();
    }

    @Test
    public void getStartMissionReturnTheDateOfBeginning(){
        assertTrue(missionEntity1.getStartDate().equals(dateI));
    }

    @Test
    public void setStartMissionSetTheDateOfBeginning(){
        missionEntity2.setStartDate(dateI);
        assertTrue(missionEntity2.getStartDate().equals(missionEntity1.getStartDate()));
    }
    @Test
    public void getEndMissionReturnTheDateOfEnding(){
        assertTrue(missionEntity1.getEndDate().equals(dateF));
    }

    @Test
    public void setEndMissionSetTheDateOfEnding(){
        missionEntity2.setEndDate(dateF);
        assertTrue(missionEntity2.getEndDate().equals(missionEntity1.getEndDate()));
    }

    @Test
    public void getLocationSetTheLocation(){
        assertTrue(missionEntity1.getLocation().equals("Venice"));
    }

    @Test
    public void setLocationSetTheLocation(){
        missionEntity2.setLocation("Venice");
        assertTrue(missionEntity2.getLocation().equals(missionEntity1.getLocation()));
    }

    @Test
    public void isRepayInitializedFalse(){
        assertTrue(!missionEntity1.isClosed());
    }

    @Test
    public void setRepaySetIsRepayCondition(){
        missionEntity2.setClosed(true);
        assertTrue(missionEntity2.isClosed());
    }

    //TODO test excel URI with mock test

    @Test
    public void getPersonIDReturnThePersonID(){
        assertTrue(missionEntity1.getPersonID()==1);
    }

    @Test
    public void setPersonIDReturnThePersonID(){
        missionEntity2.setPersonID(1);
        assertTrue(missionEntity2.getPersonID()== missionEntity1.getPersonID());
    }

    //TODO test personID method with mock
}
