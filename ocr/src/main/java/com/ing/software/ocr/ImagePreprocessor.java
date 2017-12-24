package com.ing.software.ocr;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
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

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;
import static com.annimon.stream.Stream.*;
import static java.lang.Math.*;

/**
 * Class used to process an image of a ticket.
 * <p> This class is thread safe. </p>
 *
 * <p> USAGE CASES: </p>
 *
 * <p> Real time visual feedback when framing a ticket: </p>
 * <ol> Create new ImagePreprocessor passing a the bitmap of the preview frame.</ol>
 * <ol> Call findTicket(true) </ol>
 * <ol> If findTicket callback has an empty error list, call getCorners() to get the rectangle corners
 *       to overlay on top of the preview. </ol>
 * <ol> If the callback list contains CROOKED_TICKET, alert user that the ticket is framed sideways </ol>
 *
 * <p> User shoots a photo: </p>
 * <ol> Use ImageProcessor instance of last frame when rectangle was found. </ol>
 * <ol> Call findTicket(false); </ol>
 * <ol> If findTicket callback has an empty error list, proceed to step 6) </ol>
 * <ol> If the callback list contains RECT_NOT_FOUND, let user drag the rectangle corners into position,
 *     then proceed to call setCorners(). </ol>
 * <ol> If the callback list contains CROOKED_TICKET, let user drag the rectangle corners into position,
 *     then proceed to call setCorners(). </ol>
 *
 * <ol> Call OcrManager.getTicket passing this ImagePreprocessor instance.
 *       Call undistort() to get the photo of the ticket unwarped. </ol>
 *
 * <p> New photo imported from storage: </p>
 * <ol> Same as when user shot a photo, but ImagePreprocessor must be created with imported photo. </ol>
 *
 * <p> Load and show photo already processed: </p>
 * <ol> Create new ImagePreprocessor passing the photo and the corners. </ol>
 * <ol> Call undistort().</ol>
 *
 * @author Riccardo Zaglia
 */
/*
 * I used only pivate-static and public methods to avoid side effects that increase complexity.
 * I'm sticking to the one-purpose-method rule.
 */
public class ImagePreprocessor {

    // length of smallest side of downscaled image
    // SHORT_SIDE must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final int SHORT_SIDE = 720;

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
    private static final int BORD_THICK = 2;

    //Adaptive threshold:
    private static final int THR_WIN_SZ = 75; // window size. must be odd
    private static final int THR_OFFSET = 1; //

    //Hough lines
    private static final int SECTORS = 101; // accumulator resolution, should be odd
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

    // Score values
    private static final double SCORE_AREA_MUL = 0.001;
    private static final double SCORE_RECT_FOUND = 1;


    static {
        if (OpenCVLoader.initDebug()) {
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
     * Convert a list of Android points to a list of OpenCV points
     * @param points List of Android points
     * @return List of OpenCV points
     */
    private static List<Point> androidPtsToCV(List<PointF> points) {
        return Stream.of(points).map(p -> new Point(p.x, p.y)).toList();
    }

    /**
     * Convert a list of Android points to a list of OpenCV points
     * @param points List of OpenCV points
     * @return List of Android points
     */
    private static List<PointF> cvPtsToAndroid(List<Point> points) {
        return Stream.of(points).map(p -> new PointF((int)p.x, (int)p.y)).toList();
    }

    /**
     * Convert a list of OpenCV points to MatOfPoints2f
     * @param pts List of points
     * @return MatOfPoints2f
     */
    @NonNull
    private static MatOfPoint2f ptsToMat(List<Point> pts) {
        return new MatOfPoint2f(pts.toArray(new Point[pts.size()]));
    }

    /**
     * Convert a Mat to a Bitmap
     * @param img Mat of any color. Not null.
     * @return Bitmap
     */
    private static Bitmap matToBitmap(Mat img) {
        Bitmap bm = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bm);
        return bm;
    }

    /**
     * Downscale image.
     * This method ensures that any image at any resolution or orientation is processed at the same level of detail.
     * @param img RGBA Mat. Not null.
     * @return RGBA Mat.
     */
    private static Mat downScaleRgba(Mat img) {
        float aspectRatio = (float)img.rows() / img.cols();
        Size size = aspectRatio > 1 ? new Size(SHORT_SIDE, (int)(SHORT_SIDE * aspectRatio))
                : new Size((int)(SHORT_SIDE / aspectRatio), SHORT_SIDE);
        Mat bgrResized = new Mat(size, CV_8UC4);
        resize(img, bgrResized, size);
        return bgrResized;
    }

    /**
     * Convert Mat from RGBA to gray
     * @param graySwap in-out swap of Mat (in: RGBA, out: gray). Not null.
     */
    private static void rgba2Gray(Swap<Mat> graySwap, Mat rgba) {
        cvtColor(rgba, graySwap.first, COLOR_RGBA2GRAY);
    }

    /**
     * Bilateral filter
     * @param imgSwap in-out swap of gray Mat. Not null.
     */
    private static void bilateralFilter(Swap<Mat> imgSwap) {
        Imgproc.bilateralFilter(imgSwap.first, imgSwap.swap(), BF_KER_SZ, BF_SIGMA, BF_SIGMA);
    }

    /**
     * Transform a gray image into a mask using an adaptive threshold
     * @param imgSwap in-out swap of Mat (in: gray, out: black & white). Not null.
     */
    private static void threshold(Swap<Mat> imgSwap) {
        adaptiveThreshold(imgSwap.first, imgSwap.swap(), 255,
                ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, THR_WIN_SZ, THR_OFFSET);
    }

    /**
     * Erode a mask.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param iters number of iterations
     */
    private static void erode(Swap<Mat> imgSwap, int iters) {
        Imgproc.erode(imgSwap.first, imgSwap.swap(), new Mat(), new Point(-1, -1), iters);
    }

    /**
     * Dilate a mask.
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
        copyMakeBorder(imgSwap.first, imgSwap.swap(),
                BORD_THICK, BORD_THICK, BORD_THICK, BORD_THICK, BORDER_CONSTANT);
    }

    /**
     * Downscale + convert to gray + bilateral filter + adaptive threshold + enclose, in this order.
     * @param imgSwap Swap of RGBA Mat. Not null.
     * @return B&W Mat.
     */
    private static void prepareBinaryImg(Swap<Mat> imgSwap, Mat rgba) {
        // I used Ref parameters to enable me to easily reorder the methods
        // and experiment with the image processing pipeline
        rgba2Gray(imgSwap, rgba);
        bilateralFilter(imgSwap);
        threshold(imgSwap);
        enclose(imgSwap);
    }

    /**
     * Find k biggest outer contours in a RGBA image, sorted by area (descending).
     * @param imgSwap Swap of RGBA Mat. Not null.
     * @param k number of contours to return.
     * @return Contour with biggest area. Never null.
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
     * Find all edges of thresholded image.
     * @param imgSwap Swap of B&W Mat. Not null.
     * @return Mat with white edges and black background.
     */
    private static void toEdges(Swap<Mat> imgSwap) {
        Imgproc.erode(imgSwap.first, imgSwap.swap(), ERODE_KER);
        Canny(imgSwap.first, imgSwap.swap(), 1, 1);
    }

    /**
     * Set to white everything outside contour.
     * @param imgSwap Swap of B&W Mat. Not null.
     * @param contour MatOfPoint containing a contour. Not null.
     * @return Mat with white edges and black background.
     */
    private static void removeBackground(Swap<Mat> imgSwap, MatOfPoint contour) {
        Mat binary = imgSwap.first.clone();
        imgSwap.first.setTo(new Scalar(0));
        fillPoly(imgSwap.first, Collections.singletonList(contour), new Scalar(255));
        erode(imgSwap, ERODE_KER_DATA[0].length + 1);
        bitwise_and(binary, imgSwap.first, imgSwap.swap());
    }

    /**
     * Find Hough lines (segments).
     * @param imgSwap Swap of B&W Mat. Not null.
     * @return contour.
     */
    private static MatOfInt4 houghLines(Swap<Mat> imgSwap) {
        MatOfInt4 lines = new MatOfInt4();
        HoughLinesP(imgSwap.first, lines, DIST_RES, ANGLE_RES, HOUGH_THRESH, HOUGH_MIN_LEN, HOUGH_MAX_GAP);
        return lines;
    }

    /**
     * Get size of a rectangle in perspective.
     * @param perspRect ordered corners of a rectangle in perspective. Not null.
     * @return Size in pixels proportional to the real ticket
     */
    @NonNull
    private static Size getRectSizeSimple(List<Point> perspRect) {
        double width = 1, height = 1; // non 0 initial values (should be overwritten)
        if (perspRect.size() == 4) {
            List<MatOfPoint> pts = Stream.of(perspRect).map(MatOfPoint::new).toList();
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
     * Find predominant angle of Hough lines using an accumulator
     * @param lines MatOfInt4 containing the hough lines. Not null.
     * @return predominant angle in degrees.
     */
    private static double predominantAngle(MatOfInt4 lines) {
        double[] accumulator = new double[SECTORS];
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            double xDiff = line[0] - line[2], yDiff = line[1] - line[3];
            double angle = atan(yDiff / xDiff);
            double length = sqrt(xDiff * xDiff + yDiff * yDiff);
            accumulator[min((int)((angle + PI / 2) * SECTORS / PI), SECTORS - 1)] += length;
        }

        return ((double)Collections.max(range(0, SECTORS).map(i ->
                new Scored<>(accumulator[i], i)).toList()).obj + 0.5) * 180 / SECTORS - 90;
    }

    /**
     * Order rectangle corners as the first is the top-leftmost.
     * @param srcRect MatOfPoint2f containing rect corners
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
        int topLeftIdx = Collections.min(Stream.of(newRect.toList()).indexed().map(ip ->
                new Scored<>(ip.getSecond().x + ip.getSecond().y, ip.getFirst())).toList()).obj;

        //shift verts by topLeftIdx
        //NB: sublist creates a view, not a copy.
        List<Point> newVerts = new ArrayList<>(srcRect.toList()); // Mat.toList() is immutable
        newVerts.addAll(newVerts.subList(0, topLeftIdx));
        newVerts.subList(0, topLeftIdx).clear();
        return ptsToMat(newVerts);
    }

    /**
     * Find the the bounding box of the a contour rotated by "angle"
     * @param ctr contour. Not modified.
     * @param angle to rotate the contour.
     * @param imgSize image size to get the center of image.
     * @return rotated bounding box
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
     */
    private static void scale(Mat points, Size srcSize, Size dstSize) {
        multiply(points, new Scalar(srcSize.width / dstSize.width,
                srcSize.height / dstSize.height), points);
    }

//    private static Mat rotateMat(Mat rgba, double angle) {
//        Mat m = getRotationMatrix2D(new Point(rgba.cols() / 2, rgba.rows() / 2), angle, 1);
//        Mat img = new Mat(rgba.size(), CV_8UC4);
//        warpAffine(rgba, img, m, rgba.size());
//        return img;
//    }


    //INSTANCE FIELDS:

    private Mat srcImg;
    private List<Point> corners;


    //PUBLIC:

    /**
     * You need to call setBitmap()
     */
    public ImagePreprocessor() {}

    /**
     * No need to call setBitmap().
     * @param bm ticket bitmap. Not null.
     */
    public ImagePreprocessor(@NonNull Bitmap bm) {
        setImage(bm);
    }

    /**
     * Set content of internal image buffers.
     * Always call this method before any other image manipulation method.
     * @param bm ticket bitmap. Not null.
     */
    public void setImage(@NonNull Bitmap bm) {
        srcImg = new Mat();
        bitmapToMat(bm, srcImg);
    }

    /**
     * Find the 4 corners of a ticket, ordered counter-clockwise from the top-left corner of the ticket.
     * The corners are ordered to get a straight ticket (but could be upside down).
     * @param quick <ul> true: faster but more inaccurate, good for real time visual feedback.
     *                                                     No orientation detection. </ul>
     *              <ul> false: slower but more accurate, good for recalculating the rectangle after the shot
     *                                                    or for analyzing an imported image. </ul>
     * @param callback callback with a list of TicketError argument, called when computation is finished
     *                 or an error ha occurred. The list parameter can contain.
     *                 <ul> RECT_NOT_FOUND: the rectangle is not found; </ul>
     *                 <ul> CROOKED_TICKET: The ticket is framed sideways; </ul>
     *                 <ul> INVALID_STATE: the bitmap image has not been set for this I.P. instance </ul>
     *                 <p> if there are no errors, the rectangle is found and the corners
     *                      can be obtained with getCorners(); </p>
     */
    public void findTicket(boolean quick, @NonNull Consumer<List<TicketError>> callback) {
        new Thread(() -> {
            synchronized (this) { // make sure this code is not executed concurrently when findTicket
                                  // is called more than once consecutively
                if (srcImg == null) {
                    callback.accept(Collections.singletonList(TicketError.INVALID_STATE));
                    return;
                }
                Mat resized = downScaleRgba(srcImg);
                Swap<Mat> graySwap = new Swap<>(() -> new Mat(resized.size(), CV_8UC1));
                prepareBinaryImg(graySwap, resized);
                Mat binary = graySwap.first.clone();
                toEdges(graySwap);
                Mat edges = graySwap.first.clone();
                graySwap.first = binary; // not cloning the image, it will be overwritten by using the swap

                List<Scored<Pair<MatOfPoint2f, Double>>> candidates = new ArrayList<>();
                for (Scored<MatOfPoint> ctrPair :
                        findBiggestContours(graySwap, quick ? 1 : MAX_CONTOURS)) {
                    double angle = 0;

                    MatOfPoint2f rect = findPolySimple(ctrPair.obj);
                    if (!quick) {
                        // find Hough lines of ticket text
                        graySwap.first = edges.clone();
                        removeBackground(graySwap, ctrPair.obj);
                        angle = predominantAngle(houghLines(graySwap));

                        if (rect.rows() == 4)// fix orientation of already found rectangle
                            rect = orderRectCorners(rect, angle, resized.size());
                        else { // find rotated bounding box of contour
                            rect = rotatedBoundingBox(ctrPair.obj, angle, resized.size());
                        }
                        //todo use RANSAC to find undistort transform matrix
                    }
                    candidates.add(new Scored<>(0.0, new Pair<>(rect, angle)));
                }
                //todo assign score and use Collections.max
                Pair<MatOfPoint2f, Double> winner = candidates.get(0).obj;

                //scale up the the corners to match the scale of the original image
                scale(winner.first, srcImg.size(), resized.size());

                corners = winner.first.toList();

                List<TicketError> errors = new ArrayList<>();
                if (corners.size() != 4)
                    errors.add(TicketError.RECT_NOT_FOUND);
                if (winner.second < -60 || winner.second > 60)
                    errors.add(TicketError.CROOKED_TICKET);

                // As long as I use a new thread in only this method, calling this method or any other method
                // of this class instance inside the callback, is not gonna create a double lock condition.
                callback.accept(errors);
            }
        }).start();
    }

    /**
     * Set rectangle corners.
     * @param corners must be 4, ordered counter-clockwise, first is top-left of ticket. Not null.
     * @return TicketError. - NONE: corners are valid.
     *                      - INVALID_CORNERS: corners are != 4 or not ordered counter-clockwise.
     */
    public synchronized TicketError setCorners(@NonNull List<PointF> corners) {
        this.corners = androidPtsToCV(corners);
        //todo check if corners are ordered correctly
        return corners.size() == 4 ? TicketError.NONE : TicketError.INVALID_POINTS;
    }

    /**
     * Get rectangle corners.
     * @return List of corners in bitmap space (range from (0,0) to (width, height) ).
     *         The corners should always be 4. Never null.
     */
    public synchronized List<PointF> getCorners() {
        return corners == null ? new ArrayList<>() : cvPtsToAndroid(corners);
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

        Mat dstImg = srcImg.clone();
        if (corners.size() == 4) { // at this point "corners" should have always 4 points.
            Size sz = getRectSizeSimple(corners);
            double m = marginMul * min(sz.width, sz.height);

            MatOfPoint2f dstRect = new MatOfPoint2f( // counter-clockwise
                    new Point(m, m),
                    new Point(m, sz.height + m),
                    new Point(sz.width + m, sz.height + m),
                    new Point(sz.width + m, m));
            Mat mtx = getPerspectiveTransform(new MatOfPoint2f(corners.toArray(new Point[4])), dstRect);
            warpPerspective(srcImg, dstImg, mtx, new Size(sz.width + 2 * m, sz.height + 2 * m));
        }
        return matToBitmap(dstImg);
    }


    //UTILITY FUNCTIONS:

    /**
     * Rotate a bitmap
     * @param src Source bitmap. Not null.
     * @param angle Rotation angle (degrees)
     * @return Rotated bitmap
     */
    public static Bitmap rotate(@NonNull Bitmap src, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * Apply perspective transform to a collection of points
     * @param srcPoints Points to be transformed
     * @param srcRect Reference rectangle
     * @param dstRect Distorted reference rectangle
     * @return Output points. Empty if error.
     */
    public static List<PointF> transform(
            List<PointF> srcPoints,
            List<PointF> srcRect,
            List<PointF> dstRect) {
        if (srcRect.size() != 4 || dstRect.size() != 4)
            return new ArrayList<>();
        MatOfPoint2f pts = ptsToMat(androidPtsToCV(srcPoints));
        MatOfPoint2f rect1 = ptsToMat(androidPtsToCV(srcRect));
        MatOfPoint2f rect2 = ptsToMat(androidPtsToCV(dstRect));
        MatOfPoint2f dstPts = new MatOfPoint2f();
        perspectiveTransform(pts, dstPts, getPerspectiveTransform(rect1, rect2));
        return cvPtsToAndroid(dstPts.toList());
    }
}
