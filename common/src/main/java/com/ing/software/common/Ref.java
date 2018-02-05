package com.ing.software.common;

/**
 * Wrapper for pass-by-reference in java
 * @author Riccardo Zaglia
 */
public class Ref<T> {
    public T val;

    public Ref() { val = null; }

    public Ref(T obj) { val = obj; }
}
