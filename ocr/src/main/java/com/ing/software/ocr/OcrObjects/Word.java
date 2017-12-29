package com.ing.software.ocr.OcrObjects;

import android.graphics.Point;
import android.graphics.PointF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;

import java.util.Arrays;
import java.util.List;

public class Word {
    Element elem;

    public Word(Element element) {
        elem = element;
    }

    public String text() {
        return elem.getValue();
    }

    public List<PointF> corners() {
        return Stream.of(elem.getCornerPoints()).map(p -> new PointF(p.x, p.y)).toList();
    }
}
