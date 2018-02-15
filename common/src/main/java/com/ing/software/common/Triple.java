package com.ing.software.common;

/**
 * Structure containing three objects of independent types.
 * @param <T1> first object type
 * @param <T2> second object type
 * @param <T3> third object type
 */
public class Triple<T1, T2, T3> {

    public T1 first;
    public T2 second;
    public T3 third;

    /**
     * New Triple
     * @param first first object
     * @param second second object
     * @param third third object
     */
    public Triple(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
