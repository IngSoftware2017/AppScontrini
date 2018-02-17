package com.ing.software.common;

import android.support.annotation.NonNull;

/**
 * Convenience class that uses a CompPair with double comparable.
 * @param <T> Object
 * @author Riccardo Zaglia
 */
public class Scored<T> implements Comparable<Scored<T>> { // cannot inherit from CompPair because
    private CompPair<Double, T> pair;                     // it would make this class incompatible with Podium somehow

    /**
     * New Scored object
     * @param score score value.
     * @param obj new object. Can be null.
     */
    public Scored(double score, T obj) { pair = new CompPair<>(score, obj); }

    /**
     * Get object associated with this Scored instance
     * @return object
     */
    public T obj() { return pair.obj; }

    /**
     * Get score associated with this Scored instance.
     * @return score
     */
    public double getScore() { return pair.comp; }

    /**
     * Reset score
     * @param score new score
     */
    public void setScore(double score) { pair.comp = score; }

    /**
     * Compare this Scored instance score with another Scored instance score
     * @param s other Scored instance
     * @return 0: scores are equal; 1: this score is higher; -1: other score is higher
     */
    @Override
    public int compareTo(@NonNull Scored<T> s) {  return pair.compareTo(s.pair); }
}