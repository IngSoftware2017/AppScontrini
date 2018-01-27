package com.ing.software.ocr.OcrObjects.TicketSchemes;

import com.ing.software.common.Scored;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */

public class TicketSchemeIT_PSCC implements TicketScheme{

    private String tag = "IT_PSCC";
    private List<BigDecimal> products = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal change;

    public TicketSchemeIT_PSCC(BigDecimal total, List<BigDecimal> aboveTotal, List<BigDecimal> belowTotal) {
        this.total = total;
        if (!aboveTotal.isEmpty())
            this.subtotal = aboveTotal.get(aboveTotal.size()-1);
        products = aboveTotal;
        products.remove(aboveTotal.size()-1);
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
