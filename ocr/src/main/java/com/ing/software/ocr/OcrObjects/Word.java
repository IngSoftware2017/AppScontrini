package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;
import android.util.Pair;

import com.google.android.gms.vision.text.Element;
import com.ing.software.common.Lazy;

import java.util.List;

import static com.ing.software.common.CommonUtils.ptsToPtsF;
import static java.util.Arrays.asList;

public class Word {
    private static List<Pair<String, String>> NUM_SANITIZE_LIST = asList(
            new Pair<>("O", "0"),
            new Pair<>("o", "0"),
            new Pair<>("D", "0"),
            new Pair<>("I", "1"),
            new Pair<>("l", "1"),
            new Pair<>("S", "5"),
            new Pair<>("s", "5")
    );


    private Element elem;
    private Lazy<List<PointF>> corners;
    private Lazy<String> textOnlyAlpha;
    private Lazy<String> textSanitizedNum;

    public Word(Element element) {
        elem = element;
        corners = new Lazy<>(() -> ptsToPtsF(asList(elem.getCornerPoints())));
        textOnlyAlpha = new Lazy<>(() -> text().replaceAll("[^A-Z]", ""));
        textSanitizedNum = new Lazy<>(() -> {
            String res = elem.getValue();
            for (Pair<String, String> p : NUM_SANITIZE_LIST)
                res = res.replace(p.first, p.second);
            return res;
        });
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

    public String textSanitizedNum() {
        return textSanitizedNum.get();
    }
}
