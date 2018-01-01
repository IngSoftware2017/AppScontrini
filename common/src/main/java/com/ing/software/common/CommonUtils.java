package com.ing.software.common;

import android.graphics.*;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;
import static java.util.Collections.*;
import static java.util.Arrays.*;
import static java.lang.Math.*;

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
        return sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * Convert a list of int points to a list of float points.
     * @param pts List of Point
     * @return List of PointF
     */
    public static List<PointF> ptsToPtsF(List<Point> pts) {
        return Stream.of(pts).map(PointF::new).toList();
    }

    /**
     * Convert a RectF into a list of PointF (ordered counter-clockwise from top-left).
     * @param rect RectF
     * @return List of PointF
     */
    public static List<PointF> rectToPts(RectF rect) {
        return asList(
                new PointF(rect.left, rect.top),
                new PointF(rect.left, rect.bottom),
                new PointF(rect.right, rect.bottom),
                new PointF(rect.right, rect.top)
        );
    }
}
