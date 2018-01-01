package com.ing.software.ocr.OcrObjects;

import com.ing.software.common.Lazy;
import android.graphics.PointF;
import android.graphics.RectF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import java.util.List;

import static com.ing.software.common.CommonUtils.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

public class TextLine {
    private Line line;
    private Lazy<List<Word>> words;
    private Lazy<List<PointF>> corners;
    private Lazy<Double> height, width, charWidth, charAspRatio;
    private Lazy<String> textNoSpaces, textOnlyAlpha, textSanitizedNum;

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
        charAspRatio = new Lazy<>(() -> charWidth() / height());
        textNoSpaces = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.text()));
        textOnlyAlpha = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.textOnlyAlpha()));
        textSanitizedNum = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.textSanitizedNum()));
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

    public double charAspectRatio() {
        return charAspRatio.get();
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

    public String textSanitizedNum() {
        return textSanitizedNum.get();
    }
}
