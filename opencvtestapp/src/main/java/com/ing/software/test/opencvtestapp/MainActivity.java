package com.ing.software.test.opencvtestapp;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Semaphore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.util.SizeF;
import android.widget.*;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.*;

import com.ing.software.common.*;
import com.ing.software.ocr.*;
import com.ing.software.ocr.OcrObjects.*;

import org.opencv.core.*;
import org.opencv.core.Point;

import static com.ing.software.common.CommonUtils.size;
import static com.ing.software.common.Reflect.*;
import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.*;
import static java.util.Collections.*;
import static java.lang.String.*;


/**
 * This app shows in order the pipeline steps to find the ticket rectangle
 */
public class MainActivity extends AppCompatActivity {

    private static final String folder = "TestOCR";
    private static final Class<?> IP_CLASS = ImageProcessor.class; // alias
    private static final Class<?> OA_CLASS = OcrAnalyzer.class; // alias
    private static final int DEF_THICK = 20;
    private static final int LINE_THICK = 6;
    private static final double FONT_SIZE = 0.6;

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
            hdl.obtainMessage(MSG_EXCEPTION, e.toString() + "\n" + e.getMessage()).sendToTarget()
    );

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
/*
    static void drawTextLines(Mat img, List<TextLine> lines, Scalar wordBgColor) throws Exception {
        for (TextLine line : lines) {
            polylines(img, pts2mat(line.corners()), true, RED, LINE_THICK);
            for (Word w : line.words()) {
                fillPoly(img, pts2mat(w.corners()), wordBgColor);
            }
        }
        for (TextLine line : lines) {
            for (Word w : line.words()) {
                PointF blPt = w.corners().get(3); // bottom-right point (clockwise from top-left)
                putText(img, w.text().toUpperCase(), new Point(blPt.x + 2, blPt.y - 2),
                        FONT_HERSHEY_SIMPLEX, FONT_SIZE, WHITE);
            }
        }
    }
*/
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
            imageIdx--;
            this.setTitle(String.valueOf(imageIdx));
        });
        findViewById(R.id.succ_btn).setOnClickListener(v -> {
            imageIdx++;
            this.setTitle(String.valueOf(imageIdx));
        });
        ((Switch)findViewById(R.id.result_only))
                .setOnCheckedChangeListener((v, checked) -> resultOnly = checked);

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
                    // show both contours and hough lines
                    showMat(drawLines(drawContour(rgbaResized, contour, BLUE), lines, PURPLE));

                    MatOfPoint2f cornersMat = invoke(IP_CLASS, "findPolySimple", contour);
                    cornersMat = invoke(IP_CLASS, "scale", cornersMat, rgbaResized.size(), srcImg.size());
                    List<PointF> corners = invoke(IP_CLASS, "cvPtsToAndroid", cornersMat.toList());
                    showBitmap(drawPoly(bm, corners, corners.size() == 4 ? Color.GREEN : Color.RED, DEF_THICK));
                }

                imgProc.findTicket(false);
                Bitmap textLinesBm = invoke(imgProc, "undistortForOCR");
/*
                //find amount
                List<TextLine> lines = invoke(OA_CLASS, "bitmapToLines", textLinesBm, ocrEngine);
                TextLine amountStr = invoke(OA_CLASS, "findAmountString", lines, size(textLinesBm));
                String titleStr;

//                List<Pair<String, TextLine>> dateResults = invoke(OcrAnalyzer.class,
//                        "findAllDateStrings", lines);
//                drawTextLines(mat, Stream.of(dateResults).map(p -> p.second).toList(), PURPLE);
//                if (dateResults.size() == 1)
//                    titleStr += " - " + dateResults.get(0).first;
//                else if (dateResults.size() > 1) {
//                    titleStr += " - multiple date matches";
//                }
                Mat mat = new Mat();
                bitmapToMat(textLinesBm, mat);
                drawTextLines(mat, lines, BLUE);
                if (amountStr != null)
                    drawTextLines(mat, singletonList(amountStr), DARK_GREEN);
                //asyncSetTitle(titleStr);
                showMat(mat);

                if (amountStr != null) {
                    RectF srcStripRect = invoke(OA_CLASS, "getAmountStripRect", amountStr, size(textLinesBm));
                    Bitmap amountStrip = invoke(OA_CLASS, "getAmountStrip", imgProc, amountStr, srcStripRect);
                    List<TextLine> amountLines =invoke(OA_CLASS, "bitmapToLines", amountStrip, ocrEngine);
                    BigDecimal price = invoke(OA_CLASS, "findAmountPrice",
                            amountLines, amountStr, size(srcStripRect), size(amountStrip));

                    titleStr = valueOf(imageIdx);
                    if (price != null)
                        titleStr += " -  " + price.toString();
                    asyncSetTitle(titleStr);
                    bitmapToMat(amountStrip, mat);
                    drawTextLines(mat, amountLines, BLUE);
                    showMat(mat);
                }
*/
                imageIdx++;
            }
        });
    }
}
