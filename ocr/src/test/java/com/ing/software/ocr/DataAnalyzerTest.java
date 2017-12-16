package com.ing.software.ocr;


import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

/**
 * Can't test directly with the equivalent BigDecimal cause of approximation
 */

public class DataAnalyzerTest {

    @Test
    public void analyzeAmountSingleDigit() throws Exception {
        String amount = "5";
        double expected = 5;
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        double resulD = result.doubleValue();
        assertEquals(expected, resulD);
    }

    @Test
    public void analyzeAmountDecimal() throws Exception {
        String amount = "5.33";
        double expected = 5.33;
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        double resulD = result.doubleValue();
        assertEquals(expected, resulD);
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
        double expected = 5;
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        double resulD = result.doubleValue();
        assertEquals(expected, resulD);
    }

    @Test
    public void analyzeAmountComma() throws Exception {
        String amount = "5,33";
        double expected = 5.33;
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        double resulD = result.doubleValue();
        assertEquals(expected, resulD);
    }

    @Test
    public void analyzeAmountLastCharIsPoint() throws Exception {
        String amount = "5.33.";
        double expected = 5.33;
        Method method = DataAnalyzer.class.getDeclaredMethod("analyzeAmount", String.class);
        method.setAccessible(true);
        BigDecimal result = (BigDecimal)method.invoke(null,amount);
        double resulD = result.doubleValue();
        assertEquals(expected, resulD);
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
}
