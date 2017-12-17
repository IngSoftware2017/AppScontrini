package com.ing.software.common;

import com.annimon.stream.function.Supplier;

import java.util.Hashtable;

/**
 * WIP
 * @param <T>
 */
public class Pool<T> {

    Supplier<T> gen;


    private Hashtable<T, Long> locked, unlocked;

    public Pool(Supplier<T> generator) {
        gen = generator;
    }

    public T get() {
        return gen.get();
    }


}
