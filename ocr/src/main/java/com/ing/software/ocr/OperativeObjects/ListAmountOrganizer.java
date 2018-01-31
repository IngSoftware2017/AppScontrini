package com.ing.software.ocr.OperativeObjects;

import android.support.annotation.NonNull;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrUtils;
import com.ing.software.ocr.ScoreFunc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * keep ordered list of possible results from target string
 */

public class ListAmountOrganizer implements Comparable<ListAmountOrganizer>{

    private Scored<OcrText> sourceText;
    private List<Scored<OcrText>> targetTexts = new ArrayList<>();

    public ListAmountOrganizer(Scored<OcrText> source) {
        sourceText = new Scored<>(ScoreFunc.getSourceAmountScore(source), source.obj());
    }

    public void setAmountTargetTexts(List<Scored<OcrText>> targets) {
        for (Scored<OcrText> text : targets) {
            targetTexts.add(new Scored<>(ScoreFunc.getAmountScore(text), text.obj()));
            OcrUtils.log(4, "setAmountTargetTexts: " , "For text: " + text.obj().text() + " Score is: " + ScoreFunc.getAmountScore(text));
        }
        Collections.sort(targetTexts, Collections.reverseOrder()); //higher score comes first
        //todo: remove texts with score too low
    }

    public Scored<OcrText> getSourceText() {
        return sourceText;
    }

    public List<Scored<OcrText>> getTargetTexts() {
        return targetTexts;
    }

    @Override
    public int compareTo(@NonNull ListAmountOrganizer listAmountOrganizer) {
        return sourceText.compareTo(listAmountOrganizer.getSourceText());
    }
}
