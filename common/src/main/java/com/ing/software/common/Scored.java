package com.ing.software.common;

import android.support.annotation.NonNull;

/**
 * Convenience class that uses a CompPair with double comparable.
 * @param <T> Object
 */
public class Scored<T> implements Comparable<Scored<T>> { // cannot inherit from CompPair because
    private CompPair<Double, T> pair;                    // it would make this class incompatible with Podium somehow

    public Scored(Double score, T obj) {
        pair = new CompPair<>(score, obj);
    }

    public T obj() {
        return pair.obj;
    }

    @Override
    public int compareTo(@NonNull Scored<T> s) {
        return pair.compareTo(s.pair);
    }
}