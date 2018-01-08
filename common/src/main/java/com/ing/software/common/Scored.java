package com.ing.software.common;

/**
 * Convenience subclass of CompPair with double comparable.
 * @param <T> Object
 */
public class Scored<T> extends CompPair<Double, T> {
    public Scored(Double score, T obj) {
        super(score, obj);
    }
}