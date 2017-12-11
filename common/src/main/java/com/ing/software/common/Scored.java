package com.ing.software.common;

import android.support.annotation.NonNull;

/**
 * Make an object sortable by a score value.
 * @author Riccardo Zaglia
 */
public class Scored<T> implements Comparable<Scored<T>> {
    public double score;
    public T obj;

    public Scored(T obj, double score) {
        this.obj = obj;
        this.score = score;
    }

    @Override
    public int compareTo(@NonNull Scored<T> o) {
        return Double.valueOf(score).compareTo(o.score);
    }
}
