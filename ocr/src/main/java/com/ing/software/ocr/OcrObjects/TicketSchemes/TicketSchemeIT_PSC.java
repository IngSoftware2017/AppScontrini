package com.ing.software.ocr.OcrObjects.TicketSchemes;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.ing.software.common.Scored;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */

public class TicketSchemeIT_PSC implements TicketScheme{

    private String tag = "IT_PSC";
    private List<BigDecimal> products = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal cash;

    public TicketSchemeIT_PSC(BigDecimal total, @NonNull List<BigDecimal> aboveTotal, @NonNull List<BigDecimal> belowTotal) {
        this.total = total;
        if (!aboveTotal.isEmpty())
            this.subtotal = aboveTotal.get(aboveTotal.size()-1);
        this.products = aboveTotal;
        if (aboveTotal.isEmpty())
            products = null;
        else
            products.remove(aboveTotal.size()-1);
        if (!belowTotal.isEmpty()) {
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

    /*
    Temporary, copied and modified from old amount comparator
     */
    private Scored<BigDecimal> temporaryBestAmount() {
        int FOUR_VALUES = 100;
        int THREE_VALUES = 65;
        int THREE_VALUES_AMOUNT = 80;
        int TWO_VALUES_AMOUNT = 55;
        int TWO_VALUES = 30;
        int NO_MATCH = 1;
        if (total != null) {
            if (products != null && cash != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                boolean cashAmount = cash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalCash = subtotal.compareTo(cash) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (cashPrices && pricesAmount) {
                    if (subtotalAmount) {
                        return new Scored<>(FOUR_VALUES, total);
                    } else {
                        return new Scored<>(THREE_VALUES_AMOUNT, total);
                    }
                } else if (cashAmount && subtotalAmount) {
                    return new Scored<>(THREE_VALUES_AMOUNT, total);
                } else if (pricesAmount && subtotalAmount) {
                    return new Scored<>(THREE_VALUES_AMOUNT, total);
                } else if (cashPrices && subtotalPrices) {
                    return new Scored<>(THREE_VALUES, subtotal);
                } else if (cashAmount || pricesAmount || subtotalAmount) {
                    return new Scored<>(TWO_VALUES_AMOUNT, total);
                } else if (cashPrices || subtotalCash) {
                    return new Scored<>(TWO_VALUES, cash);
                } else if (subtotalPrices) {
                    return new Scored<>(TWO_VALUES, subtotal);
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (products != null && cash != null) {
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
            } else if (products != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (subtotalPrices || pricesAmount) {
                    if (subtotalAmount) {
                        return new Scored<>(THREE_VALUES, total);
                    } else if (pricesAmount) {
                        return new Scored<>(TWO_VALUES_AMOUNT, total);
                    } else {
                        return new Scored<>(TWO_VALUES, subtotal);
                    }
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (cash != null && subtotal != null) {
                boolean cashAmount = cash.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalCash = subtotal.compareTo(cash) == 0;
                if (subtotalCash || cashAmount) {
                    if (subtotalAmount) {
                        return new Scored<>(THREE_VALUES, total);
                    } else if (cashAmount) {
                        return new Scored<>(TWO_VALUES_AMOUNT, total);
                    } else {
                        return new Scored<>(TWO_VALUES, subtotal);
                    }
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else {
                return new Scored<>(NO_MATCH, total);
            }
        } else {
            //no total
            if (products != null && cash != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                boolean subtotalCash = subtotal.compareTo(cash) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (cashPrices && subtotalCash) {
                    return new Scored<>(THREE_VALUES, cash);
                } else if (cashPrices || subtotalCash) {
                    return new Scored<>(TWO_VALUES, cash);
                } else if (subtotalPrices) {
                    return new Scored<>(TWO_VALUES, subtotal);
                }
            } else if (products != null && cash != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                if (cashPrices) {
                    return new Scored<>(TWO_VALUES, cash);
                }
            } else if (products != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (subtotalPrices) {
                    return new Scored<>(TWO_VALUES, subtotal);
                }
            } else if (cash != null && subtotal != null) {
                boolean subtotalCash = subtotal.compareTo(cash) == 0;
                if (subtotalCash) {
                    return new Scored<>(TWO_VALUES, subtotal);
                }
            }
            return null;
        }
    }
}
