package com.ing.software.common;

import android.support.annotation.NonNull;

/**
 * Convenience class that uses a CompPair with double comparable.
 * @param <T> Object
 */
public class Scored<T> implements Comparable<Scored<T>> { // cannot inherit from CompPair because
    private CompPair<Double, T> pair;                    // it would make this class incompatible with Podium somehow

    public Scored(double score, T obj) {
        pair = new CompPair<>(score, obj);
    }

    public T obj() {
        return pair.obj;
    }

    public double getScore() {
        return pair.comp;
    }

    public void setScore(double score) {
        pair.comp = score;
    }

    @Override
    public int compareTo(@NonNull Scored<T> s) {
        return pair.compareTo(s.pair);
    }
}