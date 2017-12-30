package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;

import com.google.android.gms.vision.text.Element;
import com.ing.software.common.Lazy;

import java.util.List;

import static com.ing.software.common.CommonUtils.ptsToPtsF;
import static java.util.Arrays.asList;

public class Word {
    private Element elem;
    private Lazy<List<PointF>> corners;
    private Lazy<String> textOnlyAlpha;

    public Word(Element element) {
        elem = element;
        corners = new Lazy<>(() -> ptsToPtsF(asList(elem.getCornerPoints())));
        textOnlyAlpha = new Lazy<>(() -> text().replaceAll("[^A-Z]", ""));
    }

    // for OpenCVTestApp
    public List<PointF> corners() {
        return corners.get();
    }

    public String text() {
        return elem.getValue().toUpperCase();
    }

    public String textOnlyAlpha() {
        return textOnlyAlpha.get();
    }
}
