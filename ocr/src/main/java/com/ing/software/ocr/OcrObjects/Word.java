package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;
import android.util.Pair;

import com.google.android.gms.vision.text.Element;
import com.ing.software.common.Lazy;

import java.util.List;

import static com.ing.software.common.CommonUtils.dist;
import static com.ing.software.common.CommonUtils.ptsToPtsF;
import static java.util.Arrays.asList;
import static java.util.Collections.min;

/**
 * Object that represents a single text word.
 * This object is immutable.
 * @author Riccardo Zaglia
 */
public class Word {
    private static List<Pair<String, String>> NUM_SANITIZE_LIST = asList(
            new Pair<>("O", "0"),
            new Pair<>("o", "0"),
            new Pair<>("D", "0"),
            new Pair<>("I", "1"),
            new Pair<>("l", "1"), // lowercase L
            new Pair<>("S", "5"),
            new Pair<>("s", "5"),
            new Pair<>(",", "."),

            new Pair<>("U", "0") // <- use or not?
    );

    private Element elem;
    private Lazy<List<PointF>> corners;
    private Lazy<String> textOnlyAlpha;
    private Lazy<String> textSanitizedNum;
    private Lazy<Double> charHeight;

    public Word(Element element) {
        elem = element;
        corners = new Lazy<>(() -> ptsToPtsF(asList(elem.getCornerPoints())));
        charHeight = new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(3)),
                dist(corners().get(1), corners().get(2)))));
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

    public double charHeight() {
        return charHeight.get();
    }

    public double length() {
        return text().length();
    }

    public String textOnlyAlpha() {
        return textOnlyAlpha.get();
    }

    public String textSanitizedNum() {
        return textSanitizedNum.get();
    }
}
