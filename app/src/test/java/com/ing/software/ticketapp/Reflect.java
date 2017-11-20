package com.ing.software.ticketapp;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class Reflect {

    /**
     * Invoke a static or non static method (with any access level) of a class.
     * NoSuchMethodException: The method name, the number or type of parameters is wrong
     * NullPointerException: calling an instance method with a class type.
     * ClassCastException: Return type mismatch.
     *
     * @param clazz A class instance or class type
     * @param methodName Method name
     * @param params List of arguments of the method.
     * @param <T> return type of the method.
     * @return return of the method or void.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object clazz, String methodName, Object... params)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Class<?>> paramsTypes = new ArrayList<>(params.length);
        for (Object p : params)
            paramsTypes.add(p.getClass());

        boolean isType = clazz instanceof Class<?>;

        Method[] methods = (isType ? (Class<?>)clazz : clazz.getClass()).getDeclaredMethods();

        for (Method m : methods) {
            Class<?>[] mParamsTypes = m.getParameterTypes();
            if (m.getName().equals(methodName) && mParamsTypes.length == paramsTypes.size()) {
                boolean paramsMatch = true;
                for (int i = 0; i < mParamsTypes.length; i++) {
                    Class<?> need = mParamsTypes[i], got = paramsTypes.get(i);

                    //Problem: since params is a list of objects, primitive types are boxed to respective wrappers.
                    // so if a wrapper is passed, we accept the match with the primitive type.
                    paramsMatch &= need.isAssignableFrom(got)
                            || (boolean.class.equals(need) && Boolean.class.equals(got))
                            || (byte.class.equals(need) && Byte.class.equals(got))
                            || (short.class.equals(need) && Short.class.equals(got))
                            || (int.class.equals(need) && Integer.class.equals(got))
                            || (long.class.equals(need) && Long.class.equals(got))
                            || (float.class.equals(need) && Float.class.equals(got))
                            || (double.class.equals(need) && Double.class.equals(got))
                            || (char.class.equals(need) && Character.class.equals(got));
                }
                if (paramsMatch) {
                    m.setAccessible(true);
                    //Exception if there is a return type mismatch (cannot handle it in unit tests)
                    return (T) m.invoke(isType ? null : clazz, params);
                }
            }
        }
        throw new NoSuchMethodException();
    }

    @Test
    public void testInvokeWithInstance() throws Exception {
        int r = invoke(new TestClass(), "testPrivateWithParams", 10, 20.0f);
        assertEquals(30, r);
    }

    @Test
    public void testInvokeWithClassType() throws Exception {
        TestClass r = invoke(TestClass.class, "testProtectedNoParamsReturnObject");
    }

    @Test
    public void testInvokeInnerClass() throws Exception {
        TestClass tc = new TestClass();
        invoke(tc, "testPublicReturnVoid");
        assertEquals(1, tc.field);
    }

    //These tests are not exhaustive but invoke will be used enough to be certain it's bug free
}

class TestClass {

    int field = 0;

    private int testPrivateWithParams(int a, Float b){
        return a + b.intValue();
    }

    protected static TestClass testProtectedNoParamsReturnObject(){
        return new TestClass();
    }

    public void testPublicReturnVoid(){
        field = 1;
    }
}