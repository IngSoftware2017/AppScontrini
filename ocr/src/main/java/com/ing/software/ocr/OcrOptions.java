package com.ing.software.ocr;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import static com.ing.software.ocr.OcrLevels.*;

/**
 * Object passed to the manager to avoid performing unnecessary operations
 * and consequently reduce time (time depends primarily on image scale)
 */

/* ZAGLIA: at precision level 0 and 1 we should still use ocr strip reanalysis because:
   * it's almost inexpensive time-wise
   * The benefits of strip are limited at full resolution, because the strip is always created already at full resolution
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

    boolean contains(OcrLevels setting) {
        return levels.contains(setting);
    }

    double getResolutionMultiplier() {
        if (levels.contains(VERY_QUICK))
            return 1./3.;
        else if (levels.contains(QUICK))
            return 1./2.;
        else
            return 1.;
    }
}
