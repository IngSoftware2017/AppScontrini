package com.ing.software.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
     *
     * @throws Exception:
     *  NoSuchMethodException: The method name, the number or type of parameters is wrong
     *  NullPointerException: Calling an instance method with a class type.
     *  ClassCastException: Return type mismatch.
     *  Other exception: Something went wrong. The causes might be:
     *                    * parameters incompatible with method annotations;
     *                    * exception raised inside method.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object clazz, String methodName, Object... params) throws Exception {
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
                    return (T)m.invoke(isType ? null : clazz, params);
                }
            }
        }
        throw new NoSuchMethodException();
    }

    /**
     * Get value of a static or non static field (with any access level).
     * @param clazz A class instance or class type. Not null
     * @param fieldName Field name. Not null
     * @param <T> type of the field.
     * @return value of the field.
     *
     * @throws Exception exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T fieldVal(Object clazz, String fieldName) throws Exception {
        boolean isType = clazz instanceof Class<?>;
        Field f = (isType ? (Class<?>)clazz : clazz.getClass()).getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T)f.get(isType ? null : clazz);
    }

}
