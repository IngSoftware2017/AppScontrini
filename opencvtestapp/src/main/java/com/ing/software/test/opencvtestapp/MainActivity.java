package com.ing.software.test.opencvtestapp;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;

import com.google.android.gms.vision.text.*;

import com.ing.software.common.*;
import com.ing.software.ocr.*;
import com.ing.software.ocr.OcrObjects.*;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Point;

import static com.ing.software.common.CommonUtils.rectToPts;
import static com.ing.software.common.CommonUtils.size;
import static com.ing.software.common.Reflect.*;
import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.*;
import static java.util.Collections.*;
import static java.lang.Math.*;


/**
 * This app shows in order the pipeline steps to find the ticket rectangle
 */
public class MainActivity extends AppCompatActivity {

    private static final String folder = "TestOCR";
    private static final Class<?> IP_CLASS = ImageProcessor.class; // alias
    private static final Class<?> OA_CLASS = OcrAnalyzer.class; // alias
    private static final int DEF_THICK = 6;
    private static final int LINE_THICK = 6;
    private static final double FONT_SIZE_DEF = 0.6;
    private static final double FONT_SIZE_AMOUNT = 2.;
    private static final int FONT_THICKNESS = 2;

    private static final Scalar BLUE = new Scalar(0,0,255, 255);
    private static final Scalar PURPLE = new Scalar(255,0,255, 255);
    private static final Scalar RED = new Scalar(255,0,0, 255);
    private static final Scalar GREEN = new Scalar(0,255,0, 255);
    private static final Scalar DARK_GREEN = new Scalar(0,127,0, 255);
    private static final Scalar WHITE = new Scalar(255,255,255, 255);

    private static final int MSG_TITLE = 0;
    private static final int MSG_IMAGE = 1;
    private static final int MSG_EXCEPTION = 2;

    private Semaphore sem = new Semaphore(0);
    private int imgIdx = 0;
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
            hdl.obtainMessage(MSG_EXCEPTION, e.toString() + "\n" + e.getMessage()).sendToTarget()
    );

    /**
     * Change appbar text
     * @param str title text
     */
    private void asyncSetTitle(String str) {
        hdl.obtainMessage(MSG_TITLE, str).sendToTarget();
    }

    private Mat bitmapToMat(Bitmap bm) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bm, mat);
        return mat;
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
    static Mat drawContour(Mat rgba, MatOfPoint contour, Scalar color, int thickness) {
        Mat img = rgba.clone();
        List<MatOfPoint> ctrList = Collections.singletonList(contour);
        drawContours(img, ctrList, 0, color, thickness);
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

    static void drawPoly(Mat img, MatOfPoint2f pts, Scalar color, int thick) {
        polylines(img, singletonList(new MatOfPoint(pts.toArray())), true, color, thick);
    }

    @NonNull
    static List<MatOfPoint> pts2matArr(List<PointF> pts) throws Exception {
        List<Point> cvPts = invoke(IP_CLASS, "androidPtsToCV", pts);
        return singletonList(new MatOfPoint(cvPts.toArray(new Point[4])));
    }

    static void drawTextLines(Mat img, List<TextLine> lines, Scalar backColor, double fontSize) throws Exception {
        for (TextLine line : lines) {
            polylines(img, pts2matArr(line.corners()), true, RED, LINE_THICK);
            for (Word w : line.words()) {
                fillPoly(img, pts2matArr(w.corners()), backColor);
            }
        }
        for (TextLine line : lines) {
            for (Word w : line.words()) {
                PointF blPt = w.corners().get(3); // bottom-right point (clockwise from top-left)
                putText(img, w.textUppercase(), new Point(blPt.x + 3, blPt.y - 3), // add some padding (3, -3)
                        FONT_HERSHEY_SIMPLEX, fontSize, WHITE, FONT_THICKNESS);
            }
        }
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

    /**
     * Get number of images in the dataset
     * @return number of images
     */
    static int getImgsTot() {
        return new File(Environment.getExternalStorageDirectory().toString() + "/" + folder)
                .listFiles().length;
    }

    /**
     * Get i-th image in the dataset
     * @param idx index
     * @return bitmap
     * @throws FileNotFoundException bitmap not found
     */
    static Bitmap getBitmap(int idx) throws FileNotFoundException {
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
            imgIdx--;
            this.setTitle(String.valueOf(imgIdx));
        });
        findViewById(R.id.succ_btn).setOnClickListener(v -> {
            imgIdx++;
            this.setTitle(String.valueOf(imgIdx));
        });
        ((Switch)findViewById(R.id.result_only))
                .setOnCheckedChangeListener((v, checked) -> resultOnly = checked);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
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

                if (imgIdx < 0)
                    imgIdx = 0;
                else if(imgIdx >= imgsTot)
                    imgIdx = imgsTot - 1;
                asyncSetTitle(String.valueOf(imgIdx));

                Bitmap bm = getBitmap(imgIdx);
                ImageProcessor imgProc = new ImageProcessor(bm);

                if (!resultOnly) {
                    Mat srcImg = getField(imgProc, "srcImg");
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
                    MatOfPoint2f corners = invoke(IP_CLASS, "findPolySimple", contour);
                    // show both contours and hough lines
                    Mat contourImg = drawLines(drawContour(rgbaResized, contour, BLUE, DEF_THICK),
                            lines, PURPLE);
                    drawPoly(contourImg, corners, corners.rows() == 4 ? GREEN : RED, DEF_THICK);
                    showMat(contourImg);

                }

                Bitmap textLinesBm = invoke(imgProc, "undistortForOCR", 1. / 3.);

                //find amount
                List<TextLine> lines = invoke(OA_CLASS, "bitmapToLines", textLinesBm, ocrEngine);
                TextLine amountStr = invoke(OA_CLASS, "findAmountString", lines, size(textLinesBm));
                StringBuilder titleStr = new StringBuilder();
                titleStr.append(imgIdx);

                // find date
                Date date = invoke(OcrAnalyzer.class, "findDate", lines);
                if (date != null) {
                    titleStr.append(" - ").append(
                            new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY).format(date));
                } else {
                    titleStr.append(" - date not found or multiple");
                }
                Mat mat = bitmapToMat(textLinesBm);

                //show all TextLines, amount string and amount strip rect
                if (amountStr != null) {
                    RectF amountSripRect = invoke(OA_CLASS, "getAmountStripRect",
                            amountStr, size(textLinesBm));
                    List<Point> cvPts = invoke(IP_CLASS, "androidPtsToCV",
                            rectToPts(amountSripRect));
                    MatOfPoint2f ptsMat = invoke(IP_CLASS, "ptsToMat", cvPts);
                    drawPoly(mat, ptsMat, BLUE, DEF_THICK);
                }
                drawTextLines(mat, lines, BLUE, FONT_SIZE_DEF);
                if (amountStr != null) {
                    drawTextLines(mat, singletonList(amountStr), DARK_GREEN, FONT_SIZE_DEF);
                }
                asyncSetTitle(titleStr.toString());
                showMat(mat);

                // find amount price
                if (amountStr != null) {
                    RectF srcStripRect = invoke(OA_CLASS, "getAmountStripRect",
                            amountStr, size(textLinesBm));
                    Bitmap amountStrip = invoke(OA_CLASS, "getAmountStrip",
                            imgProc, size(textLinesBm), amountStr, srcStripRect);
                    List<TextLine> amountLines = invoke(OA_CLASS, "bitmapToLines",
                            amountStrip, ocrEngine);
                    BigDecimal price = invoke(OA_CLASS, "findAmountPrice",
                            amountLines, amountStr, size(srcStripRect), size(amountStrip));

                    titleStr = new StringBuilder();
                    titleStr.append(imgIdx);
                    if (price != null)
                        titleStr.append(" -  ").append(price);
                    asyncSetTitle(titleStr.toString());
                    mat = bitmapToMat(amountStrip);
                    drawTextLines(mat, amountLines, BLUE, FONT_SIZE_AMOUNT);
                    showMat(mat);
                }

                imgIdx++;
            }
        });
    }
}
