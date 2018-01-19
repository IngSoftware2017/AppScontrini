package com.ing.software.test.opencvtestapp;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

import com.google.android.gms.vision.text.*;

import com.ing.software.common.*;
import com.ing.software.ocr.*;
import com.ing.software.ocr.OcrObjects.*;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Point;

import static com.ing.software.common.CommonUtils.*;
import static com.ing.software.common.Reflect.*;
import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.*;
import static java.util.Collections.*;


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

    private static final Scalar WHITE = new Scalar(255,255,255, 255);
    private static final Scalar RED = new Scalar(255,0,0, 255);
    private static final Scalar DARK_RED = new Scalar(127,0, 0, 255);
    private static final Scalar ORANGE = new Scalar(255,127, 0, 255);
    private static final Scalar GREEN = new Scalar(0,255,0, 255);
    private static final Scalar DARK_GREEN = new Scalar(0,127,0, 255);
    private static final Scalar BLUE = new Scalar(0,0,255, 255);
    private static final Scalar PURPLE = new Scalar(255,0,255, 255);


    private static final int MSG_TITLE = 0;
    private static final int MSG_IMAGE = 1;
    private static final int MSG_EXCEPTION = 2;

    // bit flags
    private static final int SHOW_ORIGINAL = 1;
    private static final int SHOW_OPENCV = 2;
    private static final int SHOW_OCR = 4;

    private int showFlags = SHOW_ORIGINAL | SHOW_OCR;
    private Semaphore sem = new Semaphore(0);
    private int imgIdx = 0;

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

    private static Mat bitmapToMat(Bitmap bm) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bm, mat);
        return mat;
    }

    /**
     * Show on screen an Android bitmap
     * This method is blocked until user taps screen.
     * @param bm bitmap
     */
    private void show(Bitmap bm) throws Exception {
        hdl.obtainMessage(MSG_IMAGE, bm).sendToTarget();
        sem.acquire();
    }

    /**
     * Show on screen an OpenCV Mat.
     * This method is blocked until user taps screen.
     * @param img Mat of any color
     */
    private void show(Mat img) throws Exception {
        Bitmap bm = invoke(IP_CLASS, "matToBitmap", img);
        show(bm);
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

    static void drawTextLines(Mat img, List<OcrText> lines, Scalar backColor, double fontSize) throws Exception {
        for (OcrText line : lines) {
            polylines(img, pts2matArr(line.corners()), true, RED, LINE_THICK);
            for (OcrText w : line.childs()) {
                fillPoly(img, pts2matArr(w.corners()), backColor);
            }
        }
        for (OcrText line : lines) {
            for (OcrText w : line.childs()) {
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

    void classifyAndShowTexts(Bitmap bm, List<OcrText> lines, double fontSize) throws Exception {
        Mat img = bitmapToMat(bm);

        // first of all, find best amount string and draw strip rect
        List<Scored<OcrText>> amountStrs = invoke(OA_CLASS, "findAllScoredAmountStrings",
                lines, size(bm));
        if (amountStrs.size() != 0) { //Note: size != 0 is more readable than !...isEmpty()
            OcrText amountStr = max(amountStrs).obj();
            RectF amountSripRect = invoke(OA_CLASS, "getAmountStripRect", amountStr, size(bm));
            List<Point> cvPts = invoke(IP_CLASS, "androidPtsToCV", rectToPts(amountSripRect));
            MatOfPoint2f ptsMat = invoke(IP_CLASS, "ptsToMat", cvPts);
            drawPoly(img, ptsMat, GREEN, DEF_THICK);
        }

        //draw elements in order of importance

        // draw all texts
        drawTextLines(img, lines, BLUE, fontSize);

        // draw potential prices
        List<OcrText> potPrices = invoke(OA_CLASS, "findAllPotentialPrices", lines);
        drawTextLines(img, potPrices, ORANGE, fontSize);

        // draw all dates
        List<Pair<OcrText, Date>> dates = invoke(OA_CLASS, "findAllDates", lines);
        drawTextLines(img, Stream.of(dates).map(p -> p.first).toList(), PURPLE, fontSize);

        // draw all amount strings
        drawTextLines(img, Stream.of(amountStrs).map(Scored::obj).toList(), DARK_GREEN, fontSize);

        // draw certain prices
        List<Pair<OcrText, String>> prices = invoke(OA_CLASS, "findAllCertainPrices", lines);
        drawTextLines(img, Stream.of(prices).map(p -> p.first).toList(), DARK_RED, fontSize);

        show(img);
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

        Consumer<Integer> skip = jump -> {
            imgIdx += jump;
            this.setTitle(String.valueOf(imgIdx));
        };
        findViewById(R.id.prev_btn).setOnClickListener(v -> skip.accept(-1));
        findViewById(R.id.next_btn).setOnClickListener(v -> skip.accept(+1));
        findViewById(R.id.prev_fast_btn).setOnClickListener(v -> skip.accept(-10));
        findViewById(R.id.next_fast_btn).setOnClickListener(v -> skip.accept(+10));

        // request permissions
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        new Thread(this::backgroundWork).start();
    }

    /**
     * This method processes all images from the dataset showing every step of the pipeline.
     * show stops the method execution until the screen is tapped.
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

                if (check(showFlags, SHOW_ORIGINAL))
                    show(bm);

                if (check(showFlags, SHOW_OPENCV)) {
                    Mat srcImg = getField(imgProc, "srcImg");
                    double shortSide = getField(IP_CLASS, "SHORT_SIDE");
                    Mat grayResized = invoke(IP_CLASS, "toGrayResized", srcImg, shortSide);
                    Swap<Mat> graySwap = new Swap<>(grayResized.clone(), new Mat());
                    Mat binary = ((Mat)invoke(IP_CLASS, "prepareBinaryImg",
                            graySwap)).clone();
                    show(binary);

                    List<Scored<MatOfPoint>> contours =
                            invoke(IP_CLASS, "findBiggestContours", graySwap, 2);
                    MatOfPoint contour = contours.get(0).obj();

                    graySwap.first = binary;
                    invoke(IP_CLASS, "toEdges", graySwap);
                    invoke(IP_CLASS, "removeBackground", graySwap, contour);
                    show(graySwap.first);

                    MatOfInt4 lines = invoke(IP_CLASS, "houghLines", graySwap);
                    MatOfPoint2f corners = invoke(IP_CLASS, "findPolySimple", contour);
                    // show both contours and hough lines
                    Mat rgbaResized = new Mat();
                    cvtColor(grayResized, rgbaResized, COLOR_GRAY2RGBA);
                    Mat contourImg = drawLines(drawContour(drawContour(rgbaResized, contours.get(1).obj(),
                            ORANGE, DEF_THICK), contour, BLUE, DEF_THICK), lines, PURPLE);
                    drawPoly(contourImg, corners, corners.rows() == 4 ? GREEN : RED, DEF_THICK);
                    show(contourImg);
                }

                if (check(showFlags, SHOW_OCR)) {
                    Bitmap textLinesBm = invoke(imgProc, "undistortForOCR", 1. / 3.);
                    List<OcrText> lines = invoke(OA_CLASS, "runOCR", textLinesBm, ocrEngine);

                    //find amount strings
                    List<Scored<OcrText>> amountStrs = invoke(OA_CLASS, "findAllScoredAmountStrings",
                            lines, size(textLinesBm));

                    // find dates
                    List<Pair<OcrText, Date>> dates = invoke(OA_CLASS, "findAllDates", lines);

                    // draw
                    StringBuilder titleStr = new StringBuilder();
                    titleStr.append(imgIdx);
                    if (dates.size() == 1) {
                        titleStr.append(" - ").append(new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
                                .format(dates.get(0).second));
                    } else {
                        titleStr.append(" - date not found or multiple");
                    }
                    asyncSetTitle(titleStr.toString());
                    classifyAndShowTexts(textLinesBm, lines, FONT_SIZE_DEF);

                    // find amount price
                    if (amountStrs.size() != 0) {
                        OcrText amountStr = max(amountStrs).obj();
                        RectF srcAmountStripRect = invoke(OA_CLASS, "getAmountStripRect",
                                amountStr, size(textLinesBm));
                        Bitmap amountStrip = invoke(OA_CLASS, "getAmountStrip",
                                imgProc, size(textLinesBm), amountStr, srcAmountStripRect);
                        RectF dstAmountStripRect = rectFromSize(size(amountStrip));
                        List<OcrText> amountLinesStripSpace = invoke(OA_CLASS, "runOCR",
                                amountStrip, ocrEngine);
                        List<OcrText> amountLinesBmSpace = Stream.of(amountLinesStripSpace)
                                .map(line -> new OcrText(line, dstAmountStripRect, srcAmountStripRect)).toList();
                        BigDecimal price = invoke(OA_CLASS, "findAmountPrice",
                                amountLinesBmSpace, amountStr, srcAmountStripRect);

                        // draw strip
                        titleStr = new StringBuilder();
                        titleStr.append(imgIdx);
                        if (price != null)
                            titleStr.append(" -  ").append(price);
                        asyncSetTitle(titleStr.toString());
                        classifyAndShowTexts(amountStrip, amountLinesStripSpace, FONT_SIZE_AMOUNT);
                    }
                }

                imgIdx++;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());

        switch (item.getItemId()) {
            case R.id.show_original:
                showFlags = overwriteBits(showFlags, SHOW_ORIGINAL, item.isChecked());
                break;
            case R.id.show_opencv:
                showFlags = overwriteBits(showFlags, SHOW_OPENCV, item.isChecked());
                break;
            case R.id.show_OCR:
                showFlags = overwriteBits(showFlags, SHOW_OCR, item.isChecked());
                break;
            default:
                break;
        }
        return true;
    }
}
