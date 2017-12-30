package com.ing.software.test.opencvtestapp;

import android.content.res.AssetManager;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.ing.software.common.*;
import com.ing.software.ocr.ImagePreprocessor;

import org.opencv.core.*;
import org.opencv.core.Point;

import static com.ing.software.common.Reflect.*;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.line;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * This app shows in order the pipeline steps to find the ticket rectangle
 */
public class MainActivity extends AppCompatActivity {
    public static final String folder = "photos";

    private final static Class<?> IP_CLASS = ImagePreprocessor.class; // alias
    private static final Scalar BLUE = new Scalar(0,0,255, 255);
    private static final Scalar PURPLE = new Scalar(255,0,255, 255);
    private static final int RED_INT = 0xFFFF0000;
    private static final int GREEN_INT = 0xFF00FF00;
    private static final double MARGIN = 0.05;

    private static final int MSG_TITLE = 0;
    private static final int MSG_IMAGE = 1;
    private static final int MSG_EXCEPTION = 2;


    private AssetManager mgr;
    private Semaphore sem = new Semaphore(0);
    private int imageIdx = 0;
    private boolean resultOnly = false;

    /**
     * This object is used to execute code on the main thread, from another thread.
     */
    private Handler hdl = new Handler(Looper.getMainLooper(), msg -> {
        try {
            switch (msg.what) {
                case MSG_TITLE:
                    this.setTitle((String) msg.obj);
                    break;
                case MSG_IMAGE:
                    ((ImageView) findViewById(R.id.imageView)).setImageBitmap((Bitmap) msg.obj);
                    break;
                case MSG_EXCEPTION:
                    Toast.makeText(this, (String)msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    return false;
            }
            return true;
        }
        catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    });

    /**
     * Change appbar text
     * @param str title text
     */
    private void asyncSetTitle(String str) {
        hdl.obtainMessage(MSG_TITLE, str).sendToTarget();
    }

    /**
     * Show on screen an Android bitmap
     * This method is blocked until user taps screen.
     * @param bm bitmap
     */
    private void showBitmap(Bitmap bm) throws Exception {
        hdl.obtainMessage(MSG_IMAGE, bm).sendToTarget();
        sem.acquire();
    }

    /**
     * Show on screen an OpenCV Mat.
     * This method is blocked until user taps screen.
     * @param img Mat of any color
     */
    private void showMat(Mat img) throws Exception {
        Bitmap bm = invoke(IP_CLASS, "matToBitmap", img);
        showBitmap(bm);
    }

    /**
     * Draw on contour inside Mat
     * @param rgba input RGBA Mat
     * @param contour MatOfPoint
     * @param color Scalar with 4 channels
     * @return output RGBA Mat
     */
    Mat drawContour(Mat rgba, MatOfPoint contour, Scalar color) {
        Mat img = rgba.clone();
        List<MatOfPoint> ctrList = Collections.singletonList(contour);
        drawContours(img, ctrList, 0, color, 3);
        return img;
    }


    /**
     * Draw polygon lines on a Bitmap.
     * @param bm input bitmap
     * @param corns list of corners
     * @param color integer containing channel values in the order: A R G B
     * @return output bitmap
     */
    Bitmap drawPoly(Bitmap bm, List<PointF> corns, int color) {
        Bitmap copy = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(copy);
        c.drawBitmap(bm, 0, 0, null);

        Paint p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(20);
        int cornsTot = corns.size();
        for (int i = 0; i < cornsTot; i++) {
            PointF start = corns.get(i), end = corns.get((i + 1) % cornsTot);
            c.drawLine(start.x, start.y, end.x, end.y, p);
        }
        return copy;
    }

    /**
     * Draw lines contained in a MatOfInt4
     * @param rgba input RGBA Mat
     * @param lines MatOfInt4 containing the lines
     * @param color Scalar with 4 channels
     * @return output RGBA Mat
     */
    Mat drawLines(Mat rgba, MatOfInt4 lines, Scalar color) {
        Mat img = rgba.clone();
        if (lines.rows() > 0) {
            int[] coords = lines.toArray();
            for (int i = 0; i < lines.rows(); i++) {
                line(img, new Point(coords[i * 4], coords[i * 4 + 1]),
                        new Point(coords[i * 4 + 2], coords[i * 4 + 3]), color, 3);
            }
        }
        return img;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.imageView).setOnClickListener(v -> sem.release());
        findViewById(R.id.prev_btn).setOnClickListener(v -> {
            imageIdx--;
            this.setTitle(String.valueOf(imageIdx));
        });
        findViewById(R.id.succ_btn).setOnClickListener(v -> {
            imageIdx++;
            this.setTitle(String.valueOf(imageIdx));
        });
        ((Switch)findViewById(R.id.result_only))
                .setOnCheckedChangeListener((v, checked) -> resultOnly = checked);
        mgr = getResources().getAssets();

        new Thread(this::backgroundWork).start();

    }

    /**
     * This method processes all images from the dataset showing every step of the pipeline.
     * showMat stops the method execution until the screen is tapped.
     */
    void backgroundWork() {
        try {
            int imgsTot = mgr.list(folder).length;
            while(true) {

                if (imageIdx < 0)
                    imageIdx = 0;
                else if(imageIdx >= imgsTot)
                    imageIdx = imgsTot - 1;
                asyncSetTitle(String.valueOf(imageIdx));

                Bitmap bm = BitmapFactory.decodeStream(
                        mgr.open(folder + "/" + String.valueOf(imageIdx) + ".jpg"));

                ImagePreprocessor ip = new ImagePreprocessor(bm);

                if (!resultOnly) {
                    Mat srcImg = getField(ip, "srcImg");
                    Mat rgbaResized = invoke(IP_CLASS, "downScaleRgba", srcImg);
                    showMat(rgbaResized);

                    Swap<Mat> graySwap = new Swap<>(() -> new Mat(rgbaResized.size(), CV_8UC1));

                    invoke(IP_CLASS, "prepareBinaryImg", graySwap, rgbaResized);
                    Mat binary = graySwap.first.clone();
                    showMat(binary);

                    List<CompPair<Double, MatOfPoint>> contours =
                            invoke(IP_CLASS, "findBiggestContours", graySwap, 1);
                    MatOfPoint contour = contours.get(0).obj;

                    graySwap.first = binary;
                    invoke(IP_CLASS, "toEdges", graySwap);
                    invoke(IP_CLASS, "removeBackground", graySwap, contour);
                    showMat(graySwap.first);

                    MatOfInt4 lines = invoke(IP_CLASS, "houghLines", graySwap);
                    // show both contours and hough lines
                    showMat(drawLines(drawContour(rgbaResized, contour, BLUE), lines, PURPLE));

                    MatOfPoint2f cornersMat = invoke(IP_CLASS, "findPolySimple", contour);
                    invoke(IP_CLASS, "scale", cornersMat, srcImg.size(), rgbaResized.size());
                    List<PointF> corners = invoke(IP_CLASS, "cvPtsToAndroid", cornersMat.toList());
                    showBitmap(drawPoly(bm, corners, corners.size() == 4 ? GREEN_INT : RED_INT));
                }

                Semaphore sem = new Semaphore(0);
                ip.findTicket(false, e -> sem.release());
                sem.acquire();
                showBitmap(ip.undistort(MARGIN));

                imageIdx++;
            }
        }
        catch (Exception e) {
            hdl.obtainMessage(MSG_EXCEPTION, e.getMessage()).sendToTarget();
        }
    }

}
