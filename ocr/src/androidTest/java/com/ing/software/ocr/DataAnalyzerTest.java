package com.ing.software.ocr;


import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Can't test directly with the equivalent BigDecimal cause of approximation
 */
public class DataAnalyzerTest {

    @Test
    public void analyzeAmountSingleDigit() throws Exception {
        String amount = "5";
        BigDecimal expected = new BigDecimal(5).setScale(2, RoundingMode.HALF_UP);
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    public void analyzeAmountDecimal() throws Exception {
        String amount = "5.33";
        BigDecimal expected = new BigDecimal(5.33).setScale(2, RoundingMode.HALF_UP);
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    public void analyzeAmountSingleLetter() throws Exception {
        String amount = "b";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(null, result);
    }

    @Test
    public void analyzeAmountSingleCharLett() throws Exception {
        String amount = "5c";
        BigDecimal expected = null; //less than 3/4 of length
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void analyzeAmountComma() throws Exception {
        String amount = "5,33";
        BigDecimal expected = new BigDecimal(5.33).setScale(2, RoundingMode.HALF_UP);
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    public void analyzeAmountLastCharIsPoint() throws Exception {
        String amount = "5.33.";
        BigDecimal expected = new BigDecimal(5.33).setScale(2, RoundingMode.HALF_UP);
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(0, expected.compareTo(result));
    }

	/*
    @Test
    public void analyzeAmountMaxDouble() throws Exception {
        String amount = "" + Double.MAX_VALUE;
        double expected = Double.MAX_VALUE;
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        double resultD = result.doubleValue();
        assertEquals(expected, resultD);
    }

    @Test
    public void analyzeAmountExpMixed() throws Exception {
        String amount = "3E+12ii";
        BigDecimal expected = new BigDecimal("3E+12");
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void analyzeAmountMaxDoubleDirect() throws Exception {
        String amount = "" + Double.MAX_VALUE;
        String expected = amount;
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmount", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void analyzeAmountExponential() throws Exception {
        String amount = "5E+2";
        String expected = "5E+2";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmount", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }
	*/

    @Test
    public void isExpNegative() throws Exception {
        String amount = "5E-2";
        int start = 1;
        Method method = DataAnalyzer.class.getDeclaredMethod("isExp", String.class, int.class);
        method.setAccessible(true);
        boolean result = (boolean)method.invoke(null,amount, start);
        assertEquals(true, result);
    }

    @Test
    public void isExpNoSign() throws Exception {
        String amount = "1.7976931348623157E308";
        int start = 18;
        Method method = DataAnalyzer.class.getDeclaredMethod("isExp", String.class, int.class);
        method.setAccessible(true);
        boolean result = (boolean)method.invoke(null,amount, start);
        assertEquals(true, result);
    }

    @Test
    public void isNoExp() throws Exception {
        String amount = "1.7976931348623157E308";
        int start = 15;
        Method method = DataAnalyzer.class.getDeclaredMethod("isExp", String.class, int.class);
        method.setAccessible(true);
        boolean result = (boolean)method.invoke(null,amount, start);
        assertEquals(false, result);
    }

    @Test
    public void isNoExpLength() throws Exception {
        String amount = "1.7976931348623157E308";
        int start = 19;
        Method method = DataAnalyzer.class.getDeclaredMethod("isExp", String.class, int.class);
        method.setAccessible(true);
        boolean result = (boolean)method.invoke(null,amount, start);
        assertEquals(false, result);
    }

    @Test
    public void getExpPositive() throws Exception {
        String amount = "5E+2";
        int start = 1;
        Method method = DataAnalyzer.class.getDeclaredMethod("getExp", String.class, int.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount, start);
        assertEquals("E+", result);
    }

    @Test
    public void getExpNoSign() throws Exception {
        String amount = "1.7976931348623157E308";
        int start = 18;
        Method method = DataAnalyzer.class.getDeclaredMethod("getExp", String.class, int.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount, start);
        assertEquals("E", result);
    }

    @Test
    public void test2Char() throws Exception {
        String amount = "21";
        String expected = "21.0";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void test3Char() throws Exception {
        String amount = "211";
        String expected = "21.1";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void test3CharDPD() throws Exception {
        String amount = "2.1";
        String expected = "02.1";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void test3CharPDD() throws Exception {
        String amount = ".11";
        String expected = "00.11";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void test3CharDDP() throws Exception {
        String amount = "21.";
        String expected = "21.0";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void test2CharDP() throws Exception {
        String amount = "2.";
        String expected = "02.0";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void test2CharPD() throws Exception {
        String amount = ".2";
        String expected = "00.2";
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeChars", String.class);
        method.setAccessible(true);
        StringBuilder result = (StringBuilder)method.invoke(null,amount);
        assertEquals(expected, result.toString());
    }

    @Test
    public void deepAmount2SingleLetter() throws Exception {
        String amount = "5S";
        String expected = "0.55";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmountChars", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void deepAmount2Comma() throws Exception {
        String amount = "111,5";
        String expected = "111.50";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmountChars", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void deepAmount2DoublePoint() throws Exception {
        String amount = "7.87.5";
        String expected = "787.50";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmountChars", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void deepAmount2SingleDigit() throws Exception {
        String amount = "1";
        String expected = "1.00";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmountChars", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void deepAmount2NoPoint() throws Exception {
        String amount = "250";
        String expected = "2.50";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmountChars", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }

    @Test
    public void deepAmount2NoPointMain() throws Exception {
        String amount = "250";
        BigDecimal expected = new BigDecimal("2.50");
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal) method.invoke(null,amount);
        assertTrue(expected.compareTo(result) == 0);
    }

    @Test
    public void deepAmount2ThreeDecimals() throws Exception {
        String amount = "2.500";
        String expected = "2.50";
        Method method = DataAnalyzer.class.getDeclaredMethod("deepAnalyzeAmountChars", String.class);
        method.setAccessible(true);
        String result = (String)method.invoke(null,amount);
        assertEquals(expected, result);
    }
}
