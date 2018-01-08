package com.ing.software.ticketapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.ing.software.common.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAccumulator;

import com.annimon.stream.Stream;
import com.ing.software.common.Scored;

import database.DataManager;
import database.TicketEntity;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.lang.Math.*;

/**
 * @author Riccardo Zaglia
 */
public class CropActivity extends AppCompatActivity {

    private static PointF pointToCanvasSpace(PointF pt, Drawable image, Canvas cvs) {
        float photoRatio = (float)image.getIntrinsicWidth() / image.getIntrinsicHeight();
        float width = (float)cvs.getWidth(), height = (float)cvs.getHeight();
        float cvsRatio = width / height;
        return photoRatio < cvsRatio ?
                new PointF(width / 2f + (pt.x - 0.5f) * height * photoRatio, height * pt.y) :
                new PointF(width * pt. x, height / 2f + (pt.y - 0.5f) * width / photoRatio);
    }

//    private static List<PointF> normalizedPoints(List<PointF> pts, Drawable image, Canvas cvs) {
//
//    }

    private static void setImageBounds(Drawable image, Canvas cvs) {
        PointF topLeft = pointToCanvasSpace(new PointF(0,0), image, cvs);
        PointF bottomright = pointToCanvasSpace(new PointF(1,1), image, cvs);
        image.setBounds((int)topLeft.x, (int)topLeft.y, (int)bottomright.x, (int)bottomright.y);
    }

    private void draw(Canvas cvs, Drawable image, List<PointF> corns, int colorPrimary) {
        image.draw(cvs);

        Paint p1 = new Paint();
        p1.setColor(Color.WHITE);
        p1.setStrokeWidth(14);
        Paint p2 = new Paint();
        p2.setColor(colorPrimary);
        p2.setStrokeWidth(10);

        int cornsTot = corns.size();
        for (Paint p : asList(p1, p2)) {
            for (int i = 0; i < cornsTot; i++) {
                PointF start = corns.get(i), end = corns.get((i + 1) % cornsTot);
                cvs.drawLine(start.x, start.y, end.x, end.y, p);
            }
        }
    }

    SurfaceView cropView;

    DataManager DB = DataManager.getInstance(this);

    TicketEntity te = null;
    int dragIdx = -1;
    PointF offset = new PointF();

    int imageRotation = 0;

    List<PointF> pts = new ArrayList<>();
    boolean validHldr = false;
    List<PointF> cvsPts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        long id = getIntent().getLongExtra("ID", 0);
        if (id != 0) {
            te = DB.getTicket(id);
            pts = te.getCornerPoints();
            cropView = findViewById(R.id.crop_view);
            int colorPrimary = getResources().getColor(R.color.colorPrimary);
            Glide.with(this).load(te.getFileUri()).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    cropView.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder sh) {
                            validHldr = true;
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder sh, int i, int i1, int i2) {
                            Canvas cvs = sh.lockCanvas();
                            cvsPts = Stream.of(pts).map(p -> pointToCanvasSpace(p, resource, cvs)).toList();
                            setImageBounds(resource, cvs);
                            sh.unlockCanvasAndPost(cvs);
                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder sh) {
                            validHldr = false;
                        }
                    });

                    //noinspection AndroidLintClickableViewAccessibility
                    cropView.setOnTouchListener((view, e) -> {
                        if (e.getAction() == MotionEvent.ACTION_DOWN) {
                            dragIdx = min(Stream.of(cvsPts).mapIndexed((i, p) ->
                                    new Scored<>(sqrt(pow(p.x - e.getX(), 2.) +
                                            pow(p.y - e.getY(), 2.)), i)).toList()).obj();
                            offset = new PointF(cvsPts.get(dragIdx).x - e.getX(),
                                    cvsPts.get(dragIdx).y - e.getY());
                        } else if (validHldr && e.getAction() == MotionEvent.ACTION_MOVE) {
                            Canvas cvs = cropView.getHolder().lockCanvas();
                            cvsPts.set(dragIdx, new PointF(e.getX() + offset.x, e.getY() + offset.y));
                            draw(cvs, resource, cvsPts, colorPrimary);
                            cropView.getHolder().unlockCanvasAndPost(cvs);
                        }
                        return true;
                    });
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.crop_accept) {
            if (te != null) {
                // rotate points
                List<PointF> rotatedPts = new ArrayList<>(pts);
//                rotate(rotatedPts, -min(Stream.of(pts).mapIndexed((i, p) ->
//                        new Scored<>(p.x + p.y, i)).toList()).obj());
                te.setCornerPoints(rotatedPts);
                DB.updateTicket(te);
            }
            finish();
        }
        return true;
    }
}
