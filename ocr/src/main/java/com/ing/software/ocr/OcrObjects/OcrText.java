package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.util.SizeF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Text;
import com.ing.software.common.Lazy;
import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.common.CommonUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;

/**
 * @author Zaglia
 * @author (Edit) Michelon
 * Main object used by this module. It represents a text line or a word, with its absolute position in the image,
 * the result of ocr analysis and a first raw analysis of the string extracted.
 */

public class OcrText implements Comparable<OcrText> {

    // List of substitutions used to correct common ocr mistakes in the reading of a price.
    private static final List<Pair<String, String>> NUM_SANITIZE_LIST = asList(
            new Pair<>("O", "0"),
            new Pair<>("o", "0"),
            new Pair<>("S", "5"),
            new Pair<>(",", "."),
            new Pair<>("s", "5")
    );

    // These substitutions are less frequent and may produce false positives. Should be used only if you are sure your string is a number.
    private static final List<Pair<String, String>> NUM_SANITIZE_ADVANCED = asList(
            new Pair<>("D", "0"),
            new Pair<>("U", "0"),
            new Pair<>("I", "1"),
            new Pair<>("l", "1"), // lowercase L
            new Pair<>("?", "7"),
            new Pair<>("B", "8")
    );

    private boolean isWord;
    private Lazy<List<OcrText>> children; // if this OcrText instance is a text line, its children are words
    private Lazy<List<PointF>> corners; // corners of a rotated rectangle containing the line of text
    //these values are always used, so no need for lazy
    private float height, width; // width, height of entire line
    private float charWidth, charHeight; // average width, height of single character
    private Lazy<RectF> box;
    private String text;
    private Lazy<String> textUppercase; // uppercase text
    private Lazy<String> uppercaseAlphaNum; // uppercase text where all non alphanumeric characters are removed. No spaces between words
    private Lazy<String> textNoSpaces; // uppercase text with no spaces between words
    private Lazy<String> sanitizedNum; // text where it was applied the NUM_SANITIZE_LIST substitutions
    private Lazy<String> sanitizedAdvanced; // text where it was applied the NUM_SANITIZE_LIST and NUM_SANITIZE_ADVANCED substitutions
    private List<String> tags;

    public OcrText(Text text) {
        isWord = text instanceof Element;
        OcrUtils.log(9, "OCRTEXT: ", "I'm a word: " + isWord);
        children = new Lazy<>(() -> Stream.of(text.getComponents()).map(OcrText::new).toList());
        corners = new Lazy<>(() -> pointsToPointFs(asList(text.getCornerPoints())));

        // width and height are respectively the longest and shortest side of the rotated rectangle
        width =  dist(corners().get(0), corners().get(1));
        height = dist(corners().get(0), corners().get(3));
        box = new Lazy<>(() -> new RectF(text.getBoundingBox()));

        this.text = text.getValue();
        OcrUtils.log(9, "OCRTEXT:", "Text is: " + text.getValue());
        textUppercase = new Lazy<>(() -> text().toUpperCase());
        sanitizedNum = new Lazy<>(() -> {
            String res = text();
            for (Pair<String, String> p : NUM_SANITIZE_LIST)
                res = res.replace(p.first, p.second);
            return/*sanitizedNum*/ res;
        });
        sanitizedAdvanced = new Lazy<>(() -> {
            String res = sanitizedNum();
            for (Pair<String, String> p : NUM_SANITIZE_ADVANCED)
                res = res.replace(p.first, p.second);
            return/*sanitizedAdvanced*/ res;
        });

        // I used .reduce() to concatenate every result of the lambda expression
        // if these are accessed on a word, they return empty string
        textNoSpaces = new Lazy<>(() -> Stream.of(children())
                .reduce("", (str, c) -> str + c.textUppercase()));
        if (isWord) {
            // length of the longest side of the rotated rectangle, divided by the number of characters of the word
            charWidth = max(asList(dist(corners().get(0), corners().get(1)),
                    dist(corners().get(2), corners().get(3)))) / text().length();
            // length of the shortest side of the rotated rectangle
            charHeight = min(asList(dist(corners().get(0), corners().get(3)),
                    dist(corners().get(1), corners().get(2))));

            uppercaseAlphaNum = new Lazy<>(() -> textUppercase().replaceAll("[^A-Z0-9]", ""));
        }
        else {
            // average of individual words char width, weighted on word length.
            // I do not use directly line width because sometimes it's greater than the actual font width
            charWidth = Stream.of(children()).reduce(0f, (sum, currentChild) ->
                    sum + currentChild.charWidth() * currentChild.length()) / textNoSpaces().length();

            // average of individual words char height, weighted on word length
            charHeight = Stream.of(children()).reduce(0f, (sum, currentChild) ->
                    sum + currentChild.charHeight() * currentChild.length()) / textNoSpaces().length();

            uppercaseAlphaNum = new Lazy<>(() -> Stream.of(children())
                    .reduce("", (str, c) -> str + c.uppercaseAlphaNum()));
        }
        tags = new ArrayList<>();
    }

    /**
     * Create a new OcrText with dimensions converted from one bitmap space to another
     * @param ocrText OcrText to convert
     * @param srcImgRect source bitmap space. Eg: optimized amount strip, from (0, 0)
     * @param dstImgRect destination space. Eg: amount strip in original bitmap space.
     */
    public OcrText(OcrText ocrText, RectF srcImgRect, RectF dstImgRect) {
        isWord = ocrText.isWord;
        //apply bitmap space transformation for each word in ocrText
        children = new Lazy<>(() -> Stream.of(ocrText.children())
                .map(child -> new OcrText(child, srcImgRect, dstImgRect))
                .toList());
        corners = new Lazy<>(() -> transform(ocrText.corners(), srcImgRect, dstImgRect));

        // to calculate the width and height I should use the transformed corners, but this would lead to
        // a distorted rectangle that would not give a proper width and height.
        width = transform(ocrText.width(), srcImgRect.width(), dstImgRect.width());
        height = transform(ocrText.height(), srcImgRect.height(), dstImgRect.height());
        charWidth = transform(ocrText.charWidth(), srcImgRect.width(), dstImgRect.width());
        charHeight = transform(ocrText.charHeight(), srcImgRect.height(), dstImgRect.height());
        box = new Lazy<>(() -> transform(ocrText.box(), srcImgRect, dstImgRect));
        OcrUtils.log(9, "OCRTEXT", "Analyze: " + ocrText.text());
        OcrUtils.log(9, "OCRTEXT", "Mapping rect (l,t,r,b): (" + ocrText.box().left + "," +
                ocrText.box().top + "," + ocrText.box().right + "," + ocrText.box().bottom + ")");
        OcrUtils.log(9, "OCRTEXT", "FROM (source): (" + srcImgRect.left + "," +
                srcImgRect.top + "," + srcImgRect.right + "," + srcImgRect.bottom + ")");
        OcrUtils.log(9, "OCRTEXT", "TO (dest): (" + dstImgRect.left + "," +
                dstImgRect.top + "," + dstImgRect.right + "," + dstImgRect.bottom + ")");
        OcrUtils.log(9, "OCRTEXT", "RESULT: (" + box().left + "," +
                box().top + "," + box().right + "," + box().bottom + ")");

        // the text fields remain unchanged
        text = ocrText.text;
        textUppercase = ocrText.textUppercase;
        textNoSpaces = ocrText.textNoSpaces;
        uppercaseAlphaNum = ocrText.uppercaseAlphaNum;
        sanitizedNum = ocrText.sanitizedNum;
        sanitizedAdvanced = ocrText.sanitizedAdvanced;
        tags = ocrText.getTags();
    }

    public boolean isWord() { return isWord; }
    public List<OcrText> children() { return children.get(); }
    public float length() { return text.length(); }

    // rectangle properties
    public List<PointF> corners() { return corners.get(); }
    public float width() { return width; }
    public float height() { return height; }
    public RectF box() { return box.get(); } // bounding box size is always >= of (width(), height())

    //character size
    public float charWidth() { return charWidth; }
    public float charHeight() { return charHeight; }

    // string properties
    public String text() { return text; }
    public String textUppercase() { return textUppercase.get(); }
    public String uppercaseAlphaNum() { return uppercaseAlphaNum.get(); }
    // available only if isWord() == false:
    public String textNoSpaces() { return textNoSpaces.get(); }
    public String sanitizedNum() { return sanitizedNum.get(); }
    public String sanitizedAdvancedNum() { return sanitizedAdvanced.get(); }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag))
            this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    /**
     * Necessary for schemer.
     * Order: top to bottom, left to right
     * @param rawText target text
     * @return int > 0 if target comes before source (i.e. is above/on the left)
     */
    @Override
    public int compareTo(@NonNull OcrText rawText) {
        RectF text2Rect = rawText.box();
        if (text2Rect.top != box().top)
            return box().top - text2Rect.top > 0 ? 1 : -1;
        else if (text2Rect.left != box().left)
            return box().left - text2Rect.left > 0 ? 1 : -1;
        else if (text2Rect.bottom != box().bottom)
            return box().bottom - text2Rect.bottom > 0 ? 1 : -1;
        else if (box().right != text2Rect.right)
            return box().right - text2Rect.right > 0 ? 1 : -1;
        else
            return 0;
    }
}
