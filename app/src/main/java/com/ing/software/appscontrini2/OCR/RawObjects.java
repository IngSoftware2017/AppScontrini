package com.ing.software.appscontrini2.OCR;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

class RawBlock {

    private final int COLUMNS = 16;
    private final int ROWS = 9;
    private ArrayList<RawText> rawTexts = new ArrayList<>();
    private List<? extends Text> textComponents;
    private int imageWidth;
    private int imageHeigth;
    private RectF rectF;

    RawBlock(TextBlock textBlock, Bitmap image) {
        rectF = new RectF(textBlock.getBoundingBox());
        imageWidth = image.getWidth();
        imageHeigth = image.getHeight();
        textComponents = textBlock.getComponents();
        initialize();
    }

    private void initialize() {
        int index = 0;
        for (Text currentText : textComponents) {
            rawTexts.add(new RawText(this, currentText, index));
            ++index;
        }
    }

    RectF getRect() {
        return rectF;
    }

    ArrayList<RawText> getRawTexts() {
        return rawTexts;
    }
}

class RawText {

    private RectF rectF;
    private int index;
    private Text text;

    RawText(RawBlock rawBlock, Text text, int position) {
        rectF = rawBlock.getRect();
        this.index = position;
        this.text = text;
    }

    int getPosition() {
        return index;
    }

    String getDetection() {
        return text.getValue();
    }
}
