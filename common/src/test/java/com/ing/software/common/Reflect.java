package com.ing.software.common;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Riccardo Zaglia
 */
public class Reflect {

    public static class UnknownException extends Exception {
    }

    /**
     * Invoke a static or non static method (with any access level) of a class.
     *
     * @param clazz A class instance or class type. Not null
     * @param methodName Method name. Not null
     * @param params List of arguments of the method. They can be null.
     * @param <T> return type of the method.
     * @return return of the method or void.
     * @throws NoSuchMethodException: The method name, the number or type of parameters is wrong
     * @throws NullPointerException: Calling an instance method with a class type.
     * @throws ClassCastException: Return type mismatch.
     * @throws UnknownException: Something went wrong.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object clazz, String methodName, Object... params)
            throws NoSuchMethodException, NullPointerException, ClassCastException, UnknownException {
        List<Class<?>> paramsTypes = new ArrayList<>(params.length);
        for (Object p : params) {
            if (p != null)
                paramsTypes.add(p.getClass());
            else
                paramsTypes.add(null);
        }

        boolean isType = clazz instanceof Class<?>;
        Method[] methods = (isType ? (Class<?>)clazz : clazz.getClass()).getDeclaredMethods();

        for (Method m : methods) {
            Class<?>[] mParamsTypes = m.getParameterTypes();
            if (m.getName().equals(methodName) && mParamsTypes.length == paramsTypes.size()) {
                boolean paramsMatch = true;
                for (int i = 0; i < mParamsTypes.length; i++) {
                    Class<?> need = mParamsTypes[i], got = paramsTypes.get(i);

                    //Problem: since params is an array of objects, primitive types are boxed to respective wrappers.
                    // so if a wrapper is passed, we accept the match with the primitive type.
                    paramsMatch &= (boolean.class.equals(need) && Boolean.class.equals(got))
                            || (byte.class.equals(need) && Byte.class.equals(got))
                            || (short.class.equals(need) && Short.class.equals(got))
                            || (int.class.equals(need) && Integer.class.equals(got))
                            || (long.class.equals(need) && Long.class.equals(got))
                            || (float.class.equals(need) && Float.class.equals(got))
                            || (double.class.equals(need) && Double.class.equals(got))
                            || (char.class.equals(need) && Character.class.equals(got))
                            //now we know "need" is an object
                            || got == null || need.isAssignableFrom(got);
                }
                if (paramsMatch) {
                    m.setAccessible(true);

                    //Exception if there is a return type mismatch (cannot handle it in unit tests)
                    try {
                        return (T)m.invoke(isType ? null : clazz, params);
                    }
                    catch (NullPointerException | ClassCastException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new UnknownException();
                    }

                }
            }
        }
        throw new NoSuchMethodException();
    }

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