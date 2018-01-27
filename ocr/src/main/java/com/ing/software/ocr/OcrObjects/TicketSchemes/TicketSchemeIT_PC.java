package com.ing.software.ocr.OcrObjects.TicketSchemes;

import com.ing.software.common.Scored;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class TicketSchemeIT_PC implements TicketScheme{

    private String tag = "IT_PSCC";
    private List<BigDecimal> products = new ArrayList<>();
    private BigDecimal total;
    private BigDecimal cash;

    public TicketSchemeIT_PC(BigDecimal total, List<BigDecimal> aboveTotal, List<BigDecimal> belowTotal) {
        this.total = total;
        this.products = aboveTotal;
        if (!aboveTotal.isEmpty()) {
            cash = belowTotal.get(0);
        }
    }

    @Override
    public Scored<BigDecimal> getBestAmount() {
        return null;
    }

    @Override
    public String toString() {
        return tag;
    }
}
