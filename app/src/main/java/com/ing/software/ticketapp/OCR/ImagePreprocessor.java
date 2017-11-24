package com.ing.software.ticketapp.OCR;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
//import static org.bytedeco.javacpp.opencv_imgcodecs.*;

import android.graphics.*;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

class ImagePreprocessor {

    /**
     * Find the four corners of a ticket
     * @param bm input image. Not null.
     * @return List of points in bitmap space (range from (0,0) to (width, height) ).
     *         null if corners cannot be found.
     */
    public static List<Point> findCorners(Bitmap bm) {
        Mat rgbMat = new OpenCVFrameConverter.ToMat().convert(new AndroidFrameConverter().convert(bm));

        Mat graySrc = new Mat(rgbMat.rows(), rgbMat.cols(), CV_8UC1);
        Mat blurred = graySrc.clone(), edges = graySrc.clone();

        cvtColor(rgbMat, graySrc, CV_RGB2GRAY);
        int win = 5;
        GaussianBlur(graySrc, blurred, new Size(win, win), 0);



        Canny(blurred, edges, 75, 180);

        MatVector contours = new MatVector();
        findContours(edges, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);



//        org.bytedeco.javacv.Conver
//        cvCvtColor(cvImg, cvImg, CV_RGB2GRAY);
//        int win = 5;
//        cvSmooth(cvImg, cvImg, CV_GAUSSIAN, win, win, 0, 0);

        //List<Point> pts = new ArrayList<>(4);
        return null;
    }
}
