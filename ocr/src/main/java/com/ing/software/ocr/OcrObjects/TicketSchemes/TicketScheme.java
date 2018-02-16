package com.ing.software.ocr.OcrObjects.TicketSchemes;

import android.util.Pair;
import com.ing.software.ocr.OcrObjects.OcrText;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Michelon
 */

public interface TicketScheme {

    Pair<OcrText, BigDecimal> getBestAmount();

    double getAmountScore(boolean strict);

    String toString();

    List<Pair<OcrText, BigDecimal>> getPricesList();
}
