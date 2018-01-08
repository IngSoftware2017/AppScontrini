package com.ing.software.common;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.SizeF;

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
    public static double dist(@NonNull PointF pt1, @NonNull PointF pt2) {
        double xDiff = pt1.x - pt2.x, yDiff = pt1.y - pt2.y;
        return sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * Calculate "num" mod "den".
     * @param num any number
     * @param den >= 0
     * @return modulus
     */
    public static int mod(int num, int den) {
        return num < 0 ? (num % den + den) % den : num % den;
    }

    /**
     * Convert a list of int points to a list of float points.
     * @param pts List of Point
     * @return List of PointF
     */
    public static List<PointF> ptsToPtsF(@NonNull List<Point> pts) {
        return Stream.of(pts).map(PointF::new).toList();
    }

    /**
     * Convert a RectF into a list of PointF (ordered counter-clockwise from top-left).
     * @param rect RectF
     * @return List of PointF
     */
    @NonNull
    public static List<PointF> rectToPts(@NonNull RectF rect) {
        return asList(
                new PointF(rect.left, rect.top),
                new PointF(rect.left, rect.bottom),
                new PointF(rect.right, rect.bottom),
                new PointF(rect.right, rect.top)
        );
    }

    /**
     * Get size of a Bitmap
     * @param bm bitmap. Not null.
     * @return Size
     */
    @NonNull
    public static SizeF size(@NonNull Bitmap bm) {
        return new SizeF(bm.getWidth(), bm.getHeight());
    }

    /**
     * Get size of a rectangle
     * @param rect RectF. Not null.
     * @return Size
     */
    @NonNull
    public static SizeF size(@NonNull RectF rect) {
        return new SizeF(rect.width(), rect.height());
    }
}
