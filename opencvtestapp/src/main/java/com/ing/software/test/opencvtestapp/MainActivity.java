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

import static com.ing.software.common.Reflect.fieldVal;
import static com.ing.software.common.Reflect.invoke;
import static org.opencv.imgproc.Imgproc.drawContours;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {
    public static final String folder = "photos";

    ImageView iv = null;
    AssetManager mgr;

    final Semaphore sem = new Semaphore(0);

    private static final Scalar blue = new Scalar(0,0,255, 255);
    private static final int redInt = 0xFFFF0000;
    private static final int greenInt = 0xFF00FF00;

    // alias
    private final static Class<?> IP_CLASS = ImagePreprocessor.class;

    final Handler h = new Handler(Looper.getMainLooper(), msg -> {
        iv.setImageBitmap((Bitmap)msg.obj);
        return true;
    });

    private void showBitmap(Bitmap bm) {
        h.obtainMessage(0, bm).sendToTarget();
        try {
            sem.acquire();
        }
        catch (Exception e) {
            System.out.println();
        }
    }

    private void showMat(Mat img) throws Exception {
        Bitmap bm = invoke(IP_CLASS, "matToBitmap", img);
        showBitmap(bm);
    }

    void drawContour(Mat dst, MatOfPoint contour, Scalar color) {
        //MatVector contourVec = new MatVector(1);
        //contourVec.put(0, contour);
        List<MatOfPoint> ctrList = Collections.singletonList(contour);
        //drawContours();
        drawContours(dst, ctrList, 0, color, 3);
    }

    Bitmap drawPoly(Bitmap src, List<Point> corns, int color) {
        //Bitmap copy; = Bitmap.createScaledBitmap(src, src.getWidth(), src.getHeight(), false);
        Bitmap copy = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(copy);
        c.drawBitmap(src, 0, 0, null);

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

    void backgroundWork() {
        try {
            for (int i = 0; i < mgr.list(folder).length; i++) {
                ImagePreprocessor ip = new ImagePreprocessor();
                Bitmap bm = BitmapFactory.decodeStream(mgr.open(folder + "/" + String.valueOf(i) + ".jpg"));
                ip.setImage(bm);

                Mat srcImg = fieldVal(ip, "srcImg");
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
                drawContour(imgResized, contours.get(0), blue);
                showMat(imgResized);

                Semaphore sem = new Semaphore(0);
                final Ref<TicketError> err = new Ref<>(TicketError.NONE);
                ip.findTicket(false, e -> {
                    err.value = e;
                    sem.release();
                });
                sem.acquire();

                List<Point> pts = ip.getCorners();
                showBitmap(drawPoly(bm, pts, err.value == TicketError.RECT_NOT_FOUND ? redInt : greenInt));

                showBitmap(ip.undistort(0.02));
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
