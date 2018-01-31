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
 * Keep ordered list of possible results from string search.
 */

public class ListAmountOrganizer implements Comparable<ListAmountOrganizer>{

    private Scored<OcrText> sourceText;
    private List<Scored<OcrText>> targetTexts = new ArrayList<>();

    /**
     * Constructor.
     * @param source source text containing string
     */
    public ListAmountOrganizer(Scored<OcrText> source) {
        source.setScore(ScoreFunc.getSourceAmountScore(source));
        sourceText = source;
    }

    /**
     * Set target texts for current source.
     * @param targets list of target texts. Not null.
     */
    public void setAmountTargetTexts(@NonNull List<Scored<OcrText>> targets) {
        for (Scored<OcrText> text : targets) {
            text.setScore(ScoreFunc.getAmountScore(text));
            targetTexts.add(text);
            OcrUtils.log(5, "setAmountTargetTexts: " , "For text: " + text.obj().text() + " Score is: " + ScoreFunc.getAmountScore(text));
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
