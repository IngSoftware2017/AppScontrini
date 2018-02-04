package com.ing.software.ocr.OcrObjects;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import static com.ing.software.ocr.OcrObjects.OcrLevels.*;

/**
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on image scale)
 */

public class OcrOptions {

    private List<OcrLevels> levels;

    /**
     * Constructor
     * @param levels list of actions to perform
     */
    public OcrOptions(@NonNull List<OcrLevels> levels) {
        this.levels = levels;
    }

    /**
     * Return default Options
     * @return default options
     */
    public static OcrOptions getDefaultOptions() {
        return new OcrOptions(Arrays.asList(NORMAL, AMOUNT_DEEP, DATE_NORMAL, PRICES_DEEP));
    }

    public boolean isFindTotal() {
        return levels.contains(AMOUNT_DEEP) || levels.contains(OcrLevels.AMOUNT_NORMAL) || levels.contains(OcrLevels.EXTENDED_SEARCH);
    }

    public boolean isFindDate() {
        return levels.contains(OcrLevels.DATE_NORMAL);
    }

    public boolean isFindProducts() {
        return levels.contains(OcrLevels.PRICES_DEEP) || levels.contains(OcrLevels.PRICES_NORMAL);
    }

    public boolean isRedoUpsideDown() {return levels.contains(UPSIDE_DOWN_SEARCH);}

    public boolean contains(OcrLevels setting) {
        return levels.contains(setting);
    }
}
