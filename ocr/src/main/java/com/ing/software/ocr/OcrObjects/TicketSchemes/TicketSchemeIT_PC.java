package com.ing.software.ocr.OcrObjects.TicketSchemes;

import android.support.annotation.NonNull;

import com.ing.software.common.Scored;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.annimon.stream.Stream;
import com.ing.software.ocr.OcrUtils;

/**
 * @author Michelon
 * Scheme of a ticket with list of products and cash
 */

public class TicketSchemeIT_PC implements TicketScheme{

    private String tag = "IT_PC";
    private List<BigDecimal> products = new ArrayList<>();
    private BigDecimal total;
    private BigDecimal cash;

    public TicketSchemeIT_PC(BigDecimal total, @NonNull List<BigDecimal> aboveTotal, @NonNull List<BigDecimal> belowTotal) {
        this.total = total;
        this.products = new ArrayList<>(aboveTotal); //Necessary or in psc last index is removed
        if (aboveTotal.isEmpty())
            products = null;
        if (!belowTotal.isEmpty()) {
            cash = belowTotal.get(0);
        }
    }

    @Override
    public Scored<BigDecimal> getBestAmount(boolean strict) {
        return strict ? strictBestAmount() : looseBestAmount();
    }

    @Override
    public String toString() {
        return tag;
    }

    /**
     * @return scored total if it follows this ticket scheme, null otherwise
     */
    private Scored<BigDecimal> strictBestAmount() {
        if (products != null && total != null && cash != null) {
            BigDecimal productsSum = Stream.of(products)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OcrUtils.log(3, "TicketScheme_" + tag, "productsSum is: " + productsSum);
            OcrUtils.log(3, "TicketScheme_" + tag, "total is: " + total);
            OcrUtils.log(3, "TicketScheme_" + tag, "cash is: " + cash);
            if (productsSum.compareTo(total) == 0 && cash.compareTo(total) == 0)
                return new Scored<>(100, total);
        }
        return null;
    }

    /**
     * @return best amount according to arbitrary decisions
     */
    private Scored<BigDecimal> looseBestAmount() {
        int THREE_VALUES = 80;
        int TWO_VALUES_AMOUNT = 50;
        int TWO_VALUES = 30;
        int NO_MATCH = 1;
        if (total != null) {
            if (products != null && cash != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                boolean cashAmount = cash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                if (cashPrices || pricesAmount) {
                    if (cashAmount) {
                        return new Scored<>(THREE_VALUES, total);
                    } else if (pricesAmount) {
                        return new Scored<>(TWO_VALUES_AMOUNT, total);
                    } else {
                        return new Scored<>(TWO_VALUES, cash);
                    }
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (products != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (productsSum.compareTo(total) == 0) {
                    return new Scored<>(TWO_VALUES_AMOUNT, total);
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (cash != null) {
                if (cash.compareTo(total) == 0) {
                    return new Scored<>(TWO_VALUES_AMOUNT, total);
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else {
                return new Scored<>(NO_MATCH, total);
            }
        } else {
            //no total
            if (products != null && cash != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (cash.compareTo(productsSum) == 0) {
                    return new Scored<>(TWO_VALUES, cash);
                }
            }
            return null;
        }
    }
}
