package com.ing.software.ocr.OcrObjects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class RawTag {

    private List<String> tag = new ArrayList<>();
    private double position;

    public RawTag() {
    }

    public RawTag(String tag, double position) {
        this.tag.add(tag);
        this.position = position;
    }

    public RawTag(String tag) {
        this.tag.add(tag);
    }

    public void addTag(String tag) {
        this.tag.add(tag);
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public List<String> getTag() {
        return tag;
    }

    public double getPosition() {
        return position;
    }

    public boolean contains(String s) {
        return tag.contains(s);
    }
}
