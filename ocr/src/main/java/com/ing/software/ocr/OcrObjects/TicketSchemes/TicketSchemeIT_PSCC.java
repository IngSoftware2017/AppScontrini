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
 * Scheme of a ticket with list of products, subtotal, cash and change
 */

public class TicketSchemeIT_PSCC implements TicketScheme{

    private String tag = "IT_PSCC";
    private List<Pair<OcrText, BigDecimal>> products = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal cash;
    private BigDecimal change;
    private BigDecimal bestAmount;
    private boolean acceptedList = false;
    private final int FOUR_VALUES = 100;


    public TicketSchemeIT_PSCC(BigDecimal total, @NonNull List<Pair<OcrText, BigDecimal>> aboveTotal, @NonNull List<BigDecimal> belowTotal) {
        this.total = total;
        if (!aboveTotal.isEmpty()) {
            this.subtotal = aboveTotal.get(aboveTotal.size() - 1).second;
        }
        if (aboveTotal.size() > 1) {
            products = new ArrayList<>(aboveTotal);
            products.remove(aboveTotal.size() - 1); //remove subtotal
        }
        if (!belowTotal.isEmpty()) {
            cash = belowTotal.get(0);
            if (belowTotal.size() > 1)
                change = belowTotal.get(1);
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
        if (products != null && total != null && cash != null && change != null && subtotal != null) {
            BigDecimal normCash = cash.subtract(change);
            BigDecimal productsSum = Stream.of(products)
                    .map(product -> product.second)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OcrUtils.log(3, "TicketScheme_" + tag, "productsSum is: " + productsSum);
            OcrUtils.log(3, "TicketScheme_" + tag, "total is: " + total);
            OcrUtils.log(3, "TicketScheme_" + tag, "cash is: " + normCash);
            OcrUtils.log(3, "TicketScheme_" + tag, "subtotal is: " + subtotal);
            if (productsSum.compareTo(total) == 0 && normCash.compareTo(total) == 0 && subtotal.compareTo(total) == 0){
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
            if (products != null && cash != null && subtotal != null && change != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal normCash = cash.subtract(change);
                boolean cashPrices = normCash.compareTo(productsSum) == 0;
                boolean cashAmount = normCash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalCash = subtotal.compareTo(normCash) == 0;
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
                } else if (cashPrices || subtotalPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, productsSum);
                } else if (subtotalCash) {
                    return new Scored<>(TWO_VALUES, subtotal);
                } else {
                    return new Scored<>(NO_MATCH, total);
                }
            } else if (products != null && cash != null && change != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal normCash = cash.subtract(change);
                boolean cashPrices = normCash.compareTo(productsSum) == 0;
                boolean cashAmount = normCash.compareTo(total) == 0;
                boolean pricesAmount = productsSum.compareTo(total) == 0;
                if (cashPrices || pricesAmount) {
                    acceptedList = true;
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
            } else if (cash != null && subtotal != null && change != null) {
                BigDecimal normCash = cash.subtract(change);
                boolean cashAmount = normCash.compareTo(total) == 0;
                boolean subtotalAmount = subtotal.compareTo(total) == 0;
                boolean subtotalCash = subtotal.compareTo(normCash) == 0;
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
            if (products != null && cash != null && subtotal != null && change != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal normCash = cash.subtract(change);
                boolean cashPrices = normCash.compareTo(productsSum) == 0;
                boolean subtotalCash = subtotal.compareTo(normCash) == 0;
                boolean subtotalPrices = subtotal.compareTo(productsSum) == 0;
                if (cashPrices && subtotalCash) {
                    acceptedList = true;
                    return new Scored<>(THREE_VALUES, normCash);
                } else if (cashPrices || subtotalPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, productsSum);
                } else if (subtotalCash) {
                    return new Scored<>(TWO_VALUES, subtotal);
                }
            } else if (products != null && cash != null && change != null) {
                BigDecimal productsSum = Stream.of(products)
                        .map(product -> product.second)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal normCash = cash.subtract(change);
                boolean cashPrices = normCash.compareTo(productsSum) == 0;
                if (cashPrices) {
                    acceptedList = true;
                    return new Scored<>(TWO_VALUES, normCash);
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
            } else if (cash != null && subtotal != null && change != null) {
                BigDecimal normCash = cash.subtract(change);
                boolean subtotalCash = subtotal.compareTo(normCash) == 0;
                if (subtotalCash) {
                    return new Scored<>(TWO_VALUES, subtotal);
                }
            }
            return null;
        }
    }
}
