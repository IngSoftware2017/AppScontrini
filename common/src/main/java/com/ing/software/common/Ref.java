package com.ing.software.common;

/**
 * Wrapper for pass-by-reference in java
 * @author Riccardo Zaglia
 */
public class Ref<T> {
    public T val;

    /**
     * New Ref with null value
     */
    public Ref() { val = null; }

    /**
     * New Ref with specified value
     * @param value new value
     */
    public Ref(T value) { val = value; }
}
