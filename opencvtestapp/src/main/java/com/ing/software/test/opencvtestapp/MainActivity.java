package com.ing.software.test.opencvtestapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ing.software.common.*;
import com.ing.software.ocr.ImagePreprocessor;
import com.ing.software.ocr.OcrObjects.Block;
import com.ing.software.ocr.OcrObjects.TextLine;
import com.ing.software.ocr.OcrObjects.Word;

import org.opencv.core.*;
import org.opencv.core.Point;

import static com.ing.software.common.Reflect.*;
import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.fillPoly;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.polylines;
import static org.opencv.imgproc.Imgproc.putText;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * This app shows in order the pipeline steps to find the ticket rectangle
 */
public class MainActivity extends AppCompatActivity {

    private static final String folder = "TestOCR";
    private static final Class<?> IP_CLASS = ImagePreprocessor.class; // alias
    private static final int DEF_THICK = 20;
    private static final int BLOCK_THICK = 7;
    private static final int LINE_THICK = 4;
    private static final double FONT_SIZE = 0.5;

    private static final Scalar BLUE = new Scalar(0,0,255, 255);
    private static final Scalar PURPLE = new Scalar(255,0,255, 255);
    private static final Scalar RED = new Scalar(255,0,0, 255);
    private static final Scalar GREEN = new Scalar(0,255,0, 255);
    private static final Scalar DARK_GREEN = new Scalar(0,127,0, 255);
    private static final Scalar WHITE = new Scalar(255,255,255, 255);

    private static final int MSG_TITLE = 0;
    private static final int MSG_IMAGE = 1;
    private static final int MSG_EXCEPTION = 2;


    //private AssetManager mgr;
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
                    Toast.makeText(this, "Exception: " + msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    return false;
            }
            return true;
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    });

    ExceptionHandler errHdlr = new ExceptionHandler(e ->
            hdl.obtainMessage(MSG_EXCEPTION, e.getMessage()).sendToTarget());

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
    static Mat drawContour(Mat rgba, MatOfPoint contour, Scalar color) {
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
    static Bitmap drawPoly(Bitmap bm, List<PointF> corns, int color, float thickness) {
        Bitmap copy = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(copy);
        c.drawBitmap(bm, 0, 0, null);

        Paint p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(thickness);
        int cornsTot = corns.size();
        for (int i = 0; i < cornsTot; i++) {
            PointF start = corns.get(i), end = corns.get((i + 1) % cornsTot);
            c.drawLine(start.x, start.y, end.x, end.y, p);
        }
        return copy;
    }

    @NonNull
    static List<MatOfPoint> pts2mat(List<PointF> pts) throws Exception {
        List<Point> cvPts = invoke(IP_CLASS, "androidPtsToCV", pts);
        MatOfPoint ptsMat = new MatOfPoint(cvPts.toArray(new Point[4]));
        return Collections.singletonList(ptsMat);
    }
    static Mat drawOcrResult(Bitmap bm, List<Block> blocks) throws Exception {
        Mat img = new Mat();
        bitmapToMat(bm, img);

        for (Block b : blocks) {
            polylines(img, pts2mat(b.corners()), true, DARK_GREEN, BLOCK_THICK);
            for (TextLine l : b.lines()) {
                polylines(img, pts2mat(l.corners()), true, RED, LINE_THICK);
                for (Word w : l.words()) {
                    fillPoly(img, pts2mat(w.corners()), BLUE);
                }
            }
        }
        for (Block b : blocks) {
            for (TextLine l : b.lines()) {
                for (Word w : l.words()) {
                    PointF blPt = w.corners().get(3); // bottom-right point (clockwise from top-left)
                    putText(img, w.text(), new Point(blPt.x + 2, blPt.y - 2),
                            FONT_HERSHEY_SIMPLEX, FONT_SIZE, WHITE);
                }
            }
        }
        return img;
    }

    /**
     * Draw text on a Bitmap.
     * @param bm input bitmap
     * @param color integer containing channel values in the order: A R G B
     * @return output bitmap
     */
    static Bitmap drawText(Bitmap bm, String text, PointF pos, int color) {
        Bitmap copy = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(copy);
        c.drawBitmap(bm, 0, 0, null);

        Paint p = new Paint();
        p.setColor(color);
        p.setTextSize(20);
        c.drawText(text, pos.x, pos.y, p);
        return copy;
    }

    /**
     * Draw lines contained in a MatOfInt4
     * @param rgba input RGBA Mat
     * @param lines MatOfInt4 containing the lines
     * @param color Scalar with 4 channels
     * @return output RGBA Mat
     */
    static Mat drawLines(Mat rgba, MatOfInt4 lines, Scalar color) {
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

    static int getImgsTot() {
        return new File(Environment.getExternalStorageDirectory().toString() + "/" + folder)
                .listFiles().length;
    }

    static Bitmap getBitmap(int idx) throws Exception {
        return BitmapFactory.decodeStream(new FileInputStream(
                new File(Environment.getExternalStorageDirectory().toString()
                + "/" + folder  + "/" + String.valueOf(idx) + ".jpg")));
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
        //mgr = getResources().getAssets();

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        new Thread(this::backgroundWork).start();
    }

    /**
     * This method processes all images from the dataset showing every step of the pipeline.
     * showMat stops the method execution until the screen is tapped.
     */
    void backgroundWork() {
        errHdlr.tryRun(() -> {
            TextRecognizer ocrEngine = new TextRecognizer.Builder(this).build();
            //isoperational

            int imgsTot = getImgsTot();
            while(true) {

                if (imageIdx < 0)
                    imageIdx = 0;
                else if(imageIdx >= imgsTot)
                    imageIdx = imgsTot - 1;
                asyncSetTitle(String.valueOf(imageIdx));

                Bitmap bm = getBitmap(imageIdx);

                ImagePreprocessor ip = new ImagePreprocessor(bm);

                if (!resultOnly) {
                    Mat srcImg = getField(ip, "srcImg");
                    Mat rgbaResized = invoke(IP_CLASS, "downScaleRgba", srcImg);
                    showMat(rgbaResized);

                    Swap<Mat> graySwap = new Swap<>(Mat::new);

                    invoke(IP_CLASS, "prepareBinaryImg", graySwap, rgbaResized);
                    Mat binary = graySwap.first.clone();
                    showMat(binary);

                    List<Scored<MatOfPoint>> contours =
                            invoke(IP_CLASS, "findBiggestContours", graySwap, 1);
                    MatOfPoint contour = contours.get(0).obj();

                    graySwap.first = binary;
                    invoke(IP_CLASS, "toEdges", graySwap);
                    invoke(IP_CLASS, "removeBackground", graySwap, contour);
                    showMat(graySwap.first);

                    MatOfInt4 lines = invoke(IP_CLASS, "houghLines", graySwap);
                    // show both contours and hough lines
                    showMat(drawLines(drawContour(rgbaResized, contour, BLUE), lines, PURPLE));

                    MatOfPoint2f cornersMat = invoke(IP_CLASS, "findPolySimple", contour);
                    cornersMat = invoke(IP_CLASS, "scale", cornersMat, rgbaResized.size(), srcImg.size());
                    List<PointF> corners = invoke(IP_CLASS, "cvPtsToAndroid", cornersMat.toList());
                    showBitmap(drawPoly(bm, corners, corners.size() == 4 ? Color.GREEN : Color.RED, DEF_THICK));
                }

                Semaphore sem = new Semaphore(0);
                ip.findTicket(false, e -> sem.release());
                sem.acquire();
                Bitmap finalBm = invoke(ip, "undistortResized");
                //Bitmap finalBm = ip.undistort(0.05);
                SparseArray<TextBlock> sparse = ocrEngine
                        .detect(new Frame.Builder().setBitmap(finalBm).build());
                List<Block> blocks = new ArrayList<>();
                for (int i = 0; i < sparse.size(); i++) {
                    blocks.add(new Block(sparse.valueAt(i)));
                }
                showMat(drawOcrResult(finalBm, blocks));

                imageIdx++;
            }
        });
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_fetch:
//
//                break;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//        return true;
//    }

//    void fetchPhotos() {
//        errHdlr.tryRun(() -> {
//            int idx = 0;
//
//            while (true) {
//                URL website = new URL("http://www.website.com/information.asp");
//                try (InputStream in = website.openStream()) {
//                    FileSystems.getDefault().getPath()
//                    Files.copy(in, new Path());
//                }
//                Zip
//                idx++;
//            }
//        });
//    }
}
