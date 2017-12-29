package com.ing.software.ocr.OcrObjects;


import android.graphics.Point;
import android.graphics.PointF;

import com.annimon.stream.Stream;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextLine {

    private Line line;
    private List<Word> childs;

    public TextLine(Line line) {
        this.line = line;
        childs = new ArrayList<>();
        for (Text txt : line.getComponents()) {
            childs.add(new Word((Element)txt));
        }
    }

    public List<Word> words() {
        return childs;
    }

    public String textNoSpaces() {
        StringBuilder sb = new StringBuilder();
        for (Word w : childs) {
            sb.append(w.text());
        }
        return sb.toString();
    }

    public double density() {
        return 0; //todo stub
    }

    public List<PointF> corners() {
        return Stream.of(line.getCornerPoints()).map(p -> new PointF(p.x, p.y)).toList();
    }

}
