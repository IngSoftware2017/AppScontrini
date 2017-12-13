package com.ing.software.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Riccardo Zaglia
 */
public class CommonUtils {

    /**
     * Zip two lists into a list of CompPair
     * @param compList list of comparable objects.
     * @param objList list of objects. Must be the same length of compList.
     * @param <C> Comparable
     * @param <T> Object
     * @return List of CompPair
     */
    @Deprecated //use Stream.zip().toList()
    public static <C extends Comparable<C>, T>
    List<CompPair<C, T>> zipComp(List<C> compList, List<T> objList) {
        List<CompPair<C, T>> zipped = new ArrayList<>();
        for (int i = 0; i < compList.size(); i++)
            zipped.add(new CompPair<>(compList.get(i), objList.get(i)));
        return zipped;
    }

    /**
     * Unzip a lists of CompPair into a list of obj of CompPair.
     * @param compPairList list of CompPair.
     * @param <T> Object
     * @return List of T objects
     */
    @Deprecated //use Stream.of().map(()->{}).toList()
    public static <C extends Comparable<C>, T>
    List<T> unzipComp(List<CompPair<C, T>> compPairList) {
        List<T> unzipped = new ArrayList<>();
        for (int i = 0; i < compPairList.size(); i++)
            unzipped.add(compPairList.get(i).obj);
        return unzipped;
    }
}
