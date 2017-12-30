package com.ing.software.ocr;

import com.ing.software.ocr.OcrUtils;

import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by matteosalvagno on 17/11/17.
 * @author Salvagno
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
    public void SubstringDistanceTest7() throws Exception {

        String a = "i";
        String b = "Totale";

        Method method = OcrUtils.class.getDeclaredMethod("levDistance", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(6, r);
    }

    @Test
    public void SubstringDistanceMaxTest1() throws Exception {

        String a = "i";
        String b = "Totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("levDistanceWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,max);

        assertEquals(3, r);
    }

    @Test
    public void SubstringDistanceMaxTest2() throws Exception {

        String a = "Tot";
        String b = "Totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("levDistanceWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,max);

        assertEquals(3, r);
    }

    @Test
    public void SubstringDistanceMaxTest3() throws Exception {

        String a = "Tota";
        String b = "Totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("levDistanceWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,max);

        assertEquals(2, r);
    }

    @Test
    public void SubstringDistanceMaxTest4() throws Exception {

        String a = "uu";
        String b = "Totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("levDistanceWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,max);

        assertEquals(3, r);
    }

    @Test
    public void maxLengthStringsTest1() throws Exception {

        Integer a = 6;
        Integer b = 2;

        Method method = OcrUtils.class.getDeclaredMethod("maxLengthStrings", int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(6, r);

    }


    @Test
    public void maxLengthStringsTest2() throws Exception {

        Integer a = 0;
        Integer b = 2;

        Method method = OcrUtils.class.getDeclaredMethod("maxLengthStrings", int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b);

        assertEquals(2, r);
    }

    @Test
    public void minLength3StringsTest1() throws Exception {

        Integer a = 1;
        Integer b = 2;
        Integer c = 3;

        Method method = OcrUtils.class.getDeclaredMethod("minLengthStrings", int.class, int.class, int.class );
        method.setAccessible(true);
        int r = (int)method.invoke(null,a,b,c);

        assertEquals(1, r);

    }
    @Test
    public void minLength3StringsTest2() throws Exception {

        Integer a = 1;
        Integer b = 0;
        Integer c = 1000;

        Method method = OcrUtils.class.getDeclaredMethod("minLengthStrings", int.class, int.class, int.class );
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
    public void haveSubstring4() throws Exception {

        String text = "Il cane corre nel prato";
        String substring = "pato";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(1, r);

    }

    @Test
    public void haveSubstring5() throws Exception {

        String text = "totole";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(1, r);

    }

    @Test
    public void haveSubstring6() throws Exception {

        String text = "i";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(6, r);

    }

    @Test
    public void haveSubstring7() throws Exception {

        String text = "In questa frase c'è la parola totale";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(0, r);

    }

    @Test
    public void haveSubstring8() throws Exception {

        String text = "t o t a l e";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(0, r);

    }



    @Test
    public void haveSubstring9() throws Exception {

        String text = "Nella frase c'è la parola t q t a l e";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(1, r);

    }

    @Test
    public void haveSubstring10() throws Exception {

        String text = "Nella frase c'è la parola t q t";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(4, r);

    }

    @Test
    public void haveSubstring11() throws Exception {

        String text = "tot";
        String substring = "totale";

        Method method = OcrUtils.class.getDeclaredMethod("findSubstring", String.class, String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring);

        assertEquals(3, r);

    }

    @Test
    public void haveSubstringMax1() throws Exception {

        String text = "tot";
        String substring = "totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("findSubstringWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring,max);

        assertEquals(3, r);

    }

    @Test
    public void haveSubstringMax2() throws Exception {

        String text = "to";
        String substring = "totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("findSubstringWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring,max);

        assertEquals(3, r);

    }

    @Test
    public void haveSubstringMax3() throws Exception {

        String text = "totale";
        String substring = "totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("findSubstringWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring,max);

        assertEquals(0, r);

    }

    @Test
    public void haveSubstringMax4() throws Exception {

        String text = "i";
        String substring = "totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("findSubstringWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring,max);

        assertEquals(3, r);

    }

    @Test
    public void haveSubstringMax5() throws Exception {

        String text = "Tatola";
        String substring = "totale";
        Integer max = 3;

        Method method = OcrUtils.class.getDeclaredMethod("findSubstringWithMax", String.class, String.class, Integer.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,text,substring,max);

        assertEquals(3, r);

    }



    @Test
    public void FindDateTest1() throws Exception {

        String a = "il questo testo la data 23-06-19";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void FindDateTest2() throws Exception {

        String a = "il questo testo la 23-06-1995";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void FindDateTest3() throws Exception {

        String a = "il questo testo la data non è presenteuytr";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(-1, r);
    }

    @Test
    public void FindDateTest4() throws Exception {

        String a = "il questo testo la data non è 10 -";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(1, r);
    }

    @Test
    public void FindDateTest5() throws Exception {

        String a = "il questo testo la data non è 10- dsad ty23-06-m";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void FindDateTest6() throws Exception {

        String a = "il questo testo la data non è 10- dsad 2376-06-95";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void FindDateTest7() throws Exception {

        String a = "il questo testo la data non è  dsad 2jk hjh3-6-19";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void FindDateTest8() throws Exception {

        String a = "il questo test33-20-29od la data non è  dsad 2jk hjh3-6-19";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void FindDateTest9() throws Exception {

        String a = "il questo test33-20-29od la data non è  dsad 2jk hj h3-6-19";
        Method method = DataAnalyzer.class.getDeclaredMethod("findDate", String.class);
        method.setAccessible(true);
        int r = (int)method.invoke(null,a);

        assertEquals(0, r);
    }

    @Test
    public void GetDateTest1() throws Exception {

        String a = "la data è 23-06-1995";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "23-06-1995";
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetDateTest2() throws Exception {

        String a = "il questo testo la data non è presenteuytr";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        assertEquals(null, r);
    }

    @Test
    public void GetDateTest3() throws Exception {

        String a = "il questo testo la data non è 10- dsad 01-05-2017";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "01-05-2017";
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetDateTest4() throws Exception {

        String a = "in questo testo la data non è dsad 2jk hjh3-10-17";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "3-10-17";
        Date date = new SimpleDateFormat("dd-MM-yy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetDateTest5() throws Exception {

        String a = "il questo test 10/09/2017 od la data non è dsad 2jk";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "10/09/2017";
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetDateTest6() throws Exception {

        String a = "il questo è presente s.r.l";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        assertEquals(null, r);
    }

    @Test
    public void GetDateTest7() throws Exception {

        String a = "il questo test 10-9-2017 od la data non è dsad 2jk";
        Method method = DataAnalyzer.class.getDeclaredMethod("getDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "10-9-2017";
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetFormatDateTest1() throws Exception {

        String a = "10-9-17";
        Method method = DataAnalyzer.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "10-9-2017";
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetFormatDateTest2() throws Exception {

        String a = "10/9/17";
        Method method = DataAnalyzer.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "10/9/2017";
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetFormatDateTest3() throws Exception {

        String a = "10.09.2017";
        Method method = DataAnalyzer.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "10.9.2017";
        Date date = new SimpleDateFormat("dd.MM.yyyy").parse(stringToFind);

        assertEquals(date, r);
    }

    @Test
    public void GetFormatDateTest4() throws Exception {

        String a = "10.09.17";
        Method method = DataAnalyzer.class.getDeclaredMethod("parseDate", String.class);
        method.setAccessible(true);
        Date r = (Date) method.invoke(null,a);

        String stringToFind = "10.9.17";
        Date date = new SimpleDateFormat("dd.MM.yy").parse(stringToFind);

        assertEquals(date, r);


    }



}