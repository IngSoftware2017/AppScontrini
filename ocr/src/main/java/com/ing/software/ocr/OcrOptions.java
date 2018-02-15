package com.ing.software.ocr;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import java.util.Locale;

import static com.ing.software.ocr.OcrLevels.*;

/**
 * @author Michelon
 * @author EDIT: Zaglia
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on image scale)
 */

public class OcrOptions {

    private List<OcrLevels> levels;
    private Locale locale;

    /**
     * Constructor
     * @param levels list of actions to perform
     */
    public OcrOptions(@NonNull List<OcrLevels> levels, Locale locale) {
        this.levels = levels;
        this.locale = locale;
    }

    public static OcrOptions getDefaultOptions() {
        return new OcrOptions(Arrays.asList(FULL_RES, AMOUNT_DEEP, DATE_NORMAL, PRICES_DEEP, VOID_SEARCH), Locale.ITALY);
    }

    public OcrOptions add(Locale locale) {
        this.locale = locale;
        return this;
    }

    public OcrOptions add(@NonNull List<OcrLevels> levels) {
        this.levels.addAll(levels);
        return this;
    }

    public OcrOptions add(OcrLevels level) {
        this.levels.add(level);
        return this;
    }

    boolean findTotal() {
        return levels.contains(AMOUNT_DEEP) || levels.contains(OcrLevels.AMOUNT_NORMAL) || levels.contains(OcrLevels.EXTENDED_SEARCH);
    }

    boolean findDate() {
        return levels.contains(OcrLevels.DATE_NORMAL);
    }

    boolean findProducts() {
        return levels.contains(OcrLevels.PRICES_DEEP) || levels.contains(OcrLevels.PRICES_NORMAL);
    }

    boolean redoUpsideDown() {
        return levels.contains(UPSIDE_DOWN_SEARCH);
    }

    boolean voidSearch() {
        return levels.contains(VOID_SEARCH);
    }

    boolean fixTotal() {
        return levels.contains(FIX_PRICE);
    }

    Locale getLocale() {
        return locale;
    }

    boolean contains(OcrLevels setting) {
        return levels.contains(setting);
    }

    double getResolutionMultiplier() {
        if (levels.contains(FULL_RES))
            return 1;
        else if (levels.contains(HALF_RES))
            return 1./2.;
        else if (levels.contains(LOW_RES))
            return 1./3.;
        else
            return 1.;
    }
}
