package com.ing.software.common;

import com.annimon.stream.function.Supplier;

/**
 * Create and recycle two objects to avoid creating new instances.
 * @param <T> object type.
 */
public class Swap<T> {
    public T first, second;

    /**
     * Create objects from a generator.
     * @param factory the generator functional.
     */
    public Swap(Supplier<T> factory) {
        first = factory.get();
        second = factory.get();
    }

    /**
     * Assign two already created objects.
     * @param first object
     * @param second object
     */
    public Swap(T first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Swap first and second object and return the new first object.
     * @return the new first object.
     */
    public T swap() {
        T tmp = second;
        second = first;
        first = tmp;
        return first;
    }
}
