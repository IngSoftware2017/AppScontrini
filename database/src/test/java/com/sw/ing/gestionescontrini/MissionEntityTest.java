package com.sw.ing.gestionescontrini;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import database.MissionEntity;

/**
 * Created by Stefano Elardo on 28/11/2017.
 */

public class MissionEntityTest {
    MissionEntity missionEntity1;
    MissionEntity missionEntity2;
    Date dateI,dateF;

    @Before
    public void beforeTest(){
        dateI = new Date(2017,11,20);
        dateF = new Date(2017,11,30);
        String name="mission1";
        String location="Venice";
        long personID=1;
        missionEntity1 = new MissionEntity(name,dateI,dateF,location,personID);
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
    public void isClosedInitializedFalse(){
        assertTrue(!missionEntity1.isClosed());
    }

    @Test
    public void setClosedSetIsClosedCondition(){
        missionEntity2.setClosed(true);
        assertTrue(missionEntity2.isClosed());
    }


    @Test
    public void getPersonIDReturnThePersonID(){
        assertTrue(missionEntity1.getPersonID()==1);
    }

    @Test
    public void setPersonIDReturnThePersonID(){
        missionEntity2.setPersonID(1);
        assertTrue(missionEntity2.getPersonID()== missionEntity1.getPersonID());
    }

    @Test
    public void getNameReturnTheMissionName(){assertTrue(missionEntity1.getName().equals("mission1"));}

    @Test
    public void setNameSetTheMissionName(){
        missionEntity2.setName("mission1");
        assertTrue(missionEntity2.getName().equals(missionEntity1.getName()));
    }

    //Uri fields are not tested
}
