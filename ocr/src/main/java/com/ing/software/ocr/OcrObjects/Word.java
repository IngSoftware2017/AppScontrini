package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;
import android.util.Pair;

import com.google.android.gms.vision.text.Element;
import com.ing.software.common.Lazy;

import java.util.List;

import static com.ing.software.common.CommonUtils.dist;
import static com.ing.software.common.CommonUtils.ptsToPtsF;
import static java.util.Arrays.asList;
import static java.util.Collections.*;

/**
 * Object that represents a single text word.
 * This object is immutable.
 * @author Riccardo Zaglia
 */
public class Word {
    // List of substitutions used to correct common ocr mistakes in the reading of a price.
    private static final List<Pair<String, String>> NUM_SANITIZE_LIST = asList(
            new Pair<>("O", "0"),
            new Pair<>("o", "0"),
            new Pair<>("D", "0"),
            new Pair<>("I", "1"),
            new Pair<>("l", "1"), // lowercase L
            new Pair<>("S", "5"),
            new Pair<>("s", "5"),
            new Pair<>(",", "."),
            new Pair<>("U", "0"),
            new Pair<>("B", "8")
    );

    private Element elem;
    private Lazy<List<PointF>> corners; // corners of the rotated rectangle containing this word. Ordered clockwise
    private Lazy<String> textUppecase; // uppercase text
    private Lazy<String> textOnlyAlpha; // uppercase text where all non alphabetic character are removed
    private Lazy<String> textSanitizedNum; // text where it was applied the NUM_SANITIZE_LIST substitutions
    private Lazy<Double> charHeight, charWidth;

    public Word(Element element) {
        elem = element;
        corners = new Lazy<>(() -> ptsToPtsF(asList(elem.getCornerPoints())));
        textUppecase = new Lazy<>(() -> elem.getValue().toUpperCase());

        // length of the longest side of the rotated rectangle, divided by the number of characters of the word
        charWidth = new Lazy<>(() -> max(asList(dist(corners().get(0), corners().get(1)),
                dist(corners().get(2), corners().get(3)))) / elem.getValue().length());
        // length of the shortest side of the rotated rectangle
        charHeight = new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(3)),
                dist(corners().get(1), corners().get(2)))));
        textOnlyAlpha = new Lazy<>(() -> textUppercase().replaceAll("[^A-Z]", ""));
        textSanitizedNum = new Lazy<>(() -> {
            String res = elem.getValue();
            for (Pair<String, String> p : NUM_SANITIZE_LIST)
                res = res.replace(p.first, p.second);
            return/*textSanitizedNum*/ res;
        });
    }

    // for OpenCVTestApp
    public List<PointF> corners() {
        return corners.get();
    }

    public String textUppercase() {
        return textUppecase.get();
    }

    public double charWidth() {
        return charWidth.get();
    }

    public double charHeight() {
        return charHeight.get();
    }

    public double length() {
        return textUppercase().length();
    }

    public String textOnlyAlpha() {
        return textOnlyAlpha.get();
    }

    public String textSanitizedNum() {
        return textSanitizedNum.get();
    }
}
