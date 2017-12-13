package com.ing.software.common;

import android.support.annotation.IntRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @param <T> Element type
 * @param <U> Model
 */
public class Ransac<T, U> {

    public interface ModelHandler<T, U> {
        void calcModelFromElements(List<T> eList, Ref<U> modRef);
        double inverseError(U mod, T elem);
    }

    private List<T> elems;
    private int maxIters;
    private double maxErr;
    private ModelHandler<T, U> hdl;
    private Random rnd = new Random();
    private int n;

    public Ransac(List<T> elements, int minNforModel, @IntRange(from = 1) int maxIters, double maxErr, ModelHandler<T, U> modHdl) {
        this.elems = elements;
        this.maxIters = maxIters;
        this.maxErr = maxErr;
        n = minNforModel;
        hdl = modHdl;
    }

    public boolean findModel(Ref<U> modelRef) {
        double minErr = 0;
        int iters = 0;
        while (iters < maxIters) {
            List<Boolean> outliers = new ArrayList<>(Collections.nCopies(elems.size(), true));
            List<T> inliers = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int idx = rnd.nextInt(elems.size());
                inliers.add(elems.get(idx));
                outliers.set(idx, false);
            }
            Ref<U> newModRef = new Ref<>(null);
            hdl.calcModelFromElements(inliers, newModRef);

            List<T> consensus = new ArrayList<>(inliers);
            for (int i = 0; i < elems.size(); i++) {
                //consensus.add()
            }
            iters++;
        }
        return false;
    }

}
