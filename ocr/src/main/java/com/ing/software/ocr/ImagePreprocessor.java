package com.ing.software.ocr;

import static org.bytedeco.javacpp.opencv_core.*;

import org.bytedeco.javacpp.annotation.ByVal;
import org.bytedeco.javacpp.indexer.IntRawIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
//import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import android.graphics.*;
import android.graphics.Point;
import android.support.annotation.NonNull;

import com.ing.software.common.Ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Riccardo Zaglia
 */
public class ImagePreprocessor {

    // length of smallest side of downscaled image
    // shortSide must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final int shortSide = 720;

    //Bilateral filter:
    private static final int bfKerSz = 9; // kernel size, must be odd
    private static final int bfSigma = 20; // color variance

    //Erode/Dilate iterations
    private static final int edIters = 5;

    //Median kernel size
    private static final int medSz = 7; // must be odd

    //Adaptive threshold:
    private static final int thrWinSz = 201; // window size. must be odd
    private static final int thrOffset = 3;

    // factor that determines maximum distance of detected contour from rectangle
    //private static final double polyMaxErrMul = 0.02;
    private static final int polyMaxErr = 30;

    class MatPool {
        //todo
    }


    private Size size;
    private List<Mat> contours = new ArrayList<>(2);

    /**
     * Convert a bitmap to a BGR Mat
     * @param bm bitmap
     * @return Mat BGR
     */
    public static Mat bitmapToMatBGR(@ByVal Bitmap bm) {
        return new OpenCVFrameConverter.ToMat().convert(new AndroidFrameConverter().convert(bm));
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

    /**
     * Downscale image.
     * This method ensures that any image at any resolution or orientation is processed at the same level of detail.
     * @param img in-out ref to BGR Mat
     */
    private static void downScaleBGR(Ref<Mat> img) {
        Mat bgrSrc = img.value;
        float aspectRatio = (float)bgrSrc.rows() / bgrSrc.cols();
        Size size = aspectRatio > 1 ? new Size(shortSide, (int)(shortSide * aspectRatio))
                : new Size((int)(shortSide / aspectRatio), shortSide);
        Mat bgrResized = new Mat(size, CV_8UC3);
        resize(bgrSrc, bgrResized, size);
    }
    /**
     * Convert Mat from BGR to gray
     * @param img in-out ref to Mat (in: BGR, out: gray)
     */
    private static void BGR2Gray(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        cvtColor(img.value, img2, CV_BGR2GRAY);
        img.value = img2;
    }

    /**
     * Bilateral filter
     * @param img in-out ref to gray Mat
     */
    private static void bilatFilter(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        bilateralFilter(img.value, img2, bfKerSz, bfSigma, bfSigma);
        img.value = img2;
    }

    /**
     * Transform a gray image into a mask using an adaptive threshold
     * @param img
     */
    private static void threshold(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        adaptiveThreshold(img.value, img2, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, thrWinSz, thrOffset);
        img.value = img2;
    }


    /**
     * Grow + shrink mask.
     * @param img
     */
    private static void erodeDilate(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        opencv_core.Point pt = new opencv_core.Point(-1, -1);
        erode(img.value, img2, new Mat(), pt, edIters, BORDER_CONSTANT, morphologyDefaultBorderValue());
        dilate(img2, img.value, new Mat(), pt, edIters, BORDER_CONSTANT, morphologyDefaultBorderValue());
    }

    /**
     * Smooth mask contours
     * @param img in-out ref to gray Mat
     */
    private static void median(Ref<Mat> img) {
        Mat img2 = new Mat(img.value.size(), CV_8UC1);
        medianBlur(img.value, img2, medSz);
        img.value = img2;
    }

    /**
     * Make sure that no white area is touching image edges
     * @param img in-out ref to gray Mat
     */
    private static void enclose(Ref<Mat> img) {
        copyMakeBorder(img.value, img.value, 1, 1, 1, 1, BORDER_CONSTANT);
    }

    /**
     * Find all outer contours in a BGR image, sorted by area (descending).
     * @param img BGR Mat
     * @return Contour with biggest area
     */
    private static Mat findBiggestContour(@ByVal Mat img) {
        Ref<Mat> imgRef = new Ref<>(img);

        //I used Ref parameters to enable me to easily reorder the methods
        // and experiment with the image processing pipeline
        downScaleBGR(imgRef);
        BGR2Gray(imgRef);
        bilatFilter(imgRef);
        threshold(imgRef);
        erodeDilate(imgRef);
        //median(imgRef);
        enclose(imgRef);

        MatVector contours = new MatVector();
        Mat hierachy = new Mat();
        findContours(imgRef.value, contours, hierachy, RETR_CCOMP, CHAIN_APPROX_SIMPLE);
        IntRawIndexer idxr = hierachy.createIndexer();
        double maxArea = 0;
        Mat maxContour = new Mat(1,1, CV_32SC2);
        for (int i = 0; i < hierachy.cols(); i++) {
            // select outer contours, ie contours that have no parent (hierachy-1)
            // to know more, go to the link below, look for "RETR_CCOMP":
            // https://docs.opencv.org/3.1.0/d9/d8b/tutorial_py_contours_hierarchy.html
            if (idxr.get(0, i, 3) == -1) {
                Mat contour = contours.get(i);
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
     * Find the four corners of a ticket, oredered clockwise from the top-left corner
     * @param bm input image. Not null.
     * @return List of points in bitmap space (range from (0,0) to (width, height) ).
     *         null if corners cannot be found or more than four sised are found.
     */
    public List<Point> findCorners(Bitmap bm) {
        Mat contour = findBiggestContour(bitmapToMatBGR(bm));

        Mat polyApprox = new Mat();
        //double perimeter = arcLength(contour, true);
        approxPolyDP(contour, polyApprox, polyMaxErr/*polyMaxErrMul * perimeter*/, true);
        if (polyApprox.rows() == 4) { // has polyApprox 4 sides?
            //todo
        }
        //else it's not a quadrilateral
        return null;
    }

}
