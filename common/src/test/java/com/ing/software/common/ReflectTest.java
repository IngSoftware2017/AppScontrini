package com.ing.software.common;

import org.junit.Test;

import static com.ing.software.common.Reflect.*;
import static org.junit.Assert.*;

/**
 * @author Riccardo Zaglia
 */
public class ReflectTest {

    @Test
    @SuppressWarnings("UnnecessaryBoxing")
    public void testInvokeWithInstance() throws Exception {
        int r = invoke(new TestClass(), "testPrivateWithParams", new Integer(10), 20.0f);
        assertEquals(30, r);
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
        assertEquals(1, tc.field);
    }

    @Test
    public void testInvokeNullParam() throws Exception {
        int r = invoke(new TestClass(), "testPrivateWithParams", 0, null);
        assertEquals(-1, r);
    }

    //These tests are not exhaustive but invoke will be used enough in other tests.
}

class TestClass {

    int field = 0;

    private int testPrivateWithParams(int a, Float b){
        if (b != null)
            return a + b.intValue();
        else
            return -1;
    }

    protected static TestClass testProtectedNoParamsReturnObject(){
        return new TestClass();
    }

    public void testPublicReturnVoid(){
        field = 1;
    }
}