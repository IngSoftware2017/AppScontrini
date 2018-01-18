package com.ing.software.common;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.SizeF;

import com.annimon.stream.Stream;

import java.util.List;
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
    public static float dist(@NonNull PointF pt1, @NonNull PointF pt2) {
        double xDiff = pt1.x - pt2.x, yDiff = pt1.y - pt2.y;
        return (float)sqrt(xDiff * xDiff + yDiff * yDiff);
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
    public static List<PointF> pointToPointF(@NonNull List<Point> pts) {
        return Stream.of(pts).map(PointF::new).toList();
    }

    /**
     * Convert a RectF into a list of PointF (ordered counter-clockwise from top-left).
     * @param rect RectF
     * @return List of PointF
     */
    @NonNull
    public static List<PointF> rectToPointFs(@NonNull RectF rect) {
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


    /**
     * One dimension linear transformation of a point from one interval space to another.
     * @param p one-dimensional point
     * @param srcStart start of source interval
     * @param srcEnd end of source interval
     * @param dstStart start of destination interval
     * @param dstEnd end of destination interval
     * @return transformed point
     */
    public static float transform(float p, float srcStart, float srcEnd, float dstStart, float dstEnd) {
        return (p - srcStart) / (srcEnd - srcStart) * (dstEnd - dstStart) + dstStart;
    }


    public static float transform(float length, float srcSize, float dstSize) {
        return length * dstSize / srcSize;
    }

    /**
     * Two dimensional linear transformation of a point from one space to another.
     * The the horizontal and vertical transformation are applied independently one at a time.
     * @param p two-dimensional point
     * @param srcRect source space
     * @param dstRect destination space
     * @return transformed point
     */
    public static PointF transform(PointF p, RectF srcRect, RectF dstRect) {
        return new PointF(transform(p.x, srcRect.left, srcRect.right, dstRect.left, dstRect.right),
                transform(p.y, srcRect.top, srcRect.bottom, dstRect.top, dstRect.bottom));
    }

    /**
     * Linear transformation of a list of points from one space to another.
     * @param pts list of points
     * @param srcRect source space
     * @param dstRect destination space
     * @return transformed list of points
     */
    public static List<PointF> transform(List<PointF> pts, RectF srcRect, RectF dstRect) {
        return Stream.of(pts).map(p -> transform(p, srcRect, dstRect)).toList();
    }

    /**
     * Linear transformation of a rectangle from one space to another.
     * @param rect rectangle to be transformed
     * @param srcRect source space
     * @param dstRect destination space
     * @return transformed rectangle
     */
    public static RectF transform(RectF rect, RectF srcRect, RectF dstRect) {
        return new RectF(transform(rect.left, srcRect.left, srcRect.right, dstRect.left, dstRect.right),
                transform(rect.right, srcRect.left, srcRect.right, dstRect.left, dstRect.right),
                transform(rect.top, srcRect.top, srcRect.bottom, dstRect.top, dstRect.bottom),
                transform(rect.bottom, srcRect.top, srcRect.bottom, dstRect.top, dstRect.bottom));
    }
}
