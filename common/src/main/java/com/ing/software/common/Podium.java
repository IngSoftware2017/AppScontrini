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
    private int tgtSize = 0;
    private PriorityQueue<T> pq = null;
    // NB: the first element is the lowest/worst one

    /**
     * New podium
     * @param k > 0. Size of podium.
     */
    public Podium(@Size(min = 1) int k) {
        tgtSize = k;
        pq = new PriorityQueue<>(tgtSize + 1);
    }

    /**
     * Try add element to podium.
     * Complexity: Theta(1); O(k)
     * @param obj element to add. Not null.
     * @return true if obj is added to podium, false otherwise
     */
    public boolean tryAdd(@NonNull T obj) {
        // add the object to podium and remove the last object if podium size > k
        pq.offer(obj);
        return pq.size() <= tgtSize || pq.poll() != obj; // if last is obj -> add failed
    }

    /**
     * Try add all elements in objList to podium.
     * @param objList elements to add. Not null. Each element must not be null
     */
    public void tryAddAll(@NonNull List<T> objList) {
        for (T obj : objList)
            tryAdd(obj);
    }

    /**
     * Get all elements in podium (descending order).
     * @return array of all elements
     */
    public List<T> getAll() {
        List<T> l = new ArrayList<>();
        while (!pq.isEmpty())
            l.add(0, pq.poll());
        return l;
    }
}
