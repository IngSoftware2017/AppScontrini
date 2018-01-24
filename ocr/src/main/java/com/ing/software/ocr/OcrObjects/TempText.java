package com.ing.software.ocr.OcrObjects;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Text;
import com.ing.software.common.Lazy;

import java.util.ArrayList;
import java.util.List;

import static com.ing.software.common.CommonUtils.dist;
import static com.ing.software.common.CommonUtils.pointToPointF;
import static com.ing.software.common.CommonUtils.transform;
import static com.ing.software.ocr.ScoreFunc.GRID_LENGTH;
import static java.util.Arrays.asList;
import static java.util.Collections.max;
import static java.util.Collections.min;

/**
 * todo: doc
 * Lazy<Float> is useless if the goal is to reduce memory usage, better to use float directly (Lazy uses 'Float' that is way bigger than a 'float')
 * if the goal is to reduce computational power, again Lazy is a better solution only if used rarely and here height and width are always accessed
 * to get average height
 */

public class TempText implements Comparable<TempText> {

    // List of substitutions used to correct common ocr mistakes in the reading of a price.
    private static final List<Pair<String, String>> NUM_SANITIZE_LIST = asList(
            new Pair<>("O", "0"),
            new Pair<>("o", "0"),
            new Pair<>("S", "5"),
            new Pair<>("s", "5"),
            new Pair<>(",", ".")
    );

    private boolean isWord;
    private Lazy<List<TempText>> children;
    private Lazy<List<PointF>> corners; // corners of a rotated rectangle containing the line of text
    private float height, width; // width, height of entire line
    private float charWidth, charHeight; // average width, height of single character
    private Lazy<RectF> box;
    private String text;
    private Lazy<String> textUppercase; // uppercase text
    private Lazy<String> textNoSpaces; // uppercase text with no spaces between words
    private Lazy<String> textSanitizedNum; // text where it was applied the NUM_SANITIZE_LIST substitutions
    private Lazy<String> numNoSpaces; // concatenate all words where there was applied a sanitize substitution suitable for detecting a price.
    private Lazy<String> numConcatDot; // same as numNoSpaces, but words are concatenated with a dot
    private List<String> tags;
    private int tagPosition;

    public TempText(Text text) {
        isWord = text instanceof Element;
        children = new Lazy<>(() -> Stream.of(text.getComponents()).map(TempText::new).toList());
        corners = new Lazy<>(() -> pointToPointF(asList(text.getCornerPoints())));

        // width and height are respectively the longest and shortest side of the rotated rectangle
        width =  max(asList(dist(corners().get(0), corners().get(1)),
                dist(corners().get(2), corners().get(3))));
        height = min(asList(dist(corners().get(0), corners().get(3)),
                dist(corners().get(1), corners().get(2))));

        box = new Lazy<>(() -> new RectF(text.getBoundingBox()));
        this.text = text.getValue();
        textUppercase = new Lazy<>(() -> text().toUpperCase());
        textSanitizedNum = new Lazy<>(() -> {
            String res = text();
            for (Pair<String, String> p : NUM_SANITIZE_LIST)
                res = res.replace(p.first, p.second);
            return res;
        });

        if (isWord) {
            // length of the longest side of the rotated rectangle, divided by the number of characters of the word
            charWidth = max(asList(dist(corners().get(0), corners().get(1)),
                    dist(corners().get(2), corners().get(3)))) / text().length();
            // length of the shortest side of the rotated rectangle
            charHeight = min(asList(dist(corners().get(0), corners().get(3)),
                    dist(corners().get(1), corners().get(2))));
        } else {
            // average of individual words char width, weighted on word length.
            // I do not use directly line width because sometimes it's greater than the actual font width
            charWidth = Stream.of(children()).reduce(0f, (sum, currentChild) ->
                    sum + currentChild.charWidth() * currentChild.length()) / textNoSpaces().length();

            // average of individual words char height, weighted on word length
            charHeight = Stream.of(children()).reduce(0f, (sum, currentChild) ->
                    sum + currentChild.charHeight() * currentChild.length()) / textNoSpaces().length();
        }

        // I used .reduce() to concatenate every result of the lambda expression
        // if these are accessed on a word, they return empty string
        textNoSpaces = new Lazy<>(() -> Stream.of(children())
                .reduce("", (str, c) -> str + c.textUppercase()));
        numNoSpaces = new Lazy<>(() -> Stream.of(children())
                .reduce("", (str, c) -> str + c.textSanitizedNum()));
        numConcatDot = new Lazy<>(() -> Stream.of(children())
                .reduce("", (str, c) -> str + "." + c.textSanitizedNum()));
        tags = new ArrayList<>();
    }

    /**
     * Create a new OcrText with dimensions converted from one bitmap space to another
     * @param tempText OcrText to convert
     * @param srcImgRect source bitmap space. Eg: optimized amount strip, from (0, 0)
     * @param dstImgRect destination space. Eg: amount strip in original bitmap space.
     */
    public TempText(TempText tempText, RectF srcImgRect, RectF dstImgRect) {
        isWord = tempText.isWord;
        //apply bitmap space transformation for each word in tempText
        children = new Lazy<>(() -> Stream.of(tempText.children()).map(child -> new TempText(child, srcImgRect, dstImgRect)).toList());
        corners = new Lazy<>(() -> transform(tempText.corners(), srcImgRect, dstImgRect));

        // to calculate the width and height I should use the transformed corners, but this would lead to
        // a distorted rectangle that would not give a proper width and height.
        width = transform(tempText.width(), srcImgRect.width(), srcImgRect.width());
        width = transform(tempText.height(), srcImgRect.height(), srcImgRect.height());
        charWidth = transform(tempText.charWidth(), srcImgRect.width(), srcImgRect.width());
        charHeight = transform(tempText.charHeight(), srcImgRect.height(), srcImgRect.height());
        box = new Lazy<>(() -> transform(tempText.box(), srcImgRect, dstImgRect));

        // the text fields remain unchanged
        text = tempText.text;
        textUppercase = tempText.textUppercase;
        textNoSpaces = tempText.textNoSpaces;
        numNoSpaces = tempText.numNoSpaces;
        numConcatDot = tempText.numConcatDot;
        tags = tempText.getTags();
    }

    public boolean isWord() { return isWord; }
    public List<TempText> children() { return children.get(); }
    public float length() { return text.length(); }

    // rectangle properties
    public List<PointF> corners() { return corners.get(); }
    public float width() { return width; }
    public float height() { return height; }
    public float area() { return width() * height(); }
    public RectF box() { return box.get(); } // bounding box size is always >= of (width(), height())

    //character size
    public float charWidth() { return charWidth; }
    public float charHeight() { return charHeight; }

    // string properties
    public String text() { return text; }
    public String textSanitizedNum() { return textSanitizedNum.get(); }
    public String textUppercase() { return textUppercase.get(); }
    // available only if isWord() == false:
    public String textNoSpaces() { return textNoSpaces.get(); }
    public String numNoSpaces() { return numNoSpaces.get(); }
    public String numConcatDot() { return numConcatDot.get(); }

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

    public void setTagPosition(double position) {
        if (position > (GRID_LENGTH - 1)/GRID_LENGTH) //Fix IndexOutOfBound Exception
            position = (GRID_LENGTH - 1)/GRID_LENGTH + 0.1;
        this.tagPosition = (int)(position*GRID_LENGTH);
    }

    public int getTagPosition() {
        return tagPosition;
    }

    /**
     * Order: top to bottom, left to right
     * @param rawText target text
     * @return int > 0 if target comes before source (i.e. is above/on the left)
     */
    @Override
    public int compareTo(@NonNull TempText rawText) {
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
