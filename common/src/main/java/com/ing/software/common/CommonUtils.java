package com.ing.software.common;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.SizeF;

import com.annimon.stream.Stream;

import java.util.List;
import static java.util.Collections.*;
import static java.util.Arrays.*;
import static java.lang.Math.*;

/**
 * General utilities
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
    public static List<PointF> pointsToPointFs(@NonNull List<Point> pts) {
        return Stream.of(pts).map(PointF::new).toList();
    }

    /**
     * Convert a RectF into a list of PointF (ordered counter-clockwise from top-left).
     * @param rect RectF
     * @return List of PointF
     */
    public static List<PointF> rectToPts(@NonNull RectF rect) {
        return asList(
                new PointF(rect.left, rect.top),
                new PointF(rect.left, rect.bottom),
                new PointF(rect.right, rect.bottom),
                new PointF(rect.right, rect.top)
        );
    }

    /**
     * Get an origin aligned rectangle from its dimensions.
     * @param size rectangle dimensions
     * @return rectangle
     */
    public static RectF rectFromSize(@NonNull SizeF size) {
        return new RectF(0, 0, size.getWidth(), size.getHeight());
    }

    /**
     * Get size of a Bitmap
     * @param bm bitmap. Not null.
     * @return Size
     */
    public static SizeF size(@NonNull Bitmap bm) {
        return new SizeF(bm.getWidth(), bm.getHeight());
    }

    /**
     * Get size of a rectangle
     * @param rect RectF. Not null.
     * @return Size
     */
    public static SizeF size(@NonNull RectF rect) { return new SizeF(rect.width(), rect.height()); }


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

    /**
     * Scale a length, with some ratio with srcSize, in order to have the same ratio with dstSize.
     * @param length dimension to scale
     * @param srcSize source space scale reference
     * @param dstSize destination space scale reference
     * @return new scaled length
     */
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
                transform(rect.top, srcRect.top, srcRect.bottom, dstRect.top, dstRect.bottom),
                transform(rect.right, srcRect.left, srcRect.right, dstRect.left, dstRect.right),
                transform(rect.bottom, srcRect.top, srcRect.bottom, dstRect.top, dstRect.bottom));
    }

    /**
     * Overwrite masked bits in a pattern with "value"
     * @param pattern bit pattern
     * @param mask bits to be overwritten
     * @param value value of the new bits
     * @return updated pattern
     */
    public static int overwriteBits(int pattern, int mask, boolean value) {
        return value ? (pattern | mask) : (pattern & ~mask); // turn on: OR; turn off: AND NOT
    }

    /**
     * Check if patterns has flag set.
     * @param pattern bit pattern
     * @param flag bit flag
     * @return true if flag is contained in pattern, false if it is not
     */
    public static boolean check(int pattern, int flag) { return (pattern & flag) == flag; }
}
