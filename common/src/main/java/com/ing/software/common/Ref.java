package com.ing.software.common;

/**
 * Wrapper for pass-by-reference in java
 * @author Riccardo Zaglia
 */
public class Ref<T> {
    public T value;
    public Ref(T obj) {
        value = obj;
    }
}
