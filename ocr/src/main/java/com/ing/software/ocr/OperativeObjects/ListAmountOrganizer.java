package com.ing.software.ocr.OperativeObjects;

import android.support.annotation.NonNull;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.TempText;
import com.ing.software.ocr.ScoreFunc;

import java.util.ArrayList;
import java.util.List;

/**
 * keep ordered list of possible results from target string
 */

public class ListAmountOrganizer implements Comparable<ListAmountOrganizer>{

    private Scored<TempText> sourceText;
    private List<Scored<TempText>> targetTexts = new ArrayList<>();

    public ListAmountOrganizer(Scored<TempText> source) {
        sourceText = new Scored<>(ScoreFunc.getSourceAmountScore(source), source.obj());
    }

    public void setAmountTargetTexts(List<TempText> targets) {
        for (TempText text : targets) {
            targetTexts.add(new Scored<>(ScoreFunc.getAmountScore(text), text));
        }
    }

    public Scored<TempText> getSourceText() {
        return sourceText;
    }

    public List<Scored<TempText>> getTargetTexts() {
        return targetTexts;
    }

    @Override
    public int compareTo(@NonNull ListAmountOrganizer listAmountOrganizer) {
        return sourceText.compareTo(listAmountOrganizer.getSourceText());
    }
}
