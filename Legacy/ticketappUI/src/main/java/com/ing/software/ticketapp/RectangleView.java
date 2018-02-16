package com.ing.software.ticketapp;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class RectangleView extends View {

    private Paint frontPaint = null;
    private Paint backPaint = null;
    private List<PointF> corns = null;

    public RectangleView(Context context) {
        super(context);
    }

    public RectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static void draw(Canvas cvs, List<PointF> corns, Paint p) {
        for (int i = 0; i < 4; i++) {
            PointF start = corns.get(i), end = corns.get((i + 1) % 4);
            cvs.drawLine(start.x, start.y, end.x, end.y, p);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (corns != null) {
            if (backPaint != null)
                draw(canvas, corns, backPaint);
            if (frontPaint != null)
                draw(canvas, corns, frontPaint);
        }
    }

    public void setPaint(Paint front, Paint back) {
        frontPaint = front;
        backPaint = back;
        invalidate();
    }

    public void setCorners(List<PointF> corners) {
        corns = corners;
        invalidate();
    }
}
