package com.ing.software.ocr.OcrObjects.TicketSchemes;

import com.ing.software.common.Scored;

import java.math.BigDecimal;

/**
 * @author Michelon
 */

public interface TicketScheme {

    Scored<BigDecimal> getBestAmount(boolean strict);

    String toString();
}
