package com.ing.software.ocr.OcrObjects;


import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.annimon.stream.Stream;

public class Block {
    private TextBlock tb;
    private List<TextLine> childs;
    public Block(TextBlock textBlock) {
        tb = textBlock;
        childs = new ArrayList<>();
        for (Text txt : textBlock.getComponents()) {
            childs.add(new TextLine((Line)txt));
        }
    }

    public List<TextLine> lines() {
        return childs;
    }

    public String lang() {
        return tb.getLanguage();
    }

    public Rect box() {
        return tb.getBoundingBox();
    }

    public double area() {
        //todo use getCornerPoints
        return (double)box().width() * box().height();
    }

    public List<PointF> corners() {
        return Stream.of(tb.getCornerPoints()).map(p -> new PointF(p.x, p.y)).toList();
    }

}
