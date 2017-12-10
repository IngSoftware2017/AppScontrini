package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

import com.ing.software.common.Ref;
import com.ing.software.common.TicketError;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Size; // resolve conflict
import org.opencv.core.Point; // resolve conflict
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Process an image to be suitable to be used by DataAnalyzer.
 * @author Riccardo Zaglia
 */
/*
 * I used only pivate-static and public methods to avoid side effects that increase complexity.
 * I'm sticking to the one-purpose-method rule.
 */
public class ImagePreprocessor {

    // length of smallest side of downscaled image
    // shortSide must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final int shortSide = 720;

    //Bilateral filter:
    private static final int bfKerSz = 9; // kernel size, must be odd
    private static final int bfSigma = 20; // space/color variance

    //Erode/Dilate iterations
    private static final int edIters = 4;

    //Median kernel size
    private static final int medSz = 7; // must be odd

    //Adaptive threshold:
    private static final int thrWinSz = 75; // window size. must be odd
    private static final int thrOffset = 2; //

    // factor that determines maximum distance of detected contour from rectangle
    //private static final double polyMaxErrMul = 0.02;
    private static final int polyMaxErr = 50;

    static {
        if (!OpenCVLoader.initDebug()) {
            OcrUtils.log(0, "OpenCV", "OpenCV failed to initialize");
        }
    }

    class MatPool {
        //todo
    }

    /**
     * This class is created to avoid to recalculate contour area at every comparison during sort
     */
    class Contour implements Comparable<Contour> {
        int area;
        Mat contour;

        Contour(Mat contour) {
            this.contour = contour;
            area = (int)contourArea(contour);
        }

        @Override
        public int compareTo(@NonNull Contour o) {
            return area < o.area ? 1 : -1;
        }
    }


    //private Size size;
    //private List<Mat> contours = new ArrayList<>(2);

    /**
     * Convert a Mat to a Bitmap
     * @param img Mat of any color
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
     * @param img in-out ref to BGR Mat
     */
    private static Mat downScaleRGBA(Mat img) {
        float aspectRatio = (float)img.rows() / img.cols();
        Size size = aspectRatio > 1 ? new Size(shortSide, (int)(shortSide * aspectRatio))
                : new Size((int)(shortSide / aspectRatio), shortSide);
        Mat bgrResized = new Mat(size, CV_8UC4);
        resize(img, bgrResized, size);
        return bgrResized;
    }

    /**
     * Convert Mat from RGBA to gray
     * @param img in-out ref to Mat (in: BGR, out: gray)
     */
    private static void RGBA2Gray(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        cvtColor(img.value, img2, COLOR_RGBA2GRAY);
        img.value = img2;
    }

    /**
     * Bilateral filter
     * @param img in-out ref to gray Mat
     */
    private static void bilateralFilter(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        //Imgproc.bil
        Imgproc.bilateralFilter(img.value, img2, bfKerSz, bfSigma, bfSigma);
        img.value = img2;
    }

    /**
     * Transform a gray image into a mask using an adaptive threshold
     * @param img in-out ref to Mat (in: gray, out: black & white)
     */
    private static void threshold(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        adaptiveThreshold(img.value, img2, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, thrWinSz, thrOffset);
        img.value = img2;
    }

    /**
     * Grow + shrink mask.
     * @param img in-out ref to B&W Mat
     */
    private static void erodeDilate(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        org.opencv.core.Point pt = new Point(-1, -1);
        erode(img.value, img2, new Mat(), pt, edIters);
        dilate(img2, img.value, new Mat(), pt, edIters);
    }

    /**
     * Smooth mask contours
     * @param img in-out ref to B&W Mat
     */
    private static void median(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        medianBlur(img.value, img2, medSz);
        img.value = img2;
    }

    /**
     * Make sure that no white area is touching image edges
     * @param img in-out ref to B&W Mat
     */
    private static void enclose(Ref<Mat> img) {
        copyMakeBorder(img.value, img.value, 1, 1, 1, 1, BORDER_CONSTANT);
    }


    /**
     * Find all outer contours in a BGR image, sorted by area (descending).
     * @param img BGR Mat
     * @return Contour with biggest area
     */
    private static MatOfPoint findBiggestContour(Mat img) {
        Ref<Mat> imgRef = new Ref<>(img);

        //I used Ref parameters to enable me to easily reorder the methods
        // and experiment with the image processing pipeline
        RGBA2Gray(imgRef);
        bilateralFilter(imgRef);
        threshold(imgRef);
        erodeDilate(imgRef);
        //median(imgRef);
        enclose(imgRef);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(imgRef.value, contours, hierarchy, RETR_CCOMP, CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        MatOfPoint maxContour = new MatOfPoint();
        //List<Pair<Mat, Double>> contourSelection = new ArrayList<>();

        for (int i = 0; i < hierarchy.cols(); i++) {
            // select outer contours, ie contours that have no parent (hierarchy-1)
            // to know more, go to the link below, look for "RETR_CCOMP":
            // https://docs.opencv.org/3.1.0/d9/d8b/tutorial_py_contours_hierarchy.html
            double[] asdf= hierarchy.get(0, i);
            if (asdf[3] == -1) {
                MatOfPoint contour = contours.get(i);
                double curArea = contourArea(contour);
                if (curArea > maxArea) {
                    maxArea = curArea;
                    maxContour = contour;
                }
            }
        }
        return maxContour;
    }



    /**
     * Get Size in pixels of a rectangle in perspective
     * @param perspRect ordered corners of a rectangle in perspective
     * @return Size in pixels proportional to the real ticket
     */
    @NonNull
    private static Size getRectSizeSimple(MatOfPoint2f perspRect) {
        Mat[] v = new Mat[4];
        double w = 1, h = 1;
        if (perspRect.rows() == 4) {
            for (int i = 0; i < 4; i++)
                v[i] = perspRect.row(i);
            w = (norm(v[1], v[2]) + norm(v[3], v[0])) / 2;
            h = (norm(v[0], v[1]) + norm(v[2], v[3])) / 2;
        }
        return new Size(w, h);
    }
    //todo: use a better approach:
    // http://andrewkay.name/blog/post/aspect-ratio-of-a-rectangle-in-perspective/

    /**
     * Find the polygon from a contour.
     * @param contour A MatOfPoint containing a contour.
     * @return Polygon corners.
     */
    // I used another method to retrieve the corners to make it optional
    // and to allow me to return the TicketError instead.
    private static MatOfPoint2f findRectangle(MatOfPoint contour) {
        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
        MatOfPoint2f corns = new MatOfPoint2f();
        //double perimeter = arcLength(contour, true);
        approxPolyDP(contour2f, corns, polyMaxErr/*polyMaxErrMul * perimeter*/, true);
        return corns;
    }
    //tests: is corners input non empty? does corners output have 4 elements?

    //INSTANCE FIELDS:

    private Mat srcImg;
    private List<MatOfPoint> contours;
    private MatOfPoint2f corners;



    //PUBLIC:

    /**
     * You need to call setBitmap()
     */
    public ImagePreprocessor() {}

    /**
     * No need to call setBitmap()
     * @param bm ticket bitmap
     */
    public ImagePreprocessor(Bitmap bm) {
        setImage(bm);
    }

    /**
     * Set content of internal image buffers.
     * Always call this method before any other image manipulation method.
     * @param bm ticket bitmap
     */
    public void setImage(Bitmap bm) {
        srcImg = new Mat();
        Utils.bitmapToMat(bm, srcImg);
    }


    /**
     * Find the four corners of a ticket, ordered counter-clockwise from the top-left corner of the ticket.
     * The corners are ordered to get a straight ticket (but could be upside down).
     * To obtain the corners, call getCorners().
     * @param quick true: faster but more errors: good for real time visual feedback. No orientation detection.
     *              false: slower but more accurate: good for recalculating the rectangle after the shot.
     *                                               or for analyzing an imported image.
     * @return TicketError NONE or RECT_NOT_FOUND
     */
    public TicketError findTicket(boolean quick) {
        contours = new ArrayList<>();
        Mat resized = downScaleRGBA(srcImg);
        contours.add(findBiggestContour(resized));
        corners = findRectangle(contours.get(0));

        //scale up the the corners to match the scale of the original image
        double scaleMul = (double)srcImg.cols() / resized.cols();
        multiply(corners.clone(), new Scalar(scaleMul, scaleMul), corners);

        if (corners.rows() != 4)
            return TicketError.RECT_NOT_FOUND;

        if (!quick) {
            //shift corners in order to make top-left corner the first of list.
            final List<Point> corns = new ArrayList<>(corners.toList()); // corners.toList() is immutable
            int tlIdx = Collections.min(Arrays.asList(0, 1, 2, 3), new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Integer.valueOf((int)(corns.get(o1).x + corns.get(o1).y))
                            .compareTo((int)(corns.get(o2).x + corns.get(o2).y));
                }
            });
            //shift corns by tlIdx
            //NB: sublist creates a view, not a copy.
            corns.addAll(corns.subList(0, tlIdx));
            corns.subList(0, tlIdx).clear();
            corners = new MatOfPoint2f(corns.toArray(new Point[4]));
        }


        return TicketError.NONE;
    }

    /**
     * Get rectangle (or polygon) corners.
     * @return List of points in bitmap space (range from (0,0) to (width, height) ).
     *         The corners might be more or less than 4. Never null.
     */
    public List<android.graphics.Point> getCorners() {
        List<android.graphics.Point> androidPts = new ArrayList<>(corners.rows());
        for (Point p : corners.toArray())
            androidPts.add(new android.graphics.Point((int)p.x, (int)p.y));
        return androidPts;
    }

    /**
     * Get a Bitmap of a ticket with a perspective correction applied, with a margin.
     * @param marginMul fraction/percentage of length of shortest side to be used as margin. Ex 0.1
     * @return Bitmap of ticket with perspective distortion removed
     */
    public Bitmap undistort(double marginMul) {
        MatOfPoint2f dstRect;
        Mat dstImg = srcImg.clone();
        if (corners.rows() > 4) {
            //todo select
            return matToBitmap(dstImg); // todo remove
        }
        else if (corners.rows() < 4) { // very unlikely case where the biggest contour is a triangle
            return matToBitmap(dstImg);
        }

        Size sz = getRectSizeSimple(corners);
        double m = marginMul * Math.min(sz.width, sz.height);

        dstRect = new MatOfPoint2f( // counter-clockwise
                new Point(m, m),
                new Point(m, sz.height + m),
                new Point(sz.width + m, sz.height + m),
                new Point(sz.width + m, m));
        Mat mtx = getPerspectiveTransform(corners, dstRect);
        warpPerspective(srcImg, dstImg, mtx, new Size(sz.width + 2 * m,sz.height + 2 * m));

        return matToBitmap(dstImg);
    }


    //public Bitmap rotate180(int ) {

    //}



    //UTILITY FUNCTIONS:

    /**
     * Rotate a bitmap
     * @param src Source bitmap
     * @param angle Rotation angle (degrees)
     * @return Rotated bitmap
     */
    public static Bitmap rotate(Bitmap src, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }


}
