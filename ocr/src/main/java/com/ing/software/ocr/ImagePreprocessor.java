package com.ing.software.ocr;

import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
//import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import android.graphics.*;
import android.graphics.Point;

import java.util.List;

/**
 * @author Riccardo Zaglia
 */
public class ImagePreprocessor {

    // length of smallest side of downscaled image
    // shortSide must be chosen to limit side effects of resampling, on both 16:9 and 4:3 aspect ratio images
    private static final int shortSide = 720;

    // window size for gaussian smooth, must be odd
    private static final int smoothWin = 5;

    // factor tha determines maximum distance of detected contour from rectangle
    private static final double polyMaxErr = 0.02;


    private static Size size;

    /**
     * Wrapper for pass-by-reference in java
     */
    static class MatRef {
        Mat value;

        MatRef(Mat img) {
            value = img;
        }
    }


    /**
     * Convert a bitmap to a BGR Mat
     * @param bm bitmap
     * @return Mat BGR
     */
    public static MatRef bitmapToMatBGR(Bitmap bm) {
        return new MatRef(new OpenCVFrameConverter.ToMat().convert(new AndroidFrameConverter().convert(bm)));
    }

    /**
     * Find the four corners of a ticket
     * @param bm input image. Not null.
     * @return List of points in bitmap space (range from (0,0) to (width, height) ).
     *         null if corners cannot be found.
     */
    public static List<Point> findCorners(Bitmap bm) {
//        Mat rgbMat = bitmapToMatBGR(bm);
//
//        Mat graySrc = new Mat(rgbMat.rows(), rgbMat.cols(), CV_8UC1);
//        Mat blurred = graySrc.clone(), edges = graySrc.clone();
//
//        cvtColor(rgbMat, graySrc, CV_RGB2GRAY);
//        int win = 5;
//        GaussianBlur(graySrc, blurred, new Size(win, win), 0);
//
//
//
//        Canny(blurred, edges, 75, 180);
//
//        MatVector contours = new MatVector();
//        findContours(edges, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);



//        org.bytedeco.javacv.Conver
//        cvCvtColor(cvImg, cvImg, CV_RGB2GRAY);
//        int win = 5;
//        cvSmooth(cvImg, cvImg, CV_GAUSSIAN, win, win, 0, 0);

        //List<Point> pts = new ArrayList<>(4);
        return null;
    }

    /**
     * Downscale image.
     * @param img
     * @param shortSide
     */
    void downScaleBGR(MatRef img, int shortSide) {
        Mat bgrSrc = img.value;
        float aspectRatio = (float)bgrSrc.rows() / bgrSrc.cols();
        Size size = aspectRatio > 1 ? new Size(shortSide, (int)(shortSide * aspectRatio))
                : new Size((int)(shortSide / aspectRatio), shortSide);
        Mat bgrResized = new Mat(size, CV_8UC3);
        resize(bgrSrc, bgrResized, size);
    }

    void BGR2Gray(MatRef img) {
        Mat grayImg = new Mat(img.value.size(), CV_8UC1);
        cvtColor(img.value, grayImg, CV_BGR2GRAY);
        img.value = grayImg;
    }

    /**
     *
     * @param img MatRef
     */
    void bifilter(MatRef img) {
        bilateralFilter(img.value, img.value, 9, 20, 20);
    }


    
}
