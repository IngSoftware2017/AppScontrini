package com.ing.software.ocr.OcrObjects.TicketSchemes;

import android.util.Pair;
import com.ing.software.ocr.OcrObjects.OcrText;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Michelon
 * Minimal interface representing a ticket
 */

public interface TicketScheme {

    /**
     * Get best amount according to last call of getAmountScore(boolean).
     * Value returned depends on param of the last call of getAmountScore().
     * @return best current amount
     */
    Pair<OcrText, BigDecimal> getBestAmount();

    /**
     * Updates best amount for ticket according to passed param.
     * @param strict true if you want to check if scheme is compatible with current ticket.
     *               False if you want the best approximation.
     * @return Score for current scheme, value may vary, refer to individual schemes
     */
    double getAmountScore(boolean strict);

    /**
     * @return tag of this scheme
     */
    String toString();

    /**
     * Get list of prices that match current scheme. Null if list does not correspond to current best amount.
     * @return list of prices and their texts. Null if list is invalid.
     */
    List<Pair<OcrText, BigDecimal>> getPricesList();
}
