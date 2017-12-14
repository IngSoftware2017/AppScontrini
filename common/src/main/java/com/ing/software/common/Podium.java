package com.ing.software.common;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Sorted data structure (descending order) used to select best k elements.
 * @author Riccardo Zaglia
 */
public class Podium<T extends Comparable<T>> {
    private int k = 0;
    private PriorityQueue<T> pq = null;
    // NB: the first element is the lowest/worst one

    /**
     * New podium
     * @param k > 0. Size of podium.
     */
    public Podium(@Size(min = 1) int k) {
        this.k = k;
        pq = new PriorityQueue<>(k + 1);
    }

    /**
     * Try add element to podium.
     * Complexity: Theta(1); O(k)
     * @param obj element to add. Not null.
     * @return true if obj is added to podium, false otherwise
     */
    public boolean tryAdd(@NonNull T obj) {
        pq.offer(obj);
        return pq.size() <= k || pq.poll() != obj; // if lowest is obj -> add failed
    }

    /**
     * Get all elements in podium (descending order).
     * @return array of all elements
     */
    @SuppressWarnings("unchecked")
    public List<T> getAll() { //with generics I cannot instantiate arrays.
        List<T> l = new ArrayList<>();
        while (!pq.isEmpty())
            l.add(0, pq.poll());
        return l;
    }
}
