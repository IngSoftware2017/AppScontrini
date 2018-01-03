package com.ing.software.ocr.OcrObjects;

import com.ing.software.common.Lazy;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.SizeF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import java.util.List;

import static com.ing.software.common.CommonUtils.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

/**
 * Object that represents a line of text, i.e. a collection of words.
 * This object is immutable.
 * @author Riccardo Zaglia
 */
public class TextLine {
    private Line line;
    private Lazy<List<Word>> words;
    private Lazy<List<PointF>> corners;
    private Lazy<Double> height, width, charWidth, charHeight, charAspRatio;
    private Lazy<String> textNoSpaces, textOnlyAlpha;
    private Lazy<String> numNoSpaces, numConcatDot;

    public TextLine(Line line) {
        this.line = line;
        words = new Lazy<>(() -> Stream.of(line.getComponents())
                .select(Element.class).map(Word::new).toList());
        corners = new Lazy<>(() -> ptsToPtsF(asList(line.getCornerPoints())));
        width =  new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(1)),
                                             dist(corners().get(2), corners().get(3)))));
        height = new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(3)),
                                             dist(corners().get(1), corners().get(2)))));
        charWidth = new Lazy<>(() -> width() / text().length());
        // average of individual words char height
        charHeight = new Lazy<>(() -> Stream.of(words()).reduce(0., (sum, w) ->
                sum + w.charHeight() * w.length()) / textNoSpaces().length());
        charAspRatio = new Lazy<>(() -> charWidth() / height());
        textNoSpaces = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.text()));
        textOnlyAlpha = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.textOnlyAlpha()));
        numNoSpaces = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.textSanitizedNum()));
        numConcatDot = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + "." + w.textSanitizedNum()));
    }

    public List<Word> words() {
        return words.get();
    }

    // for OpenCVTestApp
    public List<PointF> corners() {
        return corners.get();
    }

    public double width() {
        return width.get();
    }

    public double height() {
        return height.get();
    }

    public double area() {
        return width() * height();
    }

    public double charWidth() {
        return charWidth.get();
    }

    public double charHeight() {
        return charHeight.get();
    }

    public double charAspectRatio() {
        return charAspRatio.get();
    }

    public SizeF charSize() {
        return new SizeF((float)charWidth(), (float)height());
    }

    public double centerX() {
        return line.getBoundingBox().exactCenterX();
    }

    public double centerY() {
        return line.getBoundingBox().exactCenterY();
    }

    public RectF box() {
        return new RectF(line.getBoundingBox());
    }

    public String text() {
        return line.getValue();
    }

    public String textNoSpaces() {
        return textNoSpaces.get();
    }

    public String textOnlyAlpha() {
        return textOnlyAlpha.get();
    }

    public String numNoSpaces() {
        return numNoSpaces.get();
    }
    public String numConcatDot() {
        return numConcatDot.get();
    }

}
