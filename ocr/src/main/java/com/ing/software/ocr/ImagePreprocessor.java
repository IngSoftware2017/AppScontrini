package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.ing.software.common.*;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Size; // resolve conflict
import org.opencv.core.Point; // resolve conflict
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.*;
import static com.annimon.stream.Stream.*;


/*
USAGE CASES:

Real time visual feedback when framing a ticket:
 1) Create new ImagePreprocessor passing a the bitmap of the preview frame.
 2) Call findTicket(true);
 3) If findTicket returns NONE, call getCorners to get the rectangle corners to overlay on top of preview.
 4) Call getCorners to get a rectangle (if findTicket returns NONE) or a polygon (if findTicket returns RECT_NOT_FOUND).

User shoots a photo:
 1) Use ImageProcessor instance of last frame when rectangle was found.
 2) Call findTicket(false);
 3) If findTicket returns NONE proceed to step 5)
 4) If findTicket returns RECT_NOT_FOUND, let user drag the rectangle corners into position,
    then proceed to call setCorners().
 5) Call OcrManager.getTicket passing this ImagePreprocessor instance.
    Call undistort to get the photo of the ticket unwarped.

New photo loaded from storage:
 Same as when user shot a photo, but ImagePreprocessor must be created with loaded photo.

Load and show photo already processed:
 1) Create new ImagePreprocessor passing the photo and the corners.
 2) Call undistort().
 
 */

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
    // SHORT_SIDE must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final int SHORT_SIDE = 720;

    //Bilateral filter:
    private static final int BF_KER_SZ = 9; // kernel size, must be odd
    private static final int BF_SIGMA = 20; // space/color variance

    //Erode/Dilate iterations
    private static final int E_D_ITERS = 4;

    //Erode for hugh lines
    private static final int ERODE_ITERS = 2;

    //Median kernel size
    private static final int MED_SZ = 7; // must be odd

    //Enclose border thickness
    private static final int BORD_THICK = 2;

    //Adaptive threshold:
    private static final int THR_WIN_SZ = 75; // window size. must be odd
    private static final int THR_OFFSET = 2; //

    // factor that determines maximum distance of detected contour from rectangle
    //private static final double polyMaxErrMul = 0.02;
    private static final int POLY_MAX_ERR = 50;

    static {
        if (!OpenCVLoader.initDebug()) {
            OcrUtils.log(0, "OpenCV", "OpenCV failed to initialize");
        }
    }

    class MatPool {
        //todo
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
     * @param img Mat RGBA. Not null.
     * @return Mat RGBA
     */
    private static Mat downScaleRGBA(Mat img) {
        float aspectRatio = (float)img.rows() / img.cols();
        Size size = aspectRatio > 1 ? new Size(SHORT_SIDE, (int)(SHORT_SIDE * aspectRatio))
                : new Size((int)(SHORT_SIDE / aspectRatio), SHORT_SIDE);
        Mat bgrResized = new Mat(size, CV_8UC4);
        resize(img, bgrResized, size);
        return bgrResized;
    }

    /**
     * Convert Mat from RGBA to gray
     * @param img in-out ref to Mat (in: BGR, out: gray). Original mat is not modified. Not null.
     */
    private static void RGBA2Gray(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        cvtColor(img.value, img2, COLOR_RGBA2GRAY);
        img.value = img2;
    }

    /**
     * Bilateral filter
     * @param img in-out ref to gray Mat. Original mat is not modified. Not null.
     */
    private static void bilateralFilter(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        Imgproc.bilateralFilter(img.value, img2, BF_KER_SZ, BF_SIGMA, BF_SIGMA);
        img.value = img2;
    }

    /**
     * Transform a gray image into a mask using an adaptive threshold
     * @param img in-out ref to Mat (in: gray, out: black & white). Original mat is not modified. Not null.
     */
    private static void threshold(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        adaptiveThreshold(img.value, img2, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, THR_WIN_SZ, THR_OFFSET);
        img.value = img2;
    }

    private static void erode(Ref<Mat> img, int iters) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        Imgproc.erode(img.value, img2, new Mat(), new Point(-1, -1), iters);
        img.value = img2;
    }

    private static void dilate(Ref<Mat> img, int iters) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        Imgproc.dilate(img.value, img2, new Mat(), new Point(-1, -1), iters);
        img.value = img2;
    }

    /**
     * Grow + shrink mask.
     * @param img in-out ref to B&W Mat. Original mat is not modified. Not null.
     */
    private static void erodeDilate(Ref<Mat> img) {
        erode(img, E_D_ITERS);
        dilate(img, E_D_ITERS);
    }

    /**
     * Smooth mask contours
     * @param img in-out ref to B&W Mat. Original mat is not modified. Not null.
     */
    private static void median(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        medianBlur(img.value, img2, MED_SZ);
        img.value = img2;
    }

    /**
     * Make sure that no white area is touching image edges
     * @param img in-out ref to B&W Mat. Original mat IS modified. Not null.
     */
    private static void enclose(Ref<Mat> img) {
        copyMakeBorder(img.value, img.value, BORD_THICK, BORD_THICK, BORD_THICK, BORD_THICK, BORDER_CONSTANT);
    }

    private static Mat prepareBinaryImg(Mat img) {
        Ref<Mat> imgRef = new Ref<>(img);
        // I used Ref parameters to enable me to easily reorder the methods
        // and experiment with the image processing pipeline
        RGBA2Gray(imgRef);
        bilateralFilter(imgRef);
        threshold(imgRef);
        enclose(imgRef);
        return imgRef.value;
    }

    /**
     * Find k biggest outer contours in a RGBA image, sorted by area (descending).
     * @param img RGBA Mat. Not null.
     * @param k number of contours to return.
     * @return Contour with biggest area. Never null.
     */
    private static List<MatOfPoint> findBiggestContours(Mat img, int k) {
        Ref<Mat> imgRef = new Ref<>(img);
        erodeDilate(imgRef);
        //median(imgRef);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(imgRef.value, contours, hierarchy, RETR_CCOMP, CHAIN_APPROX_SIMPLE);

        Podium<CompPair<Double, MatOfPoint>> podium = new Podium<>(k);
        for (int i = 0; i < hierarchy.cols(); i++) {
            // select outer contours, i.e. contours that have no parent (hierarchy-1)
            // to know more, go to the link below, look for "RETR_CCOMP":
            // https://docs.opencv.org/3.1.0/d9/d8b/tutorial_py_contours_hierarchy.html
            if (hierarchy.get(0, i)[3] == -1) {
                MatOfPoint ctr = contours.get(i);
                podium.tryAdd(new CompPair<>(contourArea(ctr), ctr));
            }
        }
        return Stream.of(podium.getAll()).map(cp -> cp.obj).toList();
    }


    private static Mat removeBackground(Mat img, MatOfPoint contour) {
        List<MatOfPoint> ctrList = new ArrayList<>(1);
        ctrList.add(contour);
        Mat mask = new Mat(img.rows(), img.cols(), CV_8UC1, new Scalar(255));
        fillPoly(mask, ctrList, new Scalar(0));
        Ref<Mat> maskRef = new Ref<>(mask);
        dilate(maskRef, ERODE_ITERS + 1);
        bitwise_or(img, img, maskRef.value);
        return img;
    }

    private static void findHoughLines() {
        //todo
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
        approxPolyDP(contour2f, corns, POLY_MAX_ERR/*polyMaxErrMul * perimeter*/, true);
        return corns;
    }
    //todo tests: is corners input non empty?

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

    public ImagePreprocessor(Bitmap bm, List<android.graphics.Point> corners) {
        setImage(bm);
        setCorners(corners);
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
        Mat binary = prepareBinaryImg(resized);
        contours = findBiggestContours(binary, quick ? 1 : 3);

        List<CompPair<Double, MatOfPoint2f>> candidates = new ArrayList<>();
        for (MatOfPoint ctr : contours) {
            if (!quick) {
                // find Hough lines of ticket text
                Ref<Mat> binRef = new Ref<>(binary);
                //NB: original "binary" is not modified
                erode(binRef, ERODE_ITERS);
                Mat houghReady = removeBackground(binRef.value, ctr);
                //todo find Hough lines + RANSAC to find undistort transform matrix
            }
            MatOfPoint2f rect = findRectangle(ctr);
            candidates.add(new CompPair<>(0.0, rect));
        }
        //todo assign score and use Collections.max
        corners = candidates.get(0).obj;

        //scale up the the corners to match the scale of the original image
        double scaleMul = (double)srcImg.cols() / resized.cols();
        multiply(corners.clone(), new Scalar(scaleMul, scaleMul), corners);

        if (corners.rows() != 4)
            return TicketError.RECT_NOT_FOUND;

        if (!quick) {
            List<Point> corns = new ArrayList<>(corners.toList()); // corners.toList() is immutable
            //find index of point closer to top-left corner of image.
            int topLeftIdx = Collections.min(range(0, 4)
                    .map(i -> new CompPair<>(corns.get(i).x + corns.get(i).y, i)).toList()).obj;

            //shift corns by topLeftIdx
            //NB: sublist creates a view, not a copy.
            corns.addAll(corns.subList(0, topLeftIdx));
            corns.subList(0, topLeftIdx).clear();
            corners = new MatOfPoint2f(corns.toArray(new Point[4]));
        }

        return TicketError.NONE;
    }

    public TicketError setCorners(List<android.graphics.Point> corners) {
        this.corners = new MatOfPoint2f(Stream.of(corners)
                .map(p -> new Point(p.x, p.y)).toArray(v -> new Point[corners.size()]));
        return corners.size() == 4 ? TicketError.NONE : TicketError.INVALID_POINTS;
    }

    /**
     * Get rectangle (or polygon) corners.
     * @return List of points in bitmap space (range from (0,0) to (width, height) ).
     *         The corners might be more or less than 4. Never null.
     */
    public List<android.graphics.Point> getCorners() {
        return Stream.of(corners.toList())
                .map(p -> new android.graphics.Point((int)p.x, (int)p.y)).toList();
    }

    /**
     * Get a Bitmap of a ticket with a perspective correction applied, with a margin.
     * @param marginMul fraction of length of shortest side of the rectangle of the ticket.
     *                  A good value is 0.02
     * @return Bitmap of ticket with perspective distortion removed
     */
    public Bitmap undistort(double marginMul) {
        if (corners == null)
            findTicket(false);
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
