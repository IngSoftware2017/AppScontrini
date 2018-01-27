package com.ing.software.ocr.OcrObjects.TicketSchemes;

import com.ing.software.common.Scored;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class TicketSchemeIT_PCC implements TicketScheme{

    private String tag = "IT_PCC";
    private List<BigDecimal> products = new ArrayList<>();
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal change;

    public TicketSchemeIT_PCC(BigDecimal total, List<BigDecimal> aboveTotal, List<BigDecimal> belowTotal) {
        this.total = total;
        products = aboveTotal;
        if (!aboveTotal.isEmpty()) {
            cash = belowTotal.get(0);
            if (aboveTotal.size() > 0)
                change = belowTotal.get(1);
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