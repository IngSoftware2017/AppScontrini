package com.ing.software.ocr;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.annimon.stream.Stream;
import com.ing.software.common.*;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Size; // resolve conflict
import org.opencv.core.Point; // resolve conflict
import org.opencv.core.Rect; // resolve conflict
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.annimon.stream.function.*;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;
import static com.annimon.stream.Stream.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static com.ing.software.common.CommonUtils.*;

/**
 * Class used to process an image of a ticket.
 * <p> This class is thread safe. </p>
 *
 * <p> USAGE CASES: </p>
 *
 * <p> Real time visual feedback when framing a ticket: </p>
 * <ol> Create new ImageProcessor passing a the bitmap of the preview frame.</ol>
 * <ol> Call findTicket(true) </ol>
 * <ol> If findTicket callback has an empty error list, call getCorners() to get the rectangle corners
 *       to overlay on top of the preview. </ol>
 * <ol> If the callback list contains CROOKED_TICKET, alert user that the ticket is framed sideways </ol>
 *
 * <p> User shoots a photo: </p>
 * <ol> Use ImageProcessor instance of last frame when rectangle was found. </ol>
 * <ol> Call findTicket(false); </ol>
 * <ol> If findTicket callback has an empty error list, proceed to step 5) </ol>
 * <ol> If the callback list contains RECT_NOT_FOUND, let user drag the rectangle corners into position,
 *     then proceed to call setCorners(). </ol>
 * <ol> Call OcrManager.getTicket passing this ImageProcessor instance.
 *       Call undistort() to get the photo of the ticket unwarped. </ol>
 *
 * <p> New photo imported from storage: </p>
 * <ol> Same as when user shot a photo, but ImageProcessor must be created with imported photo. </ol>
 *
 * <p> Load and show photo already processed: </p>
 * <ol> Create new ImageProcessor passing the photo and the corners. </ol>
 * <ol> Call undistort().</ol>
 *
 * @author Riccardo Zaglia
 */
/*
 * I used only pivate-static and public methods to avoid side effects that increase complexity.
 * I'm sticking to the one-purpose-method rule.
 */
public class ImageProcessor {

    // length of smallest side of downscaled image
    // SHORT_SIDE must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final float SHORT_SIDE = 720;

    //Bilateral filter:
    private static final int BF_KER_SZ = 9; // kernel size, must be odd
    private static final int BF_SIGMA = 30; // space/color variance

    //Erode/Dilate iterations
    private static final int E_D_ITERS = 4;

    //Erode for hugh lines
    private static int[][] ERODE_KER_DATA = new int [][] {
            new int[] {   0,   0, 255, 255, 255, 255, 255,   0,   0},
            new int[] { 255, 225, 255, 255, 255, 255, 255, 255, 255},
            new int[] { 255, 225, 255, 255, 255, 255, 255, 255, 255},
            new int[] { 255, 225, 255, 255, 255, 255, 255, 255, 255},
            new int[] {   0,   0, 255, 255, 255, 255, 255,   0,   0},
    };
    private static Mat ERODE_KER; // assigned in the static block

    //Median kernel size
    private static final int MED_SZ = 7; // must be odd

    //Enclose border thickness
    private static final int MRG_THICK = 2;

    //Adaptive threshold:
    private static final int THR_WIN_SZ = 75; // window size. must be odd
    private static final int THR_OFFSET = 1; //

    //Accumulator for Hough lines
    private static final int SECTORS = 101; // accumulator resolution, should be odd
    private static final int MAX_SECTOR_DIST = SECTORS / 5;
    private static final double MAX_SCORE_SECTOR_OUTLIERS_MUL = 0.5;
    private static final int MIN_LINES = 5;

    //Hough lines
    private static final double DIST_RES = 1; // rho, resolution in hough space
    private static final double ANGLE_RES = PI / SECTORS; // theta, resolution in hough space
    private static final int HOUGH_THRESH = 50; // threshold
    private static final int HOUGH_MIN_LEN = 40;
    private static final int HOUGH_MAX_GAP = 5;

    // Maximum number of contours to analyze
    private static final int MAX_CONTOURS = 2;

    // factor that determines maximum distance of detected contour from rectangle
    //private static final double polyMaxErrMul = 0.02;
    private static final int POLY_MAX_ERR = 50;

    // margin for OCR analysis
    private static final double MARGIN_MUL_OCR = 0.05;

    // Score values
    private static final double SCORE_AREA_MUL = 0.001;
    private static final double SCORE_RECT_FOUND = 1;


    // Anything inside here is run once per app execution and before any other code.
    static {
        if (OpenCVLoader.initDebug()) {
            // assign ERODE_KER kernel from ERODE_KER_DATA.
            ERODE_KER = new Mat(ERODE_KER_DATA.length, ERODE_KER_DATA[0].length, CV_8UC1);
            for (int y = 0; y < ERODE_KER_DATA.length; y++) {
                for (int x = 0; x < ERODE_KER_DATA[0].length; x++) {
                    ERODE_KER.put(y, x, ERODE_KER_DATA[y][x]);
                }
            }
        }
        else {
            OcrUtils.log(0, "OpenCV", "OpenCV failed to initialize");
        }
    }

    /**
     * Convert a list of Android points to a list of OpenCV points.
     * @param points List of Android points.
     * @return List of OpenCV points.
     */
    private static List<Point> androidPtsToCV(List<PointF> points) {
        return Stream.of(points).map(p -> new Point(p.x, p.y)).toList();
    }

    /**
     * Convert a list of OpenCV points to a list of Android points.
     * @param points List of OpenCV points.
     * @return List of Android points.
     */
    private static List<PointF> cvPtsToAndroid(List<Point> points) {
        return Stream.of(points).map(p -> new PointF((int)p.x, (int)p.y)).toList();
    }

    /**
     * Convert a list of OpenCV points to MatOfPoints2f.
     * @param pts List of points
     * @return MatOfPoints2f
     */
    @NonNull
    private static MatOfPoint2f ptsToMat(List<Point> pts) {
        return new MatOfPoint2f(pts.toArray(new Point[pts.size()]));
    }

    /**
     * Convert a Mat to a Bitmap.
     * @param img Mat of any color format. Not null.
     * @return Bitmap
     */
    private static Bitmap matToBitmap(Mat img) {
        Bitmap bm = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        return bm;
    }

    /**
     * Downscale image.
     * This method ensures that any image at any resolution or orientation is processed at the same level of detail,
     * but it preserves aspect ratio.
     * @param img RGBA Mat. Not null.
     * @return RGBA Mat.
     */
    private static Mat downScaleRgba(Mat img) {
        float aspectRatio = (float)img.cols() / img.rows();
        Mat bgrResized = new Mat();
        resize(img, bgrResized, aspectRatio < 1 ? new Size(SHORT_SIDE, SHORT_SIDE / aspectRatio)
                : new Size(SHORT_SIDE * aspectRatio, SHORT_SIDE));
        return bgrResized;
    }

    /**
     * Convert Mat from RGBA to gray.
     * @param graySwap output swap of gray Mat. Not null.
     * @param rgba input RGBA Mat.
     */
    private static void rgba2Gray(Swap<Mat> graySwap, Mat rgba) {
        cvtColor(rgba, graySwap.first, COLOR_RGBA2GRAY);
    }

    /**
     * Bilateral filter.
     * @param imgSwap in-out swap of gray Mat. Not null.
     */
    private static void bilateralFilter(Swap<Mat> imgSwap) {
        Imgproc.bilateralFilter(imgSwap.first, imgSwap.swap(), BF_KER_SZ, BF_SIGMA, BF_SIGMA);
    }

    /**
     * Transform a gray image into a mask using an adaptive threshold.
     * @param imgSwap in-out swap of Mat (in: gray, out: black & white). Not null.
     */
    private static void threshold(Swap<Mat> imgSwap) {
        adaptiveThreshold(imgSwap.first, imgSwap.swap(), 255,
                ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, THR_WIN_SZ, THR_OFFSET);
    }

    /**
     * Erode a mask with a 3x3 kernel.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param iters number of iterations
     */
    private static void erode(Swap<Mat> imgSwap, int iters) {
        Imgproc.erode(imgSwap.first, imgSwap.swap(), new Mat(), new Point(-1, -1), iters);
    }

    /**
     * Dilate a mask with a 3x3 kernel.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param iters number of iterations
     */
    private static void dilate(Swap<Mat> imgSwap, int iters) {
        Imgproc.dilate(imgSwap.first, imgSwap.swap(), new Mat(), new Point(-1, -1), iters);
    }

    /**
     * Smooth mask contours.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     */
    // unused
    private static void median(Swap<Mat> imgSwap) {
        medianBlur(imgSwap.first, imgSwap.swap(), MED_SZ);
    }

    /**
     * Make sure that the mask is not touching image edges.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     */
    private static void enclose(Swap<Mat> imgSwap) {
        copyMakeBorder(imgSwap.first, imgSwap.swap(), MRG_THICK, MRG_THICK, MRG_THICK, MRG_THICK, BORDER_CONSTANT);
    }

    /**
     * Downscale + convert to gray + bilateral filter + adaptive threshold + enclose, in this order.
     * @param imgSwap output swap of gray Mat. Not null.
     * @param rgba input RGBA Mat.
     */
    private static void prepareBinaryImg(Swap<Mat> imgSwap, Mat rgba) {
        // I used swap in-out parameters to enable me to easily reorder the methods
        // and experiment with the image processing pipeline
        rgba2Gray(imgSwap, rgba);
        bilateralFilter(imgSwap);
        threshold(imgSwap);
        enclose(imgSwap);
    }

    /**
     * Find k biggest outer contours in a RGBA image, sorted by area (descending).
     * @param imgSwap Swap of gray Mat. This parameter is modified by the function. Not null.
     * @param k number of contours to return.
     * @return Contour-area pairs with biggest area. Never null.
     */
    private static List<Scored<MatOfPoint>> findBiggestContours(Swap<Mat> imgSwap, int k) {
        erode(imgSwap, E_D_ITERS);
        dilate(imgSwap, E_D_ITERS);
        //median(imgRef);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(imgSwap.first, contours, hierarchy, RETR_CCOMP, CHAIN_APPROX_SIMPLE);

        Podium<Scored<MatOfPoint>> podium = new Podium<>(k);
        for (int i = 0; i < hierarchy.cols(); i++) {
            // select outer contours, i.e. contours that have no parent (hierarchy-1)
            // to know more, go to the link below, look for "RETR_CCOMP":
            // https://docs.opencv.org/3.1.0/d9/d8b/tutorial_py_contours_hierarchy.html
            if (hierarchy.get(0, i)[3] == -1) {
                MatOfPoint ctr = contours.get(i);
                podium.tryAdd(new Scored<>(contourArea(ctr), ctr));
            }
        }
        return podium.getAll();
        //return Stream.of(podium.getAll()).map(cp -> cp.obj).toList();
    }

    /**
     * Find edges of thresholded image. Output: Mat with white edges and black background.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     */
    private static void toEdges(Swap<Mat> imgSwap) {
        Imgproc.erode(imgSwap.first, imgSwap.swap(), ERODE_KER);
        Canny(imgSwap.first, imgSwap.swap(), 1, 1);
    }

    /**
     * Set to white everything outside contour.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param contour MatOfPoint containing a contour. Not null.
     */
    private static void removeBackground(Swap<Mat> imgSwap, MatOfPoint contour) {
        Mat binary = imgSwap.first.clone();
        imgSwap.first.setTo(new Scalar(0));
        fillPoly(imgSwap.first, singletonList(contour), new Scalar(255));
        erode(imgSwap, ERODE_KER_DATA[0].length + 1);
        bitwise_and(binary, imgSwap.first, imgSwap.swap());
    }

    /**
     * Find Hough lines (segments).
     * @param imgSwap Swap of B&W Mat. Not modified by this function. Not null.
     * @return MatOfInt4 containing hough lines.
     */
    private static MatOfInt4 houghLines(Swap<Mat> imgSwap) {
        MatOfInt4 lines = new MatOfInt4();
        HoughLinesP(imgSwap.first, lines, DIST_RES, ANGLE_RES, HOUGH_THRESH, HOUGH_MIN_LEN, HOUGH_MAX_GAP);
        return lines;
    }

    /**
     * Get size of a rectangle in perspective.
     * @param perspRect MatOfPoints of a rectangle in perspective. Should contain exactly 4 rows. Not null.
     * @return Size in pixels proportional to the real ticket.
     */
    @NonNull
    private static Size rectSizeSimple(MatOfPoint2f perspRect) {
        double width = 1, height = 1; // non 0 initial values (should be overwritten)
        if (perspRect.rows() == 4) {
            List<Mat> pts =  range(0, 4).map(perspRect::row).toList();
            // The width is calculated summing the distance between the two upper and two lower corners
            // then dividing by two to get the average.
            // Similarly the height is calculated using the distance between the leftmost
            // and rightmost corners.
            width = (norm(pts.get(1), pts.get(2)) + norm(pts.get(3), pts.get(0))) / 2;
            height = (norm(pts.get(0), pts.get(1)) + norm(pts.get(2), pts.get(3))) / 2;
        }
        return new Size(width, height);
    }
    //todo: use a better approach:
    // http://andrewkay.name/blog/post/aspect-ratio-of-a-rectangle-in-perspective/

    /**
     * Approximate a contour into a polygon.
     * @param contour MatOfPoint containing a contour. Not null.
     * @return MatOfPoint2f polygon corners.
     */
    private static MatOfPoint2f findPolySimple(MatOfPoint contour) {
        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
        MatOfPoint2f verts = new MatOfPoint2f();
        //double perimeter = arcLength(contour, true);
        approxPolyDP(contour2f, verts, POLY_MAX_ERR/*polyMaxErrMul * perimeter*/, true);
        return verts;
    }

    /**
     * Find predominant angle of Hough lines using an accumulator.
     * @param lines MatOfInt4 containing the hough lines. Not null.
     * @return predominant angle in degrees [-90, 90].
     */
    //I do not calculate an average of the angles in the chosen sector,
    // so if the ticket is already upright, undistort() will not create unnecessary aliasing
    private static double predominantAngle(MatOfInt4 lines, Ref<Boolean> reliable) {
        double[] accumulator = new double[SECTORS];
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            double xDiff = line[0] - line[2], yDiff = line[1] - line[3];
            double length = sqrt(xDiff * xDiff + yDiff * yDiff);
            int sector = (int)((atan(yDiff / xDiff) + PI / 2.) * SECTORS / PI);
            accumulator[mod(sector, SECTORS)] += length;
            // to mitigate aliasing, contribute also to sector + 1 and sector - 1.
            accumulator[mod(sector + 1, SECTORS)] += length * 0.99;
            accumulator[mod(sector - 1, SECTORS)] += length * 0.99;
            //graphical algorithm explanation (these are sectors, the number is the score):
            // ... | 0 | 5 | 0 | ... | 0 | 2 | 3 | 0 | ...
            //          VVV                 VVV
            // ... | 5-| 5 | 5-| ... | 2-| 5-| 5-| 3-| ...
            //          win

            // ... | 0 | 5 | 0 | ... | 0 | 3 | 3 | 0 | ...
            //          VVV                 VVV
            // ... | 5-| 5 | 5-| ... | 3-| 6-| 6-| 3-| ...
            //                            win win
        }

        // 4 means: (1 -> best angle) + (2 -> adjacent to best) + (1 -> outlier to be found)
        Podium<Scored<Integer>> accPodium = new Podium<>(4);
        accPodium.tryAddAll(range(0, SECTORS).map(i -> new Scored<>(accumulator[i], i)).toList());
        List<Scored<Integer>> bestSects = accPodium.getAll();
        Scored<Integer> bestSect = bestSects.get(0);

        //angle is reliable if all outlier sectors are of score < best score * MAX_SCORE_SECTOR_OUTLIERS_MUL.
        // if there were <= MIN_LINES lines, angle is not reliable.
        reliable.value = lines.rows() > MIN_LINES && Stream.of(bestSects).reduce(true, (reliab, sect) -> {
            if (reliab && sect.getScore() > 0) {
                int dist = min(abs(sect.obj() - bestSect.obj()), abs(sect.obj() - bestSect.obj() + SECTORS));
                return/*reliable*/ !(dist > MAX_SECTOR_DIST // <- is outlier
                        && sect.getScore() > bestSect.getScore() * MAX_SCORE_SECTOR_OUTLIERS_MUL);
            }
            return/*reliable*/ false;
        });

        //if there are no lines, assume the ticket is upright.
        return lines.rows() > 0 ? ((double)bestSect.obj() + 0.5) * 180. / SECTORS - 90. : 0.;
    }

    /**
     * Shift the points of a MatOfPoint2f in order to make a selected point the new first point.
     * @param pts MatOfPoint2f
     * @param newFirstIdx selected index of the point to make first.
     * @return MatOfPoint2f with shifted points
     */
    @NonNull
    private static MatOfPoint2f shiftMatPoints(MatOfPoint2f pts, int newFirstIdx) {
        //NB: sublist creates a view, not a copy.
        List<Point> newVerts = new ArrayList<>(pts.toList()); // MatOfPoint2f.toList() is immutable
        Collections.rotate(newVerts, -newFirstIdx);
        return ptsToMat(newVerts);
    }

    /**
     * Order rectangle corners as the first is the top-leftmost.
     * @param srcRect MatOfPoint2f containing rect corners.
     * @param angle to rotate the corners before ordering.
     * @param imgSize image size to get the center of image.
     * @return ordered corners, not rotated.
     */
    @NonNull
    private static MatOfPoint2f orderRectCorners(MatOfPoint2f srcRect, double angle, Size imgSize) {
        // I should create the rotation matrix from the center of the contour,
        // but for now it's good enough
        Mat rotationMatrix = getRotationMatrix2D(
                new Point(imgSize.width / 2, imgSize.height / 2), angle, 1);
        MatOfPoint2f newRect = new MatOfPoint2f();
        Core.transform(srcRect, newRect, rotationMatrix);

        //find index of point closer to top-left corner of image (using taxicab distance).
        int topLeftIdx = min(Stream.of(newRect.toList()).mapIndexed((i, p) ->
                new Scored<>(p.x + p.y, i)).toList()).obj();

        //shift verts by topLeftIdx
        return shiftMatPoints(srcRect, topLeftIdx);
    }

    /**
     * Find the the bounding box of the a contour rotated by "angle".
     * @param ctr contour. Not modified.
     * @param angle to rotate the contour.
     * @param imgSize image size to get the center of image.
     * @return rotated bounding box.
     */
    private static MatOfPoint2f rotatedBoundingBox(MatOfPoint ctr, double angle, Size imgSize) {
        Point center = new Point(imgSize.width / 2, imgSize.height / 2);
        Mat rotationMatrix = getRotationMatrix2D(center, angle, 1);
        MatOfPoint newCtr = new MatOfPoint();
        Core.transform(ctr, newCtr, rotationMatrix);
        Rect box = boundingRect(newCtr);
        MatOfPoint2f newRect = new MatOfPoint2f(
                box.tl(),
                new Point(box.tl().x, box.br().y),
                box.br(),
                new Point(box.br().x, box.tl().y));
        Mat inverseRotation = getRotationMatrix2D(center, -angle, 1);
        Core.transform(newRect, newRect, inverseRotation);
        return newRect;
    }

    /**
     * Scale points from srcSize space to dstSize space
     * @param points in-out Mat of points to scale. Not null.
     * @param srcSize Size of source image. Not null.
     * @param dstSize Size of destination image. Not null.
     * @return scaled Mat of points.
     */
    private static MatOfPoint2f scale(MatOfPoint2f points, Size srcSize, Size dstSize) {
        MatOfPoint2f newPts = new MatOfPoint2f();
        multiply(points, new Scalar(dstSize.width / srcSize.width,
                dstSize.height / srcSize.height), newPts);
        return newPts;
    }

    /**
     * Apply a perspective straightening to a Mat. The returned Mat has the same level of detail of the input Mat.
     * @param img Mat of any color
     * @param corners MatOfPoint2f containing 4 normalized points ordered counter-clockwise
     * @param marginMul border multiplier
     * @return undistorted Mat
     */
    private static Mat undistort(Mat img, MatOfPoint2f corners, double marginMul) {
        Mat dstImg = new Mat();
        if (corners.rows() == 4) { // at this point "corners" should have always 4 points.
            MatOfPoint2f srcRect = scale(corners, new Size(1, 1), img.size());
            Size sz = rectSizeSimple(srcRect);
            double m = marginMul * min(sz.width, sz.height);

            MatOfPoint2f dstRect = new MatOfPoint2f( // counter-clockwise
                    new Point(m, m),
                    new Point(m, sz.height + m),
                    new Point(sz.width + m, sz.height + m),
                    new Point(sz.width + m, m));
            Mat mtx = getPerspectiveTransform(srcRect, dstRect);
            warpPerspective(img, dstImg, mtx, new Size(sz.width + 2 * m, sz.height + 2 * m));
        }
        return dstImg;
    }

    //INSTANCE FIELDS:

    private Mat srcImg;
    private Mat resized;
    private Mat undistorted;
    private MatOfPoint2f corners; // normalized
    private boolean quickCorners;


    //PACKAGE PRIVATE:

    /**
     * Undistort with a margin suitable for OCR.
     * @return bitmap
     */
    synchronized Bitmap undistortForOCR() {
        if (quickCorners || corners == null || resized == null)
            if (findTicket(false).contains(TicketError.INVALID_STATE))
                return null;

        undistorted = undistort(resized, corners, MARGIN_MUL_OCR);
        return matToBitmap(undistorted);
    }

    /**
     * Get a cropped version of the undistorted image, with "newAspectRatio".
     * The returned Bitmap size is always >= of region size.
     * @param region region of undistorted image to crop.
     *               Must be contained in the bitmap obtained with undistortForOCR()
     * @param newAspectRatio new Bitmap aspectRatio
     * @return new Bitmap.
     */
    synchronized Bitmap undistortedSubregion(RectF region, double newAspectRatio) {
        if (undistorted == null)
            if (undistortForOCR() == null)
                return null;
        double stretchMul = newAspectRatio / (region.width() / region.height());
        Size newSize = stretchMul > 1 ? new Size(region.width() * stretchMul, region.height())
                : new Size(region.width(), region.height() / stretchMul);

        Mat finalImg = new Mat();
        resize(undistorted.submat((int) region.top, (int) region.bottom,
                (int) region.left, (int) region.right), finalImg, newSize);
        return matToBitmap(finalImg);
    }


    //PUBLIC:

    /**
     * You need to call setBitmap()
     */
    public ImageProcessor() {}

    /**
     * No need to call setBitmap().
     * @param bm ticket bitmap. Not null.
     */
    public ImageProcessor(@NonNull Bitmap bm) {
        setImage(bm);
    }

    /**
     * Set content of internal image buffers.
     * Always call this method before any other image manipulation method.
     * @param bm ticket bitmap. Not null.
     */
    public synchronized void setImage(@NonNull Bitmap bm) {
        corners = null;
        resized = null;
        undistorted = null;
        srcImg = new Mat();
        Utils.bitmapToMat(bm, srcImg);
    }

    /**
     * Find the 4 corners of a ticket, ordered counter-clockwise from the top-left corner of the ticket.
     * The corners are ordered to get a straight ticket (but could be upside down).
     * @param quick <ul> true: faster but more inaccurate, good for real time visual feedback.
     *                                                     No orientation detection. </ul>
     *              <ul> false: slower but more accurate, good for recalculating the rectangle after the shot
     *                                                    or for analyzing an imported image. </ul>
     * @return list of TicketError, can contain:
     *         <ul> RECT_NOT_FOUND: the rectangle is not found; </ul>
     *         <ul> CROOKED_TICKET: The ticket is framed sideways; </ul>
     *         <ul> INVALID_STATE: the bitmap image has not been set for this I.P. instance </ul>
     *         <p> if there are no errors, the rectangle is found and the corners
     *             can be obtained with getCorners(); </p>
     */
    public synchronized List<TicketError> findTicket(boolean quick) {
        if (srcImg == null)
            return singletonList(TicketError.INVALID_STATE);
        undistorted = null;

        resized = downScaleRgba(srcImg);
        Swap<Mat> graySwap = new Swap<>(Mat::new);
        prepareBinaryImg(graySwap, resized);
        Mat binary = graySwap.first.clone();
        toEdges(graySwap);
        Mat edges = graySwap.first.clone();
        graySwap.first = binary; // not cloning the image, it will be overwritten by using the swap

        List<Scored<Pair<MatOfPoint2f, Double>>> candidates = new ArrayList<>();
        for (Scored<MatOfPoint> ctrPair : findBiggestContours(graySwap, quick ? 1 : MAX_CONTOURS)) {
            MatOfPoint2f rect = findPolySimple(ctrPair.obj());
            // find Hough lines of ticket text
            graySwap.first = edges.clone();
            removeBackground(graySwap, ctrPair.obj());
            Ref<Boolean> isAngleReliableRef = new Ref<>();
            double angle = predominantAngle(houghLines(graySwap), isAngleReliableRef);

            if (rect.rows() == 4)// fix orientation of already found rectangle
                rect = orderRectCorners(rect, angle, resized.size());
            else { // find rotated bounding box of contour
                rect = rotatedBoundingBox(ctrPair.obj(), angle, resized.size());
            }
            // if angle is not reliable, correct orientation as width < height (make ticket vertical).
            if (!isAngleReliableRef.value) {
                Size size = rectSizeSimple(rect);
                if (size.width > size.height)
                    rect = shiftMatPoints(rect, 3); //last idx -> first idx
            }
            //todo use RANSAC to find undistort transform matrix
            candidates.add(new Scored<>(0., new Pair<>(rect, angle)));
        }
        //todo assign score and use Collections.max
        Pair<MatOfPoint2f, Double> winner = candidates.get(0).obj();

        //normalize the corners in the space [0, 1]^2
        corners = scale(winner.first, resized.size(), new Size(1, 1));
        quickCorners = quick;

        List<TicketError> errors = new ArrayList<>();
        if (corners.rows() != 4)
            errors.add(TicketError.RECT_NOT_FOUND);
        if (abs(winner.second) > 60.)
            errors.add(TicketError.CROOKED_TICKET);
        return errors;
    }

    /**
     * Asynchronous version of findTicket(quick). The errors are passed by the callback parameter.
     * @param quick true: fast mode; false: slow mode.
     * @param callback Callback
     */
    public void findTicket(boolean quick, @NonNull Consumer<List<TicketError>> callback) {
        new Thread(() -> callback.accept(findTicket(quick))).start();
    }

    /**
     * Set rectangle corners.
     * @param corners must be 4, ordered counter-clockwise, first is top-left of ticket. Not null.
     * @return TicketError, can be:
     * <ul> NONE: corners are valid. </ul>
     * <ul> INVALID_CORNERS: corners are != 4 or not ordered counter-clockwise. </ul>
     */
    public synchronized TicketError setCorners(@NonNull List<PointF> corners) {
        if (corners.size() != 4)
            return TicketError.INVALID_POINTS;
        undistorted = null;

        this.corners = ptsToMat(androidPtsToCV(corners));
        quickCorners = false;
        //todo check if corners are ordered correctly
        return TicketError.NONE;
    }

    /**
     * Get rectangle corners.
     * @return List of corners in bitmap space (range from (0,0) to (width, height) ).
     *         The corners should always be 4. Empty if error. Never null.
     */
    public synchronized List<PointF> getCorners() {
        return corners == null ? new ArrayList<>() : cvPtsToAndroid(corners.toList());
    }

    /**
     * Get a Bitmap of a ticket with a perspective correction applied, with a margin.
     * @param marginMul Fraction of length of shortest side of the rectangle of the ticket.
     *                  A good value is 0.02.
     * @return Bitmap of ticket with perspective distortion removed. Null if error.
     */
    public synchronized Bitmap undistort(double marginMul) {
        if (corners == null || srcImg == null)
            return null;
        return matToBitmap(undistort(srcImg, corners, marginMul));
    }

    /**
     * Asynchronous version of undistort(marginMul). The bitmap is passed by the callback parameter.
     * @param marginMul Margin multiplier
     * @param callback Callback
     */
    public void undistort(double marginMul, @NonNull Consumer<Bitmap> callback) {
        new Thread(() -> callback.accept(undistort(marginMul))).start();
    }


    //UTILITY FUNCTIONS:

    /**
     * Rotate a bitmap.
     * @param src Source bitmap. Not null.
     * @param angle Rotation angle (degrees).
     * @return Rotated bitmap.
     */
    public static Bitmap rotate(@NonNull Bitmap src, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * Apply perspective transform to a collection of points.
     * @param points Points to be transformed.
     * @param srcRect Reference rectangle.
     * @param dstRect Distorted reference rectangle.
     * @return Output distorted points. Empty if error.
     */
    public static List<PointF> transform(
            List<PointF> points,
            List<PointF> srcRect,
            List<PointF> dstRect) {
        if (srcRect.size() != 4 || dstRect.size() != 4)
            return new ArrayList<>();
        MatOfPoint2f pts = ptsToMat(androidPtsToCV(points));
        MatOfPoint2f rect1 = ptsToMat(androidPtsToCV(srcRect));
        MatOfPoint2f rect2 = ptsToMat(androidPtsToCV(dstRect));
        MatOfPoint2f dstPts = new MatOfPoint2f();
        perspectiveTransform(pts, dstPts, getPerspectiveTransform(rect1, rect2));
        return cvPtsToAndroid(dstPts.toList());
    }
}
