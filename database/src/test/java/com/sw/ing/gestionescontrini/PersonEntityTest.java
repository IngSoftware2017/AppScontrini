package com.sw.ing.gestionescontrini;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import database.PersonEntity;


/**
 * Created by Step on 28/11/2017.
 */

public class PersonEntityTest {
    PersonEntity personEntity1, personEntity2;

    @Before
    public void beforeTest() {
        personEntity1 = new PersonEntity("Name","LastName","student");
        personEntity2 = new PersonEntity();
    }

    //TODO test person ID with mock test

    @Test
    public void getNameReturnTheName(){
        assertTrue(personEntity1.getName().equals("Name"));
    }

    @Test
    public void setNameSetTheName(){
        personEntity2.setName("Name");
        assertTrue(personEntity2.getName().equals(personEntity1.getName()));
    }

    @Test
    public void getLastNameReturnTheLastName(){
        assertTrue(personEntity1.getLastName().equals("LastName"));
    }

    @Test
    public void setLastNameSetTheLastName(){
        personEntity2.setLastName("LastName");
        assertTrue(personEntity2.getLastName().equals(personEntity1.getLastName()));
    }
    @Test
    public void getAcademicTitleReturnTheAcademicTitle(){
        assertTrue(personEntity1.getAcademicTitle().equals("student"));
    }

    @Test
    public void setAcademicTitleSetTheAcademicTitle(){
        personEntity2.setAcademicTitle("student");
        assertTrue(personEntity2.getAcademicTitle().equals(personEntity1.getAcademicTitle()));
    }

}
