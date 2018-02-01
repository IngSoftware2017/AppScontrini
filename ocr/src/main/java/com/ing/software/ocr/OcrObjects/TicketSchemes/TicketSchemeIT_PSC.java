package com.ing.software.ocr.OcrObjects.TicketSchemes;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.annimon.stream.Stream;
import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *@author Michelon
 * Scheme of a ticket with list of products, subtotal and cash
 */

public class TicketSchemeIT_PSC implements TicketScheme{

    private String tag = "IT_PSC";
    private List<Pair<OcrText, BigDecimal>> products = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal bestAmount;
    private boolean acceptedList = false;
    private final int FOUR_VALUES = 100;

    public TicketSchemeIT_PSC(BigDecimal total, @NonNull List<Pair<OcrText, BigDecimal>> aboveTotal, @NonNull List<BigDecimal> belowTotal) {
        this.total = total;
        if (!aboveTotal.isEmpty())
            this.subtotal = aboveTotal.get(aboveTotal.size()-1).second;
        this.products = new ArrayList<>(aboveTotal);
        if (aboveTotal.isEmpty())
            products = null;
        else
            products.remove(aboveTotal.size()-1);
        if (!belowTotal.isEmpty()) {
            cash = belowTotal.get(0);
        }
    }

    @Override
    public BigDecimal getBestAmount() {
        return bestAmount;
    }

    @Override
    public double getAmountScore(boolean strict) {
        Scored<BigDecimal> tempAmount = strict ? strictBestAmount() : looseBestAmount();
        if (tempAmount != null) {
            bestAmount = tempAmount.obj();
            return tempAmount.getScore();
        } else
            return -1;
    }

    @Override
    public String toString() {
        return tag;
    }

    @Override
    public List<Pair<OcrText, BigDecimal>> getPricesList() {
        return acceptedList ? products : null;
    }

    /**
     * @return scored total if it follows this ticket scheme, null otherwise
     */
    private Scored<BigDecimal> strictBestAmount() {
        if (products != null && total != null && cash != null && subtotal != null) {
            BigDecimal productsSum = Stream.of(products)
                    .map(product -> product.second)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OcrUtils.log(3, "TicketScheme_" + tag, "productsSum is: " + productsSum);
            OcrUtils.log(3, "TicketScheme_" + tag, "total is: " + total);
            OcrUtils.log(3, "TicketScheme_" + tag, "cash is: " + cash);
            OcrUtils.log(3, "TicketScheme_" + tag, "subtotal is: " + subtotal);
            if (productsSum.compareTo(total) == 0 && cash.compareTo(total) == 0 && subtotal != null){
                acceptedList = true;
                return new Scored<>(FOUR_VALUES, total);
            }
        }
        return null;
    }

    /**
     * @return best amount according to arbitrary decisions
     */
    private Scored<BigDecimal> looseBestAmount() {
        int THREE_VALUES = 65;
        int THREE_VALUES_AMOUNT = 75;
        int TWO_VALUES_AMOUNT = 55;
        int TWO_VALUES = 30;
        int NO_MATCH = 1;
        if (total != null) {
            if (products != null && cash != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                boolean cashAmount = cash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalCash = subtotal.compareTo(cash) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (cashPrices && pricesAmount) {
                    if (subtotalAmount) {
                        acceptedList = true;
                        return new Scored<>(FOUR_VALUES, total);
                    } else {
                        acceptedList = true;
                        return new Scored<>(THREE_VALUES_AMOUNT, total);
                    }
                } else if (cashAmount && subtotalAmount) {
                    return new Scored<>(THREE_VALUES_AMOUNT, total);
                } else if (pricesAmount && subtotalAmount) {
                    acceptedList = true;
                    return new Scored<>(THREE_VALUES_AMOUNT, total);
                } else if (cashPrices && subtotalPrices) {
                    acceptedList = true;
                    return new Scored<>(THREE_VALUES, subtotal);
                } else if (cashAmount || subtotalAmount) {
                    return new Scored<>(TWO_VALUES_AMOUNT, total);
                } else if (pricesAmount) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES_AMOUNT, total);
                } else if (cashPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, cash);
                } else if (subtotalCash) {
                    return new Scored<>(TWO_VALUES, cash);
                } else if (subtotalPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, subtotal);
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (products != null && cash != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                boolean cashAmount = cash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                if (cashPrices || pricesAmount) {
                    if (cashAmount) {
                        acceptedList = true;
                        return new Scored<>(THREE_VALUES, total);
                    } else if (pricesAmount) {
                        acceptedList = true;
                        return new Scored<>(TWO_VALUES_AMOUNT, total);
                    } else {
                        acceptedList = true;
                        return new Scored<>(TWO_VALUES, cash);
                    }
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (products != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (subtotalPrices || pricesAmount) {
                    acceptedList = true;
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
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                boolean subtotalCash = subtotal.compareTo(cash) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (cashPrices && subtotalCash) {
                    acceptedList = true;
                    return new Scored<>(THREE_VALUES, cash);
                } else if (cashPrices || subtotalPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, productsSum);
                } else if (subtotalCash) {
                    return new Scored<>(TWO_VALUES, subtotal);
                }
            } else if (products != null && cash != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean cashPrices = cash.compareTo(productsSum) == 0;
                if (cashPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, cash);
                }
            } else if (products != null && subtotal != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (subtotalPrices) {
                    acceptedList = true;
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
