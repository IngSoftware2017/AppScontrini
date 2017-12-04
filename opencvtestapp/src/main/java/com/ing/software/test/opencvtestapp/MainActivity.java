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
import com.ing.software.ocr.ImagePreprocessor;

import org.bytedeco.javacpp.opencv_core;

import static com.ing.software.ocr.ImagePreprocessor.*;
import static com.ing.software.common.Reflect.invoke;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.INTER_MAX;
import static org.bytedeco.javacpp.opencv_imgproc.drawContours;

public class MainActivity extends AppCompatActivity {
    public static final String folder = "photos";

    ImageView iv = null;
    AssetManager mgr;

    final Semaphore sem = new Semaphore(0);

    private static final Scalar blue = new Scalar(255,0,0, 255);
    private static final Scalar red = new Scalar(0,0,255, 255);

    // alias
    final Class<ImagePreprocessor> IP_CLASS = ImagePreprocessor.class;

    final Handler h = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            iv.setImageBitmap((Bitmap)msg.obj);
        }
    };

    private void drawMat(Mat img) {
        Bitmap bm = matToBitmap(img);
        h.obtainMessage(0, bm).sendToTarget();
        try {
            sem.acquire();
        }
        catch (Exception e) {
            System.out.println();
        }
    }

    void drawContour(Mat dst, Mat contour, Scalar color) {
        MatVector contourVec = new MatVector(1);
        contourVec.put(0, contour);
        drawContours(dst, contourVec, 0, color, 3, 8, new Mat(), INTER_MAX, new opencv_core.Point());
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
                try {
                    for (int i = 0; i < mgr.list(folder).length; i++) {
                        Bitmap bm = BitmapFactory.decodeStream(mgr.open(folder + "/" + String.valueOf(i) + ".jpg"));
                        Mat origImg = bitmapToMatBGR(bm);
                        Mat imgResized = invoke(IP_CLASS, "downScaleBGR", origImg);
                        Ref<Mat> imgRef = new Ref<>(imgResized);
                        drawMat(imgRef.value);

                        invoke(IP_CLASS, "BGR2Gray", imgRef);
                        invoke(IP_CLASS, "bilatFilter", imgRef);
                        drawMat(imgRef.value);

                        invoke(IP_CLASS, "threshold", imgRef);
                        drawMat(imgRef.value);

                        invoke(IP_CLASS, "erodeDilate", imgRef);
                        invoke(IP_CLASS, "enclose", imgRef);
                        drawMat(imgRef.value);

                        Mat contour = invoke(IP_CLASS, "findBiggestContour", imgResized);
                        drawContour(imgResized, contour, blue);
                        drawMat(imgResized);

                        //List<Point> pts = new ArrayList<>();
                        //findRectangle(bm, pts);

                    }
                }
                catch (Exception e) {
                    System.out.println();
                }
            }
        }.start();
    }
}
