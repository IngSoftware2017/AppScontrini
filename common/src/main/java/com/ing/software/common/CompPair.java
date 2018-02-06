package com.ing.software.common;

import android.support.annotation.NonNull;

/**
 * Make an object sortable by a Comparable.
 * @author Riccardo Zaglia
 */
public class CompPair<C extends Comparable<C>, T> implements Comparable<CompPair<C, T>> {
    public C comp;
    public T obj;

    public CompPair(@NonNull C comp, T obj) {
        this.comp = comp;
        this.obj = obj;
    }

    @Override
    public int compareTo(@NonNull CompPair<C, T> o) { return comp.compareTo(o.comp); }
}
