package com.ing.software.ocr.OcrObjects;

import android.graphics.*;
import com.google.android.gms.vision.text.*;
import java.util.List;
import com.annimon.stream.Stream;
import com.ing.software.common.Lazy;

import static com.ing.software.common.CommonUtils.*;
import static java.util.Arrays.*;

public class Block {
    private TextBlock tb;
    private Lazy<List<TextLine>> lines;
    private Lazy<List<PointF>> corners;

    public Block(TextBlock textBlock) {
        tb = textBlock;
        lines = new Lazy<>(() -> Stream.of(tb.getComponents())
                .select(Line.class).map(TextLine::new).toList());
        corners = new Lazy<>(() -> ptsToPtsF(asList(tb.getCornerPoints())));
    }

    public List<TextLine> lines() {
        return lines.get();
    }

    // for OpenCVTestApp
    public List<PointF> corners() {
        return corners.get();
    }

    public Rect box() {
        return tb.getBoundingBox();
    }

    public double area() {
        //todo use getCornerPoints
        return (double)box().width() * box().height();
    }
}
