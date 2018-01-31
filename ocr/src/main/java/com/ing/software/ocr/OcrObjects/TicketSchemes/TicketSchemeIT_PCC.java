package com.ing.software.ocr.OcrObjects.TicketSchemes;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *@author Michelon
 * Scheme of a ticket with list of products, cash and change
 */

public class TicketSchemeIT_PCC implements TicketScheme{

    private String tag = "IT_PCC";
    private List<BigDecimal> products = new ArrayList<>();
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal change;

    public TicketSchemeIT_PCC(BigDecimal total, @NonNull List<BigDecimal> aboveTotal, @NonNull List<BigDecimal> belowTotal) {
        this.total = total;
        products = new ArrayList<>(aboveTotal);
        if (aboveTotal.isEmpty())
            products = null;
        if (!belowTotal.isEmpty()) {
            cash = belowTotal.get(0);
            if (belowTotal.size() > 1)
                change = belowTotal.get(1);
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
        if (products != null && total != null && cash != null && change != null) {
            BigDecimal normCash = cash.subtract(change);
            BigDecimal productsSum = Stream.of(products)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OcrUtils.log(3, "TicketScheme_" + tag, "productsSum is: " + productsSum);
            OcrUtils.log(3, "TicketScheme_" + tag, "total is: " + total);
            OcrUtils.log(3, "TicketScheme_" + tag, "cash is: " + normCash);
            if (productsSum.compareTo(total) == 0 && normCash.compareTo(total) == 0)
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
            if (products != null && cash != null && change != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal normCash = cash.subtract(change);
                boolean cashPrices = normCash.compareTo(productsSum) == 0;
                boolean cashAmount = normCash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                if (cashPrices || pricesAmount) {
                    if (cashAmount) {
                        return new Scored<>(THREE_VALUES, total);
                    } else if (pricesAmount) {
                        return new Scored<>(TWO_VALUES_AMOUNT, total);
                    } else {
                        return new Scored<>(TWO_VALUES, normCash);
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
            } else if (cash != null && change != null) {
                BigDecimal normCash = cash.subtract(change);
                if (normCash.compareTo(total) == 0) {
                    return new Scored<>(TWO_VALUES_AMOUNT, total);
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else {
                return new Scored<>(NO_MATCH, total);
            }
        } else {
            //no total
            if (products != null && cash != null && change != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal normCash = cash.subtract(change);
                if (normCash.compareTo(productsSum) == 0) {
                    return new Scored<>(TWO_VALUES, normCash);
                }
            }
            return null;
        }
    }
}