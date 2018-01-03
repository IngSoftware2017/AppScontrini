package com.example.nicoladalmaso.gruppo1;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Cristian on 02/01/2018.
 */

public class AddNewMissionTest {
    AppUtilities test=new  AppUtilities();
    @Test
    public void checkDateTest()
    {
        int year=2011;
        int month=01;
        int day=01;
             while(year<2050)
            {
                while(month<12)
                {
                    while(day<31)
                    {
                        String start=day+"/"+month+"/"+year;
                        day++;
                        String finish=day+"/"+month+"/"+year;
                        assertEquals(true,test.checkDate(start,finish));
                    }
                    day=01;
                    month++;

                }
                day=01;
                month=01;
                year++;

            }

        while(year<2050)
        {
            while(month<12)
            {
                while(day<31)
                {
                    String finish=day+"/"+month+"/"+year;
                    day+=3;
                    String start=day+"/"+month+"/"+year;
                    assertEquals(false,test.checkDate(start,finish));
                }
                day=01;
                month++;

            }
            day=01;
            month=01;
            year++;

        }







    }

}