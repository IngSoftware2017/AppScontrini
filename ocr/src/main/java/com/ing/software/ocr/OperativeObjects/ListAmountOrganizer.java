package com.ing.software.ocr.OperativeObjects;

import android.support.annotation.NonNull;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michelon
 * Keep ordered list of possible results from string search.
 * Now it's redundant. Will be removed.
 */

public class ListAmountOrganizer implements Comparable<ListAmountOrganizer>{

    private Scored<OcrText> sourceText;
    private List<Scored<OcrText>> targetTexts = new ArrayList<>();
    private RawImage mainImage;

    /**
     * Constructor.
     * @param source source text containing string
     * @param mainImage source image
     */
    public ListAmountOrganizer(Scored<OcrText> source, RawImage mainImage) {
        source.setScore(ScoreFunc.getSourceAmountScore(source, mainImage));
        sourceText = source;
        this.mainImage = mainImage;
    }

    /**
     * Set target texts for current source and score them.
     * @param targets list of target texts. Not null.
     */
    public void setAmountTargetTexts(@NonNull List<Scored<OcrText>> targets) {
        for (Scored<OcrText> text : targets) {
            text.setScore(ScoreFunc.getAmountScore(text, mainImage));
            targetTexts.add(text);
            OcrUtils.log(5, "setAmountTargetTexts: " , "For text: " + text.obj().text() + " Score is: " + ScoreFunc.getAmountScore(text, mainImage));
        }
        Collections.sort(targetTexts, Collections.reverseOrder()); //higher score comes first
    }

    /**
     * @return current source text (contains source string)
     */
    public Scored<OcrText> getSourceText() {
        return sourceText;
    }

    /**
     * @return current ordered target texts
     */
    public List<Scored<OcrText>> getTargetTexts() {
        return targetTexts;
    }

    @Override
    public int compareTo(@NonNull ListAmountOrganizer listAmountOrganizer) {
        return sourceText.compareTo(listAmountOrganizer.getSourceText());
    }
}
