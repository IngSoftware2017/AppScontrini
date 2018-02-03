package com.ing.software.ticketapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SizeF;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.annimon.stream.Stream;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ing.software.common.Scored;

import database.DataManager;
import database.TicketEntity;

import static java.util.Collections.*;
import static java.lang.Math.*;
import static android.view.MotionEvent.*;

/**
 * @author Riccardo Zaglia
 */
public class CropActivity extends AppCompatActivity {

    private static final String BUNDLE_PTS = "pts";

    private static final int FRONT_LINE_COLOR = Color.BLACK;
    private static final int BACK_LINE_COLOR = Color.WHITE;
    private static final int FRONT_LINE_WIDTH = 13;
    private static final int BACK_LINE_WIDTH = 15;

    private static SizeF size(Canvas cvs) {
        return new SizeF(cvs.getWidth(), cvs.getHeight());
    }

    private static SizeF size(Drawable img) {
        return new SizeF(img.getIntrinsicWidth(), img.getIntrinsicHeight());
    }

    private static List<PointF> pointToCanvasSpace(List<PointF> pts, SizeF imgSize, SizeF cvsSize) {
        float photoRatio = imgSize.getWidth() / imgSize.getHeight();
        float width = cvsSize.getWidth(), height = cvsSize.getHeight();
        return Stream.of(pts).map(p -> photoRatio < width / height ?
                new PointF(width / 2f + (p.x - 0.5f) * height * photoRatio, height * p.y) :
                new PointF(width * p.x, height / 2f + (p.y - 0.5f) * width / photoRatio)).toList();
    }

    private static List<PointF> normalizedPoints(List<PointF> pts, SizeF imgSize, SizeF cvsSize) {
        float photoRatio = imgSize.getWidth() / imgSize.getHeight();
        float width = cvsSize.getWidth(), height = cvsSize.getHeight();
        return Stream.of(pts).map(p -> photoRatio < width / height ?
                new PointF(0.5f + (p.x - width / 2f) / height / photoRatio, p.y / height) :
                new PointF(p.x / width, 0.5f + (p.y - height / 2f) / width * photoRatio)).toList();
    }

    ImageView imgView;
    RectangleView rectView;

    DataManager DB;

    TicketEntity te = null;
    int dragIdx = -1;
    PointF offset = new PointF();

    int imageRotation = 0;

    List<PointF> normPts = null;
    List<PointF> cvsPts = null;

    SizeF imgSize = null;
    SizeF cvsSize = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        DB = DataManager.getInstance(this.getApplicationContext());
        setTitle("");
        long id = getIntent().getLongExtra("ID", 0);
        imgView = findViewById(R.id.img_view);
        rectView = findViewById(R.id.rectView);
        if (id != 0) {
            te = DB.getTicket(id);
            if (savedInstanceState == null)
                normPts = te.getCornerPoints();
            else
                normPts = savedInstanceState.getParcelableArrayList(BUNDLE_PTS);

            Paint front = new Paint();
            front.setColor(FRONT_LINE_COLOR);
            front.setStrokeWidth(FRONT_LINE_WIDTH);
            Paint back = new Paint();
            back.setColor(BACK_LINE_COLOR);
            back.setStrokeWidth(BACK_LINE_WIDTH);
            rectView.setPaint(front, back);

            rectView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                Glide.with(this).load(te.getFileUri()).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        imgView.setImageDrawable(resource);
                        imgSize = new SizeF(resource.getIntrinsicWidth(), resource.getIntrinsicHeight());

                        cvsSize = new SizeF(rectView.getWidth(), rectView.getHeight());
                        calcCvsPoints();
                        rectView.setCorners(cvsPts);
                    }
                });
            });


            //noinspection AndroidLintClickableViewAccessibility
            rectView.setOnTouchListener((view, e) -> {
                int act = e.getAction();
                if (act == ACTION_DOWN) {
                    dragIdx = min(Stream.of(cvsPts).mapIndexed((i, p) -> new Scored<>(
                            sqrt(pow(p.x - e.getX(), 2.) + pow(p.y - e.getY(), 2.)), i)).toList()).obj();
                    offset = new PointF(cvsPts.get(dragIdx).x - e.getX(),
                            cvsPts.get(dragIdx).y - e.getY());
                } else if (act == ACTION_MOVE) {
                    cvsPts.set(dragIdx, new PointF(e.getX() + offset.x, e.getY() + offset.y));
                    rectView.setCorners(cvsPts);
                } else if (act == ACTION_UP)
                    normPts = normalizedPoints(cvsPts, imgSize, cvsSize);
                return true;
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_PTS, new ArrayList<>(normPts));
    }

    public void calcCvsPoints() {
        cvsPts = pointToCanvasSpace(normPts, imgSize, cvsSize);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_crop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crop_accept:
                if (normPts != null) {
                    // rotate points
                    List<PointF> rotatedPts = new ArrayList<>(normPts);
//                rotate(rotatedPts, -min(Stream.of(pts).mapIndexed((i, p) ->
//                        new Scored<>(p.x + p.y, i)).toList()).obj());
                    te.setCornerPoints(rotatedPts);
                    DB.updateTicket(te);
                }
                onBackPressed();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
