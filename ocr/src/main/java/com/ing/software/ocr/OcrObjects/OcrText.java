package com.ing.software.ocr.OcrObjects;

import com.google.android.gms.vision.text.Text;
import com.ing.software.common.Lazy;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Pair;
import android.util.SizeF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;

import java.util.List;

import static com.ing.software.common.CommonUtils.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

/**
 * Object that represents a block (a collection of lines of text), a line of text (a collection of words) or a word.
 * This object is immutable.
 * @author Riccardo Zaglia
 */
public class OcrText {
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

    private boolean isWord;
    private Lazy<List<OcrText>> childs;
    private Lazy<List<PointF>> corners; // corners of a rotated rectangle containing the line of text
    private Lazy<Float> height, width; // width, height of entire line
    private Lazy<Float> charWidth, charHeight; // average width, height of single character
    private Lazy<Float> charAspRatio; // charWidth / charHeight
    private Lazy<RectF> box;
    private String text;
    private Lazy<String> textUppercase; // uppercase text
    private Lazy<String> textOnlyAlpha; // uppercase text where all non alphabetic character are removed. No spaces between words
    private Lazy<String> textNoSpaces; // uppercase text with no spaces between words
    private Lazy<String> textSanitizedNum; // text where it was applied the NUM_SANITIZE_LIST substitutions
    private Lazy<String> numNoSpaces; // concatenate all words where there was applied a sanitize substitution suitable for detecting a price.
    private Lazy<String> numConcatDot; // same as numNoSpaces, but words are concatenated with a dot

    public OcrText(Text text) {
        isWord = text instanceof Element;
        childs = new Lazy<>(() -> Stream.of(text.getComponents()).map(OcrText::new).toList());
        corners = new Lazy<>(() -> ptsToPtsF(asList(text.getCornerPoints())));

        // width and height are respectively the longest and shortest side of the rotated rectangle
        width =  new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(1)),
                                             dist(corners().get(2), corners().get(3)))));
        height = new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(3)),
                                             dist(corners().get(1), corners().get(2)))));

        charAspRatio = new Lazy<>(() -> charWidth() / height());
        box = new Lazy<>(() -> new RectF(text.getBoundingBox()));

        this.text = text.getValue();
        textUppercase = new Lazy<>(() -> text().toUpperCase());
        textSanitizedNum = new Lazy<>(() -> {
            String res = text();
            for (Pair<String, String> p : NUM_SANITIZE_LIST)
                res = res.replace(p.first, p.second);
            return/*textSanitizedNum*/ res;
        });

        if (isWord) {
            // length of the longest side of the rotated rectangle, divided by the number of characters of the word
            charWidth = new Lazy<>(() -> max(asList(dist(corners().get(0), corners().get(1)),
                    dist(corners().get(2), corners().get(3)))) / text().length());

            // length of the shortest side of the rotated rectangle
            charHeight = new Lazy<>(() -> min(asList(dist(corners().get(0), corners().get(3)),
                    dist(corners().get(1), corners().get(2)))));

            textOnlyAlpha = new Lazy<>(() -> textUppercase().replaceAll("[^A-Z]", ""));
        } else {
            // average of individual words char width, weighted on word length.
            // I do not use directly line height because sometimes it's greater than the actual font height
            charWidth = new Lazy<>(() -> Stream.of(childs()).reduce(0f, (sum, c) ->
                    sum + c.charWidth() * c.length()) / textNoSpaces().length());

            // average of individual words char height, weighted on word length
            charHeight = new Lazy<>(() -> Stream.of(childs()).reduce(0f, (sum, c) ->
                    sum + c.charHeight() * c.length()) / textNoSpaces().length());

            textOnlyAlpha = new Lazy<>(() -> Stream.of(childs())
                    .reduce("", (str, c) -> str + c.textOnlyAlpha()));
        }

        // I used .reduce() to concatenate every result of the lambda expression
        // if these are accessed on a word, they return empty string
        textNoSpaces = new Lazy<>(() -> Stream.of(childs())
                .reduce("", (str, c) -> str + c.textUppercase()));
        numNoSpaces = new Lazy<>(() -> Stream.of(childs())
                .reduce("", (str, c) -> str + c.textSanitizedNum()));
        numConcatDot = new Lazy<>(() -> Stream.of(childs())
                .reduce("", (str, c) -> str + "." + c.textSanitizedNum()));
    }

    /**
     * Create a new OcrText with dimensions converted from one bitmap space to another
     * @param tl OcrText to convert
     * @param srcImgRect source bitmap space. Eg: optimized amount strip, from (0, 0)
     * @param dstImgRect destination space. Eg: amount strip in original bitmap space.
     */
    public OcrText(OcrText tl, RectF srcImgRect, RectF dstImgRect) {
        isWord = tl.isWord;
        //apply bitmap space transformation for each word in tl
        childs = new Lazy<>(() -> Stream.of(tl.childs()).map(c -> new OcrText(c, srcImgRect, dstImgRect)).toList());
        corners = new Lazy<>(() -> transform(tl.corners(), srcImgRect, dstImgRect));

        // to calculate the width and height I should use the transformed corners, but this would lead to
        // a distorted rectangle that would not give a proper width and height.
        width = new Lazy<>(() -> transform(tl.width(), srcImgRect.width(), srcImgRect.width()));
        width = new Lazy<>(() -> transform(tl.height(), srcImgRect.height(), srcImgRect.height()));
        charWidth = new Lazy<>(() -> transform(tl.charWidth(), srcImgRect.width(), srcImgRect.width()));
        charHeight = new Lazy<>(() -> transform(tl.charHeight(), srcImgRect.height(), srcImgRect.height()));
        charAspRatio = new Lazy<>(() -> charWidth() / height());
        box = new Lazy<>(() -> transform(tl.box(), srcImgRect, dstImgRect));

        // the text fields remain unchanged
        text = tl.text;
        textUppercase = tl.textUppercase;
        textNoSpaces = tl.textNoSpaces;
        textOnlyAlpha = tl.textOnlyAlpha;
        numNoSpaces = tl.numNoSpaces;
        numConcatDot = tl.numConcatDot;
    }

    public boolean isWord() { return isWord; }
    public List<OcrText> childs() { return childs.get(); }
    public float length() { return text.length(); }

    // rectangle properties
    public List<PointF> corners() { return corners.get(); }
    public float width() { return width.get(); }
    public float height() { return height.get(); }
    public float area() { return width() * height(); }
    public RectF box() { return box.get(); } // bounding box size is always >= of (width(), height())
    public float centerX() { return box().centerX(); }
    public float centerY() { return box().centerY(); }

    //character size
    public float charWidth() { return charWidth.get(); }
    public float charHeight() { return charHeight.get(); }
    public float charAspectRatio() { return charAspRatio.get(); }
    public SizeF charSize() { return new SizeF(charWidth(), height()); }

    // string properties
    public String text() { return text; }
    public String textSanitizedNum() { return textSanitizedNum.get(); }
    public String textUppercase() { return textUppercase.get(); }
    public String textOnlyAlpha() { return textOnlyAlpha.get(); }
    // available only if isWord() == false:
    public String textNoSpaces() { return textNoSpaces.get(); }
    public String numNoSpaces() { return numNoSpaces.get(); }
    public String numConcatDot() { return numConcatDot.get(); }

}
