package com.ing.software.test.opencvtestapp;

import android.content.res.AssetManager;
import android.graphics.*;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.ing.software.common.Ref;
import com.ing.software.common.TicketError;
import com.ing.software.ocr.ImagePreprocessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;

import static com.ing.software.common.Reflect.*;
import static org.opencv.imgproc.Imgproc.drawContours;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * This app shows in order the pipeline steps to find the ticket rectangle
 */
public class MainActivity extends AppCompatActivity {
    public static final String folder = "photos";

    ImageView iv = null;
    AssetManager mgr;

    final Semaphore sem = new Semaphore(0);

    private static final Scalar BLUE = new Scalar(0,0,255, 255);
    private static final int RED_INT = 0xFFFF0000;
    private static final int GREEN_INT = 0xFF00FF00;

    // alias
    private final static Class<?> IP_CLASS = ImagePreprocessor.class;

    /**
     * this object is used to execute code on the main thread, from another thread.
     */
    final Handler h = new Handler(Looper.getMainLooper(), msg -> {
        iv.setImageBitmap((Bitmap)msg.obj);
        return true;
    });

    /**
     * Show on screen an Android bitmap
     * This method is blocked until user taps screen.
     * @param bm bitmap
     */
    private void showBitmap(Bitmap bm) {
        h.obtainMessage(0, bm).sendToTarget();
        try {
            sem.acquire();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Show on screen an OpenCV Mat.
     * This method is blocked until user taps screen.
     * @param img Mat any color
     */
    private void showMat(Mat img) throws Exception {
        try {
            Bitmap bm = invoke(IP_CLASS, "matToBitmap", img);
            showBitmap(bm);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Draw on contour inside Mat
     * @param img in-out Mat RGBA
     * @param contour MatOfPoint
     * @param color Scalar with 3 or 4 channels
     */
    void drawContour(Mat img, MatOfPoint contour, Scalar color) {
        List<MatOfPoint> ctrList = Collections.singletonList(contour);
        drawContours(img, ctrList, 0, color, 3);
    }

    /**
     * Draw polygon lines on a Bitmap
     * @param bm input bitmap
     * @param corns list of corners
     * @param color integer containing channel values in the order: A R G B
     * @return final bitmap
     */
    Bitmap drawPoly(Bitmap bm, List<Point> corns, int color) {
        Bitmap copy = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(copy);
        c.drawBitmap(bm, 0, 0, null);

        Paint p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(20);
        int cornsTot = corns.size();
        for (int i = 0; i < cornsTot; i++) {
            Point start = corns.get(i), end = corns.get((i + 1) % cornsTot);
            c.drawLine(start.x, start.y, end.x, end.y, p);
        }
        return copy;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.imageView);
        iv.setOnClickListener(v -> sem.release());
        mgr = getResources().getAssets();

        new Thread(this::backgroundWork).start();
    }

    /**
     * The entire image processing pipeline in one method.
     * showMat stops the method execution until the screen is tapped.
     */
    void backgroundWork() {
        try {
            for (int i = 0; i < mgr.list(folder).length; i++) {
                ImagePreprocessor ip = new ImagePreprocessor();
                Bitmap bm = BitmapFactory.decodeStream(mgr.open(folder + "/" + String.valueOf(i) + ".jpg"));
                ip.setImage(bm);

                Mat srcImg = getField(ip, "srcImg");
                Mat imgResized = invoke(IP_CLASS, "downScaleRGBA", srcImg);
                Ref<Mat> imgRef = new Ref<>(imgResized);
                showMat(imgRef.value);

                invoke(IP_CLASS, "RGBA2Gray", imgRef);
                invoke(IP_CLASS, "bilateralFilter", imgRef);
                showMat(imgRef.value);

                invoke(IP_CLASS, "threshold", imgRef);
                invoke(IP_CLASS, "enclose", imgRef);
                invoke(IP_CLASS, "erodeDilate", imgRef);
                showMat(imgRef.value);

                Mat contourReady = invoke(IP_CLASS, "prepareBinaryImg", imgResized);
                List<MatOfPoint> contours = invoke(IP_CLASS, "findBiggestContours", contourReady, 1);
                drawContour(imgResized, contours.get(0), BLUE);
                showMat(imgResized);

                Semaphore sem = new Semaphore(0);
                final Ref<TicketError> err = new Ref<>(TicketError.NONE);
                final Ref<List<Point>> ptsRef = new Ref<>();
                ip.findTicket(false, e -> {
                    err.value = e;
                    ptsRef.value = ip.getCorners();
                    sem.release();
                });
                sem.acquire();
                showBitmap(drawPoly(bm, ptsRef.value, err.value == TicketError.RECT_NOT_FOUND ? RED_INT : GREEN_INT));

                showBitmap(ip.undistort(0.02));
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
