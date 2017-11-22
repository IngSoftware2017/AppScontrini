package com.ing.software.ticketapp.OCR;

import com.ing.software.ticketapp.OCR.OcrUtils;

import org.junit.Test;

import java.lang.reflect.Method;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by matteosalvagno on 17/11/17.
 */

public class DistanceStringUnitTest {

    @Test
    public void SubstringDistanceTest1() throws Exception {

        String a = "T1st";
        String b = "Test";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(1, r);
    }


    @Test
    public void SubstringDistanceTest2() throws Exception {

        String a = "";
        String b = "";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(0, r);
    }

    @Test
    public void SubstringDistanceTest3() throws Exception {

        String a = "Test";
        String b = "";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(4, r);
    }

    @Test
    public void SubstringDistanceTest4() throws Exception {

        String a = "Test";
        String b = "Tes";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(1, r);
    }

    @Test
    public void SubstringDistanceTest5() throws Exception {

        String a = "Test";
        String b = "Tess";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(1, r);
    }

    @Test
    public void SubstringDistanceTest6() throws Exception {

        String a = null;
        String b = "Test";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(-1, r);
    }


    @Test
    public void maxLenghtStringsTest1() throws Exception {

        Integer a = 6;
        Integer b = 2;

        Method method = OcrUtils.class.getDeclaredMethod("maxLenghtStrings", int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(6, r);

    }


    @Test
    public void maxLenghtStringsTest2() throws Exception {

        Integer a = 0;
        Integer b = 2;

        Method method = OcrUtils.class.getDeclaredMethod("maxLenghtStrings", int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(2, r);
    }

    @Test
    public void minLenght3StringsTest1() throws Exception {

        Integer a = 1;
        Integer b = 2;
        Integer c = 3;

        Method method = OcrUtils.class.getDeclaredMethod("minLenghtStrings", int.class, int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,c);

        assertEquals(1, r);

    }
    @Test
    public void minLenght3StringsTest2() throws Exception {

        Integer a = 1;
        Integer b = 0;
        Integer c = 1000;

        Method method = OcrUtils.class.getDeclaredMethod("minLenghtStrings", int.class, int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,c);

        assertEquals(0, r);
    }



    @Test
    public void haveSubstring1() throws Exception {

        String text = "There is a word test in this text";
        String substring = "Test";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(0, r);

    }

    @Test
    public void haveSubstring2() throws Exception {

        String text = "In this text";
        String substring = "Test";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(1, r);

    }

    @Test
    public void haveSubstring3() throws Exception {

        String text = "";
        String substring = "Test";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(-1, r);

    }


    @Test
    public void FindDateTest1() throws Exception {

        String a = "il questo testo la 23-06-1995";
        Method method = OcrUtils.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(6, r);
        //assertTrue((r >= 6) && (r <= 8));
    }
    


}