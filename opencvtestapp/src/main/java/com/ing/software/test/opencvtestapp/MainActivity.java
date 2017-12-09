package com.ing.software.test.opencvtestapp;

import android.content.res.AssetManager;
import android.graphics.*;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
    private final static Class<ImagePreprocessor> IP_CLASS = ImagePreprocessor.class;

    final Handler h = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            iv.setImageBitmap((Bitmap)msg.obj);
        }
    };

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
        mgr = getResources().getAssets();
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sem.release();
            }
        });

        new Thread() {
            @Override
            public void run() {
                backgroundWork();
            }
        }.start();
    }

    void backgroundWork() {
        int len = 0;
        try {
            len = mgr.list(folder).length;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

        for (int i = 0; i < len; i++) {
            ImagePreprocessor ip = new ImagePreprocessor();
            Bitmap bm = null;
            try {
                bm = BitmapFactory.decodeStream(mgr.open(folder + "/" + String.valueOf(i) + ".jpg"));
                ip.setImage(bm);

                Mat srcImg = fieldVal(ip, "srcImg");
                Mat imgResized = invoke(IP_CLASS, "downScaleRGBA", srcImg);
                Ref<Mat> imgRef = new Ref<>(imgResized);
                showMat(imgRef.value);

                invoke(IP_CLASS, "RGBA2Gray", imgRef);
                invoke(IP_CLASS, "bilateralFilter", imgRef);
                showMat(imgRef.value);

                invoke(IP_CLASS, "threshold", imgRef);
                invoke(IP_CLASS, "erodeDilate", imgRef);
                invoke(IP_CLASS, "enclose", imgRef);
                showMat(imgRef.value);

                MatOfPoint contour = invoke(IP_CLASS, "findBiggestContour", imgResized);
                drawContour(imgResized, contour, blue);
                showMat(imgResized);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }

            TicketError err = ip.findRectangle();
            List<Point> pts = ip.getCorners();
            showBitmap(drawPoly(bm, pts, err == TicketError.RECT_NOT_FOUND ? redInt : greenInt));

            showBitmap(ip.undistort(0));
        }
    }

}
