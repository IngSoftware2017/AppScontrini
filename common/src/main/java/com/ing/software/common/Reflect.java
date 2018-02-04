package com.ing.software.common;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Riccardo Zaglia
 */
public class Reflect {

    /**
     * Invoke a static or non static method (with any access level) of a class.
     *
     * @param clazz A class instance or class type. Not null
     * @param method Method name. Not null
     * @param params List of arguments of the method. They can be null.
     * @param <T> return type of the method.
     * @return return value of the method or void.
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
    public static <T> T invoke(@NonNull Object clazz, @NonNull String method, Object... params) throws Exception {
        //get type of parameters, or null if null
        List<Class<?>> givenParamTypes = new ArrayList<>();
        for (Object param : params)
            givenParamTypes.add(param != null ? param.getClass() : null);

        boolean isType = clazz instanceof Class<?>;
        Class<?> classType = isType ? (Class<?>)clazz : clazz.getClass();

        for (Method m : classType.getDeclaredMethods()) {
            Class<?>[] methodParamTypes = m.getParameterTypes();
            if (m.getName().equals(method)
                    && methodParamTypes.length == givenParamTypes.size()) {

                boolean paramsMatch = true;
                for (int i = 0; i < methodParamTypes.length; i++) {
                    Class<?> need = methodParamTypes[i],
                            got = givenParamTypes.get(i);

                    //Problem: since params is an array of objects, primitive types are boxed to respective wrappers.
                    // so if a wrapper is passed, we accept it as if it was the primitive type.
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
                    try {
                        return (T)m.invoke(isType ? null : clazz, params);
                    } catch (InvocationTargetException e) {
                        // log where the real exception occurred
                        e.getCause().printStackTrace();
                        throw e;
                    }
                }
            }
        }
        throw new NoSuchMethodException();
    }

    /**
     * Common code for getField and setField
     */
    private static Field getFieldInstance(Object clazz, String fieldName) throws Exception {
        boolean isType = clazz instanceof Class<?>;
        Class<?> classType = isType ? (Class<?>)clazz : clazz.getClass();
        Field field = classType.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Get value of a static or non static field (with any access level).
     * @param clazz A class instance or class type. Not null
     * @param fieldName Field name. Not null
     * @param <T> Type of the field.
     * @return Value of the field.
     *
     * @throws Exception:
     *  NullPointerException: Trying to get an instance field passing a class type.
     *  ClassCastException: Field type mismatch.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(@NonNull Object clazz, @NonNull String fieldName) throws Exception {
        return (T)getFieldInstance(clazz, fieldName).get(clazz instanceof Class<?> ? null : clazz);
    }

    /**
     * Set value of a static or non static field (with any access level).
     * @param clazz A class instance or class type. Not null
     * @param fieldName Field name. Not null
     * @param newVal New value of the field.
     *
     * @throws Exception:
     *  NullPointerException: Trying to set an instance field passing a class type.
     */
    public static void setField(@NonNull Object clazz, @NonNull String fieldName, Object newVal) throws Exception {
        getFieldInstance(clazz, fieldName).set(clazz instanceof Class<?> ? null : clazz, newVal);
    }
}
