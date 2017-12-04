package com.sw.ing.gestionescontrini;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import database.Person;


/**
 * Created by Step on 28/11/2017.
 */

public class PersonTest {
    Person person1,person2;

    @Before
    public void beforeTest() {
        person1 = new Person("Name","LastName","student");
        person2 = new Person();
    }

    //TODO test person ID with mock test

    @Test
    public void getNameReturnTheName(){
        assertTrue(person1.getName().equals("Name"));
    }

    @Test
    public void setNameSetTheName(){
        person2.setName("Name");
        assertTrue(person2.getName().equals(person1.getName()));
    }

    @Test
    public void getLastNameReturnTheLastName(){
        assertTrue(person1.getLastName().equals("LastName"));
    }

    @Test
    public void setLastNameSetTheLastName(){
        person2.setLastName("LastName");
        assertTrue(person2.getLastName().equals(person1.getLastName()));
    }
    @Test
    public void getAcademicTitleReturnTheAcademicTitle(){
        assertTrue(person1.getAcademicTitle().equals("student"));
    }

    @Test
    public void setAcademicTitleSetTheAcademicTitle(){
        person2.setAcademicTitle("student");
        assertTrue(person2.getAcademicTitle().equals(person1.getAcademicTitle()));
    }

}
