package com.ing.software.ocr;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.util.SizeF;

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
import java.util.concurrent.atomic.DoubleAccumulator;

import com.annimon.stream.function.*;

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;
import static com.annimon.stream.Stream.*;
import static java.lang.Math.*;
import static java.util.Collections.*;
import static com.ing.software.common.CommonUtils.*;

/**
 * Class used to process an image of a ticket.
 * <p> This class behaves like a state machine, the behaviour of a method call depends on which methods
 * were called previously.</p>
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

    private static final int WHITE = 255;
    private static final int BLACK = 0;

    // length of smallest side of downscaled image
    // must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final double SHORT_SIDE = 720;

    //Bilateral filter:
    private static final int BF_KER_SZ = 9; // kernel size, must be odd
    private static final int BF_SIGMA = 30; // space/color variance

    //Opening iterations
    private static final int OPEN_ITERS = 5;

    //Closing iterations
    private static final int CLOSE_ITERS = 30;

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
    private static final double THR_OFFSET = 1; // offset the threshold. NB: OpenCV takes the ceiling

    //Accumulator for Hough lines
    private static final int SECTORS = 101; // accumulator resolution, should be odd
    private static final int MAX_SECTOR_DIST = SECTORS / 10; // max distance from best sector
    // that separate inliers from outliers
    private static final double MIN_CONFIDENCE = 0.7; // ratio of inlier lines score
    private static final int MIN_LINES = 5;
    // the best angle found is accepted if in the 2 / 10 of all sectors are concentrated half of all lines

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

    private static final double CROOCKED_THRESH = 60.;

    // margin for OCR analysis
    private static final double OCR_MARGIN_MUL = 0.05;

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
        return Stream.of(points).map(p -> new PointF((float)p.x, (float)p.y)).toList();
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

    private static MatOfPoint2f rectToPtsMat(Rect rect) {
        return new MatOfPoint2f(
                rect.tl(),
                new Point(rect.tl().x, rect.br().y),
                rect.br(),
                new Point(rect.br().x, rect.tl().y));
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

    private static Size calcScaledSize(Size inpSize, double shortSide) {
        double aspectRatio = inpSize.width / inpSize.height;
        // find the shortest dimension, set it to "shortSide" and consequently set the other dimension
        // to keep the original aspect ratio
        return aspectRatio < 1 ? new Size(shortSide, shortSide / aspectRatio)
                : new Size(shortSide * aspectRatio, shortSide);
    }

    /**
     * Convert an image to grayscale and downscale it.
     * This method ensures that any image at any resolution or orientation is processed at the same level of detail,
     * but it preserves aspect ratio.
     * @param img RGBA Mat. Not null.
     * @return gray Mat.
     */
    private static Mat toGrayResized(Mat img, double shortSide) {
        Mat gray = new Mat(), grayResized = new Mat();
        cvtColor(img, gray, COLOR_RGBA2GRAY);
        resize(gray, grayResized, calcScaledSize(img.size(), shortSide));
        return grayResized;
    }

    /**
     * Erode a mask with a 3x3 kernel.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param iters number of iterations.
     */
    private static void erode(Swap<Mat> imgSwap, int iters) {
        Imgproc.erode(imgSwap.first, imgSwap.swap(), new Mat(), new Point(-1, -1), iters);
    }

    /**
     * Dilate a mask with a 3x3 kernel.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param iters number of iterations.
     */
    private static void dilate(Swap<Mat> imgSwap, int iters) {
        Imgproc.dilate(imgSwap.first, imgSwap.swap(), new Mat(), new Point(-1, -1), iters);
    }

    /**
     * Erode then dilate a mask with a 3x3 kernel.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param iters number of iterations.
     */
    private static void opening(Swap<Mat> imgSwap, int iters) {
        erode(imgSwap, iters);
        dilate(imgSwap, iters);
    }

    /**
     * Bilateral filter + adaptive threshold + enclose, in this order.
     * @param imgSwap in-out swap of gray Mat. Input-output image is first of swap. Not null.
     * @return output image (== new first of swap).
     */
    private static Mat prepareBinaryImg(Swap<Mat> imgSwap) {
        bilateralFilter(imgSwap.first, imgSwap.swap(), BF_KER_SZ, BF_SIGMA, BF_SIGMA);
        adaptiveThreshold(imgSwap.first, imgSwap.swap(), WHITE,
                ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, THR_WIN_SZ, THR_OFFSET);
//        medianBlur(imgSwap.first, imgSwap.swap(), MED_SZ);
        copyMakeBorder(imgSwap.first, imgSwap.swap(), MRG_THICK, MRG_THICK, MRG_THICK, MRG_THICK, BORDER_CONSTANT);
        return imgSwap.first;
    }

    /**
     * Find k biggest outer contours in a RGBA image, sorted by area (descending).
     * @param imgSwap Swap of gray Mat. This parameter is modified by the function. Not null.
     * @param k number of contours to return.
     * @return Contour-area pairs with biggest area. Never null.
     */
    private static List<Scored<MatOfPoint>> findBiggestContours(Swap<Mat> imgSwap, int k) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        //NB: findContours mangles the image!
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
     * @param imgSwap in-out swap of B&W Mat. Input-output image is first of swap. Not null.
     * @return output image (== new first of swap).
     */
    private static Mat toEdges(Swap<Mat> imgSwap) {
        Imgproc.erode(imgSwap.first, imgSwap.swap(), ERODE_KER);
        Canny(imgSwap.first, imgSwap.swap(), 1, 1); // 1: threshold, any number above 0
        return imgSwap.first;
    }

    /**
     * Get a mask from a contour
     * @param imgSwap out swap of B&W Mat. Not null.
     * @param contour MatOfPoint containing a contour. Not null.
     */
    private static void maskFromContour(Swap<Mat> imgSwap, MatOfPoint contour) {
        imgSwap.first.setTo(new Scalar(BLACK));
        fillPoly(imgSwap.first, singletonList(contour), new Scalar(WHITE));
    }

    /**
     * Set to white everything outside contour.
     * @param imgSwap in-out swap of B&W Mat. Not null.
     * @param contour MatOfPoint containing a contour. Not null.
     */
    private static void removeBackground(Swap<Mat> imgSwap, MatOfPoint contour) {
        Mat binary = imgSwap.first.clone();
        maskFromContour(imgSwap, contour);
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
    // I use this method instead of a simple LSD (least square distance) of the hough lines angle because
    // often some perpendicular lines or other outliers are detected and must be rejected.
    //I do not calculate an average of the angles in the chosen sector,
    // so if the ticket is already upright, undistort() will not create unnecessary aliasing
    // todo: try average anyway
    private static double predominantAngle(MatOfInt4 lines, Ref<Double> confidence) {
        double[] accumulator = new double[SECTORS]; // == array of sectors
        double totalScore = 0;
        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            double xDiff = line[0] - line[2], yDiff = line[1] - line[3];
            double length = sqrt(xDiff * xDiff + yDiff * yDiff);
            int sector = (int)((atan(yDiff / xDiff) + PI / 2.) * SECTORS / PI);
            accumulator[mod(sector, SECTORS)] += length;
            totalScore += (1. + 0.99 + 0.99) * length;
            // to mitigate aliasing, contribute also to sector + 1 and sector - 1.
            accumulator[mod(sector + 1, SECTORS)] += length * 0.99;
            accumulator[mod(sector - 1, SECTORS)] += length * 0.99;
            //graphical explanation of why contributing to adjacent sectors
            // (bars are sector delimiters, the number is the sector score):
            // ... | 0 | 5 | 0 | ... | 0 | 2 | 3 | 0 | ...
            //          VVV                 VVV
            // ... | 5-| 5 | 5-| ... | 2-| 5-| 5-| 3-| ...
            //          win

            // ... | 0 | 5 | 0 | ... | 0 | 3 | 3 | 0 | ...
            //          VVV                 VVV
            // ... | 5-| 5 | 5-| ... | 3-| 6-| 6-| 3-| ...
            //                            win win
        }

        //find best sector:
        int bestSect = max(range(0, SECTORS).map(i -> new Scored<>(accumulator[i], i)).toList()).obj();

        //inliers are the sectors with distance fom bestSect within MAX_SECTOR_DIST.
        double inlierScore = range(0, SECTORS).reduce(0., (score, sect) -> {
            // for the angle distance, I take into account the angle wrap-around.
            // (the angle between lines of angle +89deg and -89deg is 2deg)
            int dist = min(abs(sect - bestSect), SECTORS - abs(sect - bestSect));
            return dist < MAX_SECTOR_DIST ? score + accumulator[sect] : score;
        });
        //confidence is the ratio of inliers. accepted only if the number of hough lines is > MIN_LINES.
        confidence.val = (lines.rows() > MIN_LINES ? inlierScore / totalScore : 0);

        //if there are no lines, assume the ticket is upright.
        return lines.rows() > 0 ? ((double)bestSect + 0.5) * 180. / SECTORS - 90. : 0.;
    }

    /**
     * Shift the points of a MatOfPoint2f in order to make a selected point the new first point.
     * @param pts MatOfPoint2f
     * @param newFirstIdx selected index of the point to make first.
     * @return MatOfPoint2f with shifted points
     */
    private static MatOfPoint2f shiftMatPoints(MatOfPoint2f pts, int newFirstIdx) {
        List<Point> newVerts = new ArrayList<>(pts.toList()); // MatOfPoint2f.toList() is immutable
        Collections.rotate(newVerts, -newFirstIdx);
        return ptsToMat(newVerts);
    }

    private static int getTopLeftCornerIdx(MatOfPoint2f corners) {
        //find index of point closer to top-left corner of image (using taxicab distance).
        return min(Stream.of(corners.toList())
                .mapIndexed((i, p) -> new Scored<>(p.x + p.y, i)).toList()).obj();
    }

    /**
     * Order rectangle corners as the first is the top-leftmost.
     * @param srcRect MatOfPoint2f containing rect corners.
     * @param angle to rotate the corners before ordering.
     * @param imgSize image size to get the center of image.
     * @return ordered corners, not rotated.
     */
    private static MatOfPoint2f orderRectCorners(MatOfPoint2f srcRect, double angle, Size imgSize) {
        // I should create the rotation matrix from the center of the contour,
        // but for now it's good enough
        Mat rotationMatrix = getRotationMatrix2D(
                new Point(imgSize.width / 2, imgSize.height / 2), angle, 1);
        MatOfPoint2f newRect = new MatOfPoint2f();
        Core.transform(srcRect, newRect, rotationMatrix);
        int topLeftIdx = getTopLeftCornerIdx(newRect);

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
        MatOfPoint2f newRect = rectToPtsMat(box);
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
     * Get a score proportional to exposure
     * @param img gray Mat. Not null
     * @param contour contour containing the ticket
     * @return
     */
    //Check if average of area inside contour is above e.g. 200, if not: UNDEREXPOSED
    // then check if the the darker shades (text) are well spread
    // (can use ratio between area of convex hull of contour and area of convex hull of text), if not: OVEREXPOSED
    private static double getExposure(Mat img, MatOfPoint contour) {
        return 0; // stub
    }

    /**
     * Get a score proportional to focus
     * @param img gray Mat. Not null
     * @param contour contour containing the ticket
     * @return
     */
    // Use a simple edge detector (try Canny) tuned to detect sharp edges.
    // if there are too few edges, the image is out of focus.
    // return (number of edge pixels)^2 / contour area
    // use ^2 because edges are one dimensional, but we want to compare to an area (2 dimensional)
    private static double getFocus(Mat img, MatOfPoint contour, double area) {
        return 1; // stub
    }

    /**
     * Get a score proportional to contrast from background
     * @param rect perspective rectangle containing the ticket
     * @param contour contour containing the ticket
     * @return
     */
    //Problem: Sometimes, if the contrast of the ticket with background is poor, the contour bleeds
    // into the background. Sometimes if the text is too close to the edge of the ticked, a carving
    // happens instead. So, to detect the first case and reject the second, I can use a convex hull
    // on the contour, then find the ratio between the area of the convex hull with the bounding rectangle one.
    // if the ratio is too low (ex: 0.7/1.0) then communicate bad contrast.
    private static double getBackgroundCoontrast(MatOfPoint2f rect, MatOfPoint contour) {
        return 1; // stub
    }

    /**
     * Apply dilation followed by erosion on a contour to remove creeks.
     * Not to be confused with closing a curve into a contour.
     * @param contour input contour. Not null.
     * @param imgSwap in swap of B&W Mat. Modified by this function. Not null.
     * @return new contour or null if no contour found.
     */
    private static Scored<MatOfPoint> contourClosing(MatOfPoint contour, Swap<Mat> imgSwap, int iters) {
        maskFromContour(imgSwap, contour);
        //closing
        dilate(imgSwap, iters);
        // I have to force erode to consider the outside of the image all black.
        medianBlur(imgSwap.first, imgSwap.swap(), MED_SZ);
        Imgproc.erode(imgSwap.first, imgSwap.swap(), new Mat(), new Point(-1, -1), iters,
                BORDER_CONSTANT, new Scalar(BLACK));
//        erode(imgSwap, iters);
        List<Scored<MatOfPoint>> contours = findBiggestContours(imgSwap, 1);
        // no contours found if image side is < than 2 * iters
        return contours.size() > 0 ? contours.get(0) : null;
    }

    /**
     * Create a rectangle (4 corners inside a MatOfPoint2f) from size and margin
     * @param size rectangle size
     * @param margin rectangle margin
     * @return MatOfPoint2f containing the rectangle
     */
    private static MatOfPoint2f createRectMatWithMargin(Size size, double margin) {
        return new MatOfPoint2f( // counter-clockwise
                new Point(margin, margin),
                new Point(margin, size.height + margin),
                new Point(size.width + margin, size.height + margin),
                new Point(size.width + margin, margin));
    }

    /**
     * Apply a perspective straightening to a Mat. The returned Mat has the same level of detail of the input Mat.
     * @param srcImg Mat of any color
     * @param corners MatOfPoint2f containing 4 normalized points ordered counter-clockwise
     * @param marginMul border multiplier
     * @param sizeMulOrShortSide shortest side length or multiplier for default bitmap size
     * @param isSizeMul true if scaleOrShortSide is size scale, short side length otherwise.
     * @return undistorted Mat
     */
    private static Mat undistort(
            Mat srcImg, MatOfPoint2f corners, double marginMul, double sizeMulOrShortSide, boolean isSizeMul) {
        Mat dstImg = new Mat();
        if (corners.rows() == 4) { // at this point "corners" should have always 4 points.
            MatOfPoint2f srcRect = scale(corners, new Size(1, 1), srcImg.size());

            // dstRect has approximately the same size as srcRect, but is aligned with the axes and translated by margin
            Size dstSize = rectSizeSimple(srcRect);
            double mrg = marginMul * min(dstSize.width, dstSize.height);
            MatOfPoint2f dstRect = createRectMatWithMargin(dstSize, mrg);

            // find the output bitmap size and scale it if requested
            Size dstSizeWithMargin = new Size(dstSize.width + 2 * mrg,
                    dstSize.height + 2 * mrg);
            Size resizedDstSize;
            if (isSizeMul) {
                resizedDstSize = new Size(dstSizeWithMargin.width * sizeMulOrShortSide,
                        dstSizeWithMargin.height * sizeMulOrShortSide);
            } else {
                resizedDstSize = sizeMulOrShortSide > 0
                        ? calcScaledSize(dstSizeWithMargin, sizeMulOrShortSide) : dstSizeWithMargin;
            }
            MatOfPoint2f resizedDstRect = scale(dstRect, dstSizeWithMargin, resizedDstSize);

            Mat mtx = getPerspectiveTransform(srcRect, resizedDstRect);
            // apply the image perspective correction
            warpPerspective(srcImg, dstImg, mtx, resizedDstSize);
        }
        return dstImg;
    }

    //INSTANCE FIELDS:

    private Mat srcImg;
    private MatOfPoint2f corners; // normalized in [0, 1]^2 space
    private boolean quickCorners;


    //PACKAGE PRIVATE:

    synchronized Bitmap undistortForOCR(double sizeMul) {
        if (quickCorners || corners == null) {
            if (findTicket(false).contains(IPError.IMAGE_NOT_SET))
                return null;
        }
        return matToBitmap(undistort(srcImg, corners, OCR_MARGIN_MUL, sizeMul, true));
    }

    /**
     * Get a cropped version of the undistorted image, with "newAspectRatio".
     * The returned Bitmap size is always >= of region size.
     * @param undistorted undistorted bitmap size on which has been extracted region.
     * @param region region of undistorted bitmap to crop. Undistorted size space
     * @param newAspectRatio new aspectRatio of the returned bitmap
     * @return new Bitmap, null if error.
     */
    // to obtain the higher resolution possible, I have to map the region to the original bitmap space,
    // then I use warpPerspective. I need to backtrack and then re-execute undistort steps.
    synchronized Bitmap undistortedSubregion(SizeF undistorted, RectF region, double newAspectRatio) {
        if (quickCorners || corners == null)
            return null; // region and aspect ratio make sense only if calculated from
        // the bitmap obtained with undistort(), which calculates the corners.
        // convert android structures to OpenCV. "resized" means these are resized compared to the original bitmap.
        Size resizedUndistorted = new Size(undistorted.getWidth(), undistorted.getHeight());
        MatOfPoint2f resizedRegVerts = new MatOfPoint2f(
                new Point(region.left, region.top),
                new Point(region.left, region.bottom),
                new Point(region.right, region.bottom),
                new Point(region.right, region.top)
        );

        // redo undistort steps to calculate dstRect and dstSize
        MatOfPoint2f srcRect = scale(corners, new Size(1, 1), srcImg.size());
        Size dstSize = rectSizeSimple(srcRect);
        double mrg = OCR_MARGIN_MUL * min(dstSize.width, dstSize.height);
        MatOfPoint2f dstRect = createRectMatWithMargin(dstSize, mrg);
        Size dstSizeWithMargin = new Size(dstSize.width + 2 * mrg,
                dstSize.height + 2 * mrg);

        // scale resizedRegVerts to srcImg space. resizedUndistorted has already the margin
        MatOfPoint2f dstRegRect = scale(resizedRegVerts, resizedUndistorted, dstSizeWithMargin);
        Point[] dstRegPts = dstRegRect.toArray();
        Size dstRegSize = new Size(dstRegPts[3].x - dstRegPts[0].x,
                dstRegPts[1].y - dstRegPts[0].y);

        // swap dstRect and srcRect to get the inverse matrix
        Mat mtx = getPerspectiveTransform(dstRect, srcRect);
        MatOfPoint2f srcRegRect = new MatOfPoint2f();
        // transform region rectangle from destination to source space
        perspectiveTransform(dstRegRect, srcRegRect, mtx);

        // stretch resizedRegVerts vertically or horizontally in order to not lose resolution
        double stretchMul = newAspectRatio / (region.width() / region.height());
        Size newDstRegSize = stretchMul > 1
                ? new Size(dstRegSize.width * stretchMul, dstRegSize.height)
                : new Size(dstRegSize.width, dstRegSize.height / stretchMul);
        // align destination region with axes
        dstRegRect = createRectMatWithMargin(newDstRegSize, 0);

        // finally redo last undistort steps with region
        mtx = getPerspectiveTransform(srcRegRect, dstRegRect);
        Mat dstImg = new Mat();
        warpPerspective(srcImg, dstImg, mtx, newDstRegSize);
        return matToBitmap(dstImg);
    }

    /**
     * Convenience class to group some contour related properties
     */
    private class ContourResult {
        MatOfPoint2f rect;
        double angle, angleConfidence;
        ContourResult(MatOfPoint2f rect, double angle, double angleConfidence) {
            this.rect = rect;
            this.angle = angle;
            this.angleConfidence = angleConfidence;
        }
    }


    //PUBLIC:

    /**
     * To make this {@link ImageProcessor} instance valid, you still need to call {@link #setImage(Bitmap)}.
     */
    public ImageProcessor() {}

    /**
     * Copy constructor
     * @param otherInstance instance to be copied.
     */
    public ImageProcessor(ImageProcessor otherInstance) {
        srcImg = otherInstance.srcImg.clone();
        if (otherInstance.corners != null)
            corners = new MatOfPoint2f(otherInstance.corners.toArray());
        quickCorners = otherInstance.quickCorners;
    }

    /**
     * Constructor that calls {@link #setImage(Bitmap)}.
     * @param bm ticket bitmap. Not null.
     */
    public ImageProcessor(@NonNull Bitmap bm) { setImage(bm); }

    /**
     * Constructor that calls {@link #setImage(Bitmap)}, {@link #setCorners(List)}.
     * @param bm ticket bitmap. Not null.
     * @param corners pre-calculated ticket corners with {@link #findTicket(boolean)}.
     */
    public ImageProcessor(@NonNull Bitmap bm, @NonNull List<PointF> corners) {
        setImage(bm);
        setCorners(corners);
    }

    /**
     * Set content of internal image buffers.
     * Always call this method before any other image manipulation method.
     * @param bm ticket bitmap. Not null.
     */
    public synchronized void setImage(@NonNull Bitmap bm) {
        corners = null;
        srcImg = new Mat();
        bitmapToMat(bm, srcImg);
    }

    /**
     * Find the 4 corners of a ticket, ordered counter-clockwise from the top-left corner of the ticket.
     * The corners are ordered to get a straight ticket (but could be upside down).
     * @param quick <ul> true: faster but more inaccurate, good for real time visual feedback. </ul>
     *              <ul> false: slower but more accurate, good for recalculating the rectangle after the shot
     *                                                    or for analyzing an imported image. </ul>
     * @return list of {@link IPError}.
     *         <p> The rectangle corners are always found (unless the error list contains INVALID_CORNERS),
     *             but they can be useless, depending on the presence of other errors. </p>
     */
    public synchronized List<IPError> findTicket(boolean quick) {
        if (srcImg == null)
            return singletonList(IPError.IMAGE_NOT_SET);

        // prepare binary and edge images
        Mat grayResized = toGrayResized(srcImg, SHORT_SIDE);
        Swap<Mat> graySwap = new Swap<>(grayResized.clone(), new Mat());
        Mat binary = prepareBinaryImg(graySwap).clone(); // I use clone because otherwise
        Mat edges = toEdges(graySwap).clone();           // they will be recycled by the swap.
        graySwap.first = binary; // not cloning the image, it will be overwritten
        opening(graySwap, OPEN_ITERS);
        List<Scored<MatOfPoint>> contours = findBiggestContours(graySwap, quick ? 1 : MAX_CONTOURS);

        // select the contour that most likely contains a Ticket
        List<Scored<ContourResult>> candidates = new ArrayList<>();
        for (Scored<MatOfPoint> contour : contours) {
            Scored<MatOfPoint> sanitizedCtr = contourClosing(contour.obj(), graySwap, CLOSE_ITERS);
            MatOfPoint2f rect = findPolySimple((sanitizedCtr != null ? sanitizedCtr : contour).obj());
            // find Hough lines of ticket text
            graySwap.first = edges.clone();
            removeBackground(graySwap, contour.obj());
            Ref<Double> angleConfidence = new Ref<>();
            double angle = predominantAngle(houghLines(graySwap), angleConfidence);

            if (rect.rows() == 4) {// fix orientation of already found rectangle
                rect = orderRectCorners(rect, angle, grayResized.size());
            } else { // find rotated bounding box of contour
                rect = rotatedBoundingBox(contour.obj(), angle, grayResized.size());
            }
            // if angle is not reliable, correct orientation as width < height (make ticket vertical).
            if (angleConfidence.val < MIN_CONFIDENCE) {
                Size size = rectSizeSimple(rect);
                if (size.width > size.height)
                    rect = shiftMatPoints(rect, angle > 0 ? 1 : 3); // 1 -> rotate clockwise
            }                                                       // 3 -> rotate counter clockwise
            candidates.add(new Scored<>(0., new ContourResult(rect, angle, angleConfidence.val)));
        }
        List<IPError> errors = new ArrayList<>();

        if (candidates.size() > 0) {
            quickCorners = quick;

            //todo assign score and use Collections.max
            ContourResult winner = candidates.get(0).obj();

            //normalize the corners in the space [0, 1]^2
            corners = scale(winner.rect, grayResized.size(), new Size(1, 1));

            // find and return errors
            if (corners.rows() != 4)
                errors.add(IPError.RECT_NOT_FOUND);
            Size size = rectSizeSimple(corners);
            if (winner.angleConfidence < MIN_CONFIDENCE && size.width > size.height)
                errors.add(IPError.UNCERTAIN_DIRECTION);
            if (abs(winner.angle) > CROOCKED_THRESH)
                errors.add(IPError.CROOKED_TICKET);
//        if (!isFocused(grayResized))
//            errors.add(IPError.OUT_OF_FOCUS);
//        IPError exposureErr = checkExposure(grayResized);
//        if (exposureErr != IPError.NONE)
//            errors.add(exposureErr);
        } else {
            errors.add(IPError.INVALID_CORNERS);
        }
        return errors;
    }

    /**
     * Asynchronous version of {@link #findTicket(boolean)}. The errors are passed by the callback parameter.
     * @param quick true: fast mode; false: slow mode.
     * @param callback Callback
     */
    public void findTicket(boolean quick, @NonNull Consumer<List<IPError>> callback) {
        //in a new thread, run findTicket, then return the result executing the callback.
        new Thread(() -> callback.accept(findTicket(quick))).start();
    }

    /**
     * Set pre-calculated ticket rectangle corners.
     * @param corners must be 4, ordered counter-clockwise, first is top-left of ticket. Not null.
     * @return List of IPError, can be:
     * <ul> INVALID_CORNERS: corners are != 4 or not ordered counter-clockwise. </ul>
     */
    public synchronized List<IPError> setCorners(@NonNull List<PointF> corners) {
        if (corners.size() != 4)
            return singletonList(IPError.INVALID_CORNERS);

        this.corners = ptsToMat(androidPtsToCV(corners));
        quickCorners = false;
        //todo check if corners are ordered correctly
        return new ArrayList<>();
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
     * Get a {@link Bitmap} of a ticket with a perspective correction applied, with a margin.
     * @param marginMul Fraction of length of shortest side of the rectangle of the ticket.
     *                  A good value is 0.02.
     * @param shortSide set the shortest side of the output bitmap,
     *                  the other side is calculated to maintain right aspect ratio.
     *                  Useful for generating thumbnails. If <=0, the original side length is used.
     * @return Bitmap of ticket with perspective distortion removed. Null if error.
     */
    public synchronized Bitmap undistort(double marginMul, int shortSide) {
        if (quickCorners || corners == null) {
            if (findTicket(false).contains(IPError.IMAGE_NOT_SET))
                return null;
        }
        return matToBitmap(undistort(srcImg, corners, marginMul, shortSide, false));
    }

    /**
     * Convenience overload for undistort(0, 0).
     * @return Bitmap, null if error.
     */
    public synchronized Bitmap undistort() { return undistort(0, 0); }

    /**
     * Asynchronous version of {@link #undistort(double, int)}. The bitmap is passed by the callback parameter.
     * @param marginMul Margin multiplier
     * @param callback Callback
     */
    public void undistort(double marginMul, int shortSide, @NonNull Consumer<Bitmap> callback) {
        new Thread(() -> callback.accept(undistort(marginMul, shortSide))).start();
    }

    /**
     * Rotate corners, so when undistort is called, the resulting image is rotated 180deg.
     */
    public void rotateUpsideDown() {
        corners = shiftMatPoints(corners, 2); // in a rectangle, opposite corner is 2 corners away
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
    public static List<PointF> transform(List<PointF> points, List<PointF> srcRect, List<PointF> dstRect) {
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
