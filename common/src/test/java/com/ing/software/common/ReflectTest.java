package com.ing.software.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.common.Reflect.*;
import static org.junit.Assert.*;

/**
 * @author Riccardo Zaglia
 */
public class ReflectTest {

    //invoke

    @Test
    @SuppressWarnings("UnnecessaryBoxing")
    public void testInvokeWithInstance() throws Exception {
        int sum = invoke(new TestClass(), "testPrivateWithParams", new Integer(10), 20.0f);
        assertEquals(30, sum);
    }

    @Test
    public void testInvokeWithClassType() throws Exception {
        TestClass r = invoke(TestClass.class, "testProtectedNoParamsReturnObject");
        assertNotEquals(null, r);
    }

    @Test
    public void testInvokeReturnVoid() throws Exception {
        TestClass tc = new TestClass();
        invoke(tc, "testPublicReturnVoid");
        assertEquals(1, tc.intField);
    }

    @Test
    public void testInvokeNullParam() throws Exception {
        int r = invoke(new TestClass(), "testPrivateWithParams", 0, null);
        assertEquals(-1, r);
    }


    @Test
    public void testInvokeOverload() throws Exception {
        int diff = invoke(new TestClass(), "testPrivateWithParams", 15, 5.0);
        assertEquals(10, diff);
    }


    // fieldVal

    @Test
    public void testFieldValPrimitive() throws Exception {
        assertEquals(1, fieldVal(new TestClass(), "intField"));
    }

    @Test
    public void testFieldValObj() throws Exception {
        assertNotEquals(null, fieldVal(TestClass.class, "staticObj"));
    }

    @Test
    public void testFieldValNull() throws Exception {
        assertEquals(null, fieldVal(new TestClass(), "nullObj"));
    }


    //These tests are not exhaustive but invoke and fieldVal will be used enough in other tests.
}

class TestClass {

    int intField = 1;
    protected static TestClass staticObj = new TestClass();
    private final TestClass nullObj = null;

    // sum
    private int testPrivateWithParams(int a, Float b) {
        if (b != null)
            return a + b.intValue();
        else
            return -1;
    }

    // difference (overload)
    private int testPrivateWithParams(int a, Double b) {
        if (b != null)
            return a - b.intValue();
        else
            return -1;
    }

    protected static TestClass testProtectedNoParamsReturnObject() {
        return new TestClass();
    }

    public void testPublicReturnVoid() {
        intField = 1;
    }
}