package com.ing.software.common;

import android.graphics.*;
import com.annimon.stream.Stream;
import java.util.List;

/**
 * @author Riccardo Zaglia
 */
public class CommonUtils {

    /**
     * Euclidean distance
     * @param pt1 point 1
     * @param pt2 point 2
     * @return distance
     */
    public static double dist(PointF pt1, PointF pt2) {
        double xDiff = pt1.x - pt2.x, yDiff = pt1.y - pt2.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * Convert a list of int points to a list of float points.
     * @param pts List of Point
     * @return List of PointF
     */
    public static List<PointF> ptsToPtsF(List<Point> pts) {
        return Stream.of(pts).map(p -> new PointF(p.x, p.y)).toList();
    }
}
