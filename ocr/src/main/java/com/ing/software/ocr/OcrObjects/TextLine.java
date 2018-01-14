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
    private Lazy<List<PointF>> corners; // corners of a rotated rectangle containing the line of text
    private Lazy<Double> height, width; // width, height of entire line
    private Lazy<Double> charWidth, charHeight; // average width, height of single character
    private Lazy<Double> charAspRatio; // charWidth / charHeight
    private Lazy<String> textNoSpaces; // uppercase text with no spaces between words
    private Lazy<String> textOnlyAlpha; // uppercase text where all non alphabetic characters are removed. No spaces between words
    private Lazy<String> numNoSpaces; // concatenate all words where there was applied a sanitize substitution suitable for detecting a price.
    private Lazy<String> numConcatDot; // same as numNoSpaces, but words are concatenated with a dot

    public TextLine(Line line) {
        this.line = line;
        words = new Lazy<>(() -> Stream.of(line.getComponents())
                .select(Element.class).map(Word::new).toList());
        corners = new Lazy<>(() -> ptsToPtsF(asList(line.getCornerPoints())));
        // width and height are respectively the longest and shortest side of the rotated rectangle
        width =  new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(1)),
                                             dist(corners().get(2), corners().get(3)))));
        height = new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(3)),
                                             dist(corners().get(1), corners().get(2)))));
        // average of individual words char width, weighted on word length.
        charWidth = new Lazy<>(() -> Stream.of(words()).reduce(0., (sum, w) ->
                sum + w.charWidth() * w.length()) / textNoSpaces().length());
        // average of individual words char height, weighted on word length
        charHeight = new Lazy<>(() -> Stream.of(words()).reduce(0., (sum, w) ->
                sum + w.charHeight() * w.length()) / textNoSpaces().length());
        charAspRatio = new Lazy<>(() -> charWidth() / height());
        textNoSpaces = new Lazy<>(() -> Stream.of(words())
                .reduce("", (str, w) -> str + w.textUppercase()));
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

    public float centerX() {
        return line.getBoundingBox().exactCenterX();
    }

    public float centerY() {
        return line.getBoundingBox().exactCenterY();
    }

    // bounding box (which size is always > of (width(), height()) )
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
