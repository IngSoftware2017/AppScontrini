package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;
import com.ing.software.common.Lazy;

import java.util.List;

import static com.ing.software.common.CommonUtils.ptsToPtsF;
import static java.util.Arrays.asList;

public class Word {
    private Element elem;
    private Lazy<List<PointF>> corners;

    public Word(Element element) {
        elem = element;
        corners = new Lazy<>(() -> ptsToPtsF(asList(elem.getCornerPoints())));
    }

    public String text() {
        return elem.getValue();
    }

    // for OpenCVTestApp
    public List<PointF> corners() {
        return corners.get();
    }
}
