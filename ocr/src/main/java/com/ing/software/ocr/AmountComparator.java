package com.ing.software.ocr;

import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ing.software.ocr.DataAnalyzer.analyzeAmount;
import static com.ing.software.ocr.OcrVars.NUMBER_MIN_VALUE;

/**
 * Class to analyze amount and compare it with list of prices, subtotal, cash paid, change.
 * Get methods are not private 'cause I want them to be accessible from outside for future implementations.
 * todo: handle tips and taxes
 */

class AmountComparator {

    private BigDecimal amount;
    private RawText amountText;
    private int precision = 0;
    private boolean hasSubtotal = false;
    private boolean hasPriceList = false;
    private boolean hasCash = false;
    private boolean hasChange = false;
    private boolean hasAmount = false;
    private BigDecimal subTotal = null;
    private BigDecimal priceList = null;
    private BigDecimal cash = null;
    private BigDecimal change = null;

    /**
     * @author Michelon
     * Constructor
     * @date 3-1-18
     * @param amountText RawText containing amount. Not null.
     * @param amount BigDecimal containing decoded amount.
     */
    AmountComparator(@NonNull RawText amountText, BigDecimal amount) {
        this.amountText = amountText;
        this.amount = amount;
        if (amount != null)
            hasAmount = true;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return amount. Note: this is the amount parsed in constructor, not the best amount from getBestAmount()
     */
    BigDecimal getAmount() {
        return amount;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return RawText containing amount
     */
    private RawText getAmountText() {
        return amountText;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * Adds one hit in search (=one value among subtotal, cash etc. was found)
     */
    private void addPrecision() {
        ++precision;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * Subtotal was found for current receipt. Set a hit.
     * @param subTotal subtotal found. Not null.
     */
    private void flagHasSubtotal(@NonNull BigDecimal subTotal) {
        if (!hasSubtotal)
            addPrecision();
        hasSubtotal = true;
        this.subTotal = subTotal;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * PriceList was found for current receipt. Set a hit.
     * @param priceList sum of prices found. Not null.
     */
    private void flagHasPriceList(@NonNull BigDecimal priceList) {
        if (!hasPriceList)
            addPrecision();
        hasPriceList = true;
        this.priceList = priceList;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * Cash was found for current receipt. Set a hit.
     * @param cash cash. Not null.
     */
    private void flagHasCash(@NonNull BigDecimal cash) {
        if (!hasCash)
            addPrecision();
        hasCash = true;
        this.cash = cash;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * Change was found for current receipt. Set a hit.
     * @param change change. Not null.
     */
    private void flagHasChange(@NonNull BigDecimal change) {
        if (!hasChange)
            addPrecision();
        hasChange = true;
        this.change = change;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return number of hits
     */
    private int getPrecision() {
        return precision;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return subtotal
     */
    BigDecimal getSubTotal() {
        return subTotal;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return priceList
     */
    BigDecimal getPriceList() {
        return priceList;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return change
     */
    BigDecimal getChange() {
        return change;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * @return cash
     */
    BigDecimal getCash() {
        return cash;
    }

    /**
     * @author Michelon
     * @date 17-12-17
     * @return true if subtotal is not null
     */
    boolean hasSubtotal() {
        return hasSubtotal;
    }

    /**
     * @author Michelon
     * @date 17-12-17
     * @return true if priceList is not null
     */
    boolean hasPriceList() {
        return hasPriceList;
    }

    /**
     * @author Michelon
     * @date 17-12-17
     * @return true if cash is not null
     */
    boolean hasCash() {
        return hasCash;
    }

    /**
     * @author Michelon
     * @date 17-12-17
     * @return true if change is not null
     */
    boolean hasChange() {
        return hasChange;
    }

    /**
     * @author Michelon
     * @date 3-1-18
     * @return true if amount is not null
     */
    boolean hasAmount() {
        return hasAmount;
    }

    /**
     * @author Michelon
     * @date 4-1-18
     * Analyze a list of possible prices and sum them. Then try to find subtotal, if present.
     * If found update this object accordingly
     * todo: handle tips and explicit taxes
     * @param possiblePrices list of ordered (top to bottom) possible prices. Not null.
     */
    void analyzePrices(@NonNull List<RawGridResult> possiblePrices) {
        BigDecimal decodedAmount = getAmount();
        int maxDistance = 2; //only 2 miss in amount detection
        //above amount we can have all prices and a subtotal, so first sum all products with distance > 0
        if (possiblePrices.size() == 0 || possiblePrices.get(0).getPercentage() < 0)
            return;
        BigDecimal productsSum = null;
        BigDecimal possibleSubTotal = null;
        int index = 0;
        //Search for first parsable product price
        while (productsSum == null && index < possiblePrices.size() && possiblePrices.get(index).getPercentage() > 0) {
            String productPrice = possiblePrices.get(index).getText().getValue();
            if (OcrUtils.isPossiblePriceNumber(productPrice) < NUMBER_MIN_VALUE)
                productsSum = analyzeAmount(productPrice);
            ++index;
        }
        if (productsSum != null)
            OcrUtils.log(3, "analyzePrices", "List of prices, first value is: " + productsSum.toString());
        else
            OcrUtils.log(3, "analyzePrices", "List of prices, no value found");
        while (index < possiblePrices.size() && possiblePrices.get(index).getPercentage() > 0) {
            String productPrice = possiblePrices.get(index).getText().getValue();
            if (OcrUtils.isPossiblePriceNumber(productPrice) < NUMBER_MIN_VALUE) {
                BigDecimal adder = analyzeAmount(productPrice);
                if (adder != null) {
                    productsSum = productsSum.add(adder);
                    possibleSubTotal = adder; //this way when i exit the while i'll have in possibleSubTotal last value analyzed
                }
            } else {
                OcrUtils.log(3, "analyzePrices", "Not a valid number: " + productPrice);
            }
            OcrUtils.log(3, "analyzePrices", "List of prices, new total value is: " + productsSum.toString());
            ++index;
        }
        if (productsSum != null) {
            BigDecimal halfProductSum = productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
            //Check if my subtotal is the same as total, or if amount is null
            //if (possibleSubTotal != null && (!hasAmount() || decodedAmount.compareTo(possibleSubTotal) == 0)) {
            if (possibleSubTotal != null) {
                //Accept the value
                OcrUtils.log(3, "analyzePrices", "Subtotal is: " + possibleSubTotal.toString());
                flagHasSubtotal(possibleSubTotal);
            }
            //now we may have the same value of decodedAmount, or its double (=*2)
            if (hasAmount()) {
                if (decodedAmount.compareTo(halfProductSum) == 0) {
                    OcrUtils.log(3, "analyzePrices", "List of prices/2 equals decoded amount");
                    flagHasPriceList(halfProductSum);
                } else {
                    flagHasPriceList(productsSum);
                }
            } else if (possibleSubTotal != null){ //amount is null
                if (possibleSubTotal.compareTo(halfProductSum) == 0) {
                    OcrUtils.log(3, "analyzePrices", "List of prices/2 equals subtotal");
                    flagHasPriceList(halfProductSum);
                } else {
                    flagHasPriceList(productsSum);
                }
            } else { //amount and possibleSubtotal are null
                flagHasPriceList(productsSum); //just add it
            }
        }
        if (getPriceList() != null)
            OcrUtils.log(2, "analyzePrices", "List of prices, final value is: " + getPriceList().toString());
    }

    /**
     * @author Michelon
     * @date 10-12-17
     * Analyze a list of possible prices looking for cash (='contante') and change (='resto').
     * If found update this object accordingly
     * todo: handle tips and explicit taxes
     * @param possiblePrices list of possible prices. Not null.
     */
    void analyzeTotals(@NonNull List<RawGridResult> possiblePrices) {
        BigDecimal decodedAmount = getAmount();
        int maxDistance = 2; //only 2 miss in amount detection
        //under amount we accept only 'contante' and 'resto' (if present)
        BigDecimal cash = null;
        BigDecimal change = null;
        RawText cashText = null;
        int index = 0;
        //Search for first parsable product price
        while (cash == null && index < possiblePrices.size()) {
            if (possiblePrices.get(index).getPercentage() < 0) {//must be below total
                if (OcrSchemer.isPossibleCash(getAmountText(), possiblePrices.get(index).getText())) {
                    String possibleCash = possiblePrices.get(index).getText().getValue();
                    if (OcrUtils.isPossiblePriceNumber(possibleCash) < NUMBER_MIN_VALUE) {
                        cash = analyzeAmount(possibleCash);
                        cashText = possiblePrices.get(index).getText();
                    } else {
                        OcrUtils.log(3, "analyzeTotals", "Not a valid number: " + possibleCash);
                    }
                }
            }
            ++index;
        }
        if (cash != null) {
            OcrUtils.log(2, "analyzeTotals", "Cash is: " + cash.toString());
            while (index < possiblePrices.size() && change == null) {
                //change must be below cash, here i use the same method as above, but parsing cash as rawtext
                if (OcrSchemer.isPossibleCash(cashText, possiblePrices.get(index).getText())) {
                    String possibleChange = possiblePrices.get(index).getText().getValue();
                    if (OcrUtils.isPossiblePriceNumber(possibleChange) < NUMBER_MIN_VALUE)
                        change = analyzeAmount(possibleChange);
                }
                ++index;
            }
            if (change != null)
                OcrUtils.log(2, "analyzeTotals", "Change is: " + change.toString());
            if (hasAmount()) {
                //Check if cash is the same as total
                if (decodedAmount.compareTo(cash) == 0) {
                    //Accept the value
                    flagHasCash(cash);
                    OcrUtils.log(3, "analyzeTotals", "Cash equals decoded amount");
                } else {
                    flagHasCash(cash);
                    OcrUtils.log(3, "analyzeTotals", "Cash diffs from decoded amount");
                }
                if (change != null) {
                    BigDecimal cashSubChange = cash.subtract(change);
                    if (cashSubChange.compareTo(decodedAmount) == 0) {
                        flagHasChange(change);
                        OcrUtils.log(3, "analyzeTotals", "decoded amount is cash - change");
                    } else if (hasSubtotal()){
                        if (cashSubChange.compareTo(getSubTotal()) == 0) {
                            flagHasChange(change);
                            OcrUtils.log(3, "analyzeTotals", "subtotal equals cash - change");
                        }
                    } else if (hasPriceList()) {
                        if (cashSubChange.compareTo(getPriceList()) == 0) {
                            flagHasChange(change);
                            OcrUtils.log(3, "analyzeTotals", "subtotal equals cash - change");
                        }
                    }
                }
            } else {
                //amount is null: add both cash and change
                flagHasCash(cash);
                if (change != null) {
                    BigDecimal cashSubChange = cash.subtract(change);
                    if (hasSubtotal()){
                        if (cashSubChange.compareTo(getSubTotal()) == 0) {
                            flagHasChange(change);
                            OcrUtils.log(3, "analyzeTotals", "subtotal equals cash - change");
                        }
                    } else if (hasPriceList()) {
                        if (cashSubChange.compareTo(getPriceList()) == 0) {
                            flagHasChange(change);
                            OcrUtils.log(3, "analyzeTotals", "subtotal equals cash - change");
                        }
                    }
                }
                OcrUtils.log(3, "analyzeTotals", "Amount is null: adding cash");
            }
        }
    }

    /**
     * @author Michelon
     * @date 3-1-18
     * Analyze results stored in this amountAnalyzer. If at least two numbers among subtotal, pricelist
     * and cash (= cash - change) are equals, return this value.
     * @param minHit minimum number of hit to accept a value. Int from 0 to 2 (price, cash, subtotal, amount)
     *               0 = only amount/subtotal/cash/prices
     *               1 = two equal values
     *               2 = three equal values
     * @return BigDecimal containing the probable amount
     * todo: it's quite a mess now...
     */
    BigDecimal getBestAmount(@IntRange (from = 0, to = 2) int minHit) {
        if (getPrecision() > 0) {
            //analyze all possible cases
            BigDecimal subtotal = getSubTotal();
            BigDecimal cash = null;
            BigDecimal prices = getPriceList();
            BigDecimal amount = getAmount();
            if (hasCash() && !hasChange())
                cash = getCash();
            else if (hasCash()) {
                cash = getCash().subtract(getChange());
            }
            if (hasAmount()) { //return if find 2 or 3 equal values
                if (hasPriceList() && hasCash()) {
                    boolean cashPrices = cash.compareTo(prices) == 0;
                    boolean cashAmount = cash.compareTo(amount) == 0;
                    if (cashPrices) {//Probably if both pricelist and cash are the same amount is wrong
                        if (cashAmount) {
                            OcrUtils.log(2, "getBestAmount", "Three equal values found: (cash, pricelist, amount)");
                            OcrUtils.log(2, "getBestAmount", "New amount is: " + cash.toString());
                            return cash;
                        } else if (minHit < 2) {
                            OcrUtils.log(2, "getBestAmount", "Two equal values found: (cash, pricelist)");
                            OcrUtils.log(2, "getBestAmount", "New amount is: " + cash.toString());
                            return cash;
                        }
                    }
                }
                if (hasSubtotal() && hasPriceList()) {
                    boolean subtotalPrices = subtotal.compareTo(prices) == 0;
                    boolean subtotalAmount = subtotal.compareTo(amount) == 0;
                    if (subtotalPrices) { //Probably if both subtotal and cash are the same amount is wrong
                        if (subtotalAmount) {
                            OcrUtils.log(2, "getBestAmount", "Three equal values found: (subtotal, pricelist, amount)");
                            OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                            return subtotal;
                        } else if (minHit < 2) {
                            OcrUtils.log(2, "getBestAmount", "Two equal values found: (subtotal, pricelist)");
                            OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                            return subtotal;
                        }
                    }
                }
                if (hasSubtotal() && hasCash()) {
                    boolean cashSubtotal = cash.compareTo(subtotal) == 0;
                    boolean cashAmount = cash.compareTo(amount) == 0;
                    if (cashSubtotal) { //Probably if both subtotal and cash are the same amount is wrong
                        if (cashAmount) {
                            OcrUtils.log(2, "getBestAmount", "Three equal values found: (cash, subtotal, amount)");
                            OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                            return subtotal;
                        } else if (minHit < 2) {
                            OcrUtils.log(2, "getBestAmount", "Two equal values found: (cash, subtotal)");
                            OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                            return subtotal;
                        }
                    }
                } //here minHit must be 0 or 1
                if (minHit == 1) {
                    if ((hasCash() && cash.compareTo(amount) == 0) || (hasPriceList() && prices.compareTo(amount) == 0)
                            || (hasSubtotal() && subtotal.compareTo(amount) == 0)) {
                        OcrUtils.log(2, "getBestAmount", "Two equal values found. Return amount.");
                        return amount;
                    }
                } else //minHit is 0
                    return amount;
            } else { //amount is null
                if (hasCash() && hasSubtotal() && hasPriceList()) {
                    boolean cashPrices = cash.compareTo(prices) == 0;
                    boolean subtotalPrices = subtotal.compareTo(prices) == 0;
                    boolean cashSubtotal = cash.compareTo(subtotal) == 0;
                    if (cashPrices && subtotalPrices) {
                        OcrUtils.log(2, "getBestAmount", "Three equal values found: (cash, pricelist, subtotal)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + cash.toString());
                        return cash;
                    } else if (cashPrices && minHit < 2) {
                        OcrUtils.log(2, "getBestAmount", "Two equal values found: (cash, pricelist)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + cash.toString());
                        return cash;
                    } else if (subtotalPrices && minHit < 2) {
                        OcrUtils.log(2, "getBestAmount", "Two equal values found: (subtotal, pricelist)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                        return subtotal;
                    } else if (cashSubtotal && minHit < 2) {
                        OcrUtils.log(2, "getBestAmount", "Two equal values found: (cash, subtotal)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + cash.toString());
                        return cash;
                    }
                }
                if (hasPriceList() && hasCash() && minHit < 2) {
                    boolean cashPrices = cash.compareTo(prices) == 0;
                    if (cashPrices) {//Probably if both pricelist and cash are the same amount is wrong
                        OcrUtils.log(2, "getBestAmount", "Two equal values found: (cash, pricelist)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + cash.toString());
                        return cash;
                    }
                }
                if (hasSubtotal() && hasPriceList() && minHit < 2) {
                    boolean subtotalPrices = subtotal.compareTo(prices) == 0;
                    if (subtotalPrices) { //Probably if both subtotal and cash are the same amount is wrong
                        OcrUtils.log(2, "getBestAmount", "Two equal values found: (subtotal, pricelist)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                        return subtotal;
                    }
                }
                if (hasSubtotal() && hasCash() && minHit < 2) {
                    boolean cashSubtotal = cash.compareTo(subtotal) == 0;
                    if (cashSubtotal) { //Probably if both subtotal and cash are the same amount is wrong
                        OcrUtils.log(2, "getBestAmount", "Two equal values found: (cash, subtotal)");
                        OcrUtils.log(2, "getBestAmount", "New amount is: " + subtotal.toString());
                        return subtotal;
                    }
                }
                if (hasSubtotal() && minHit < 1) {
                    return subtotal;
                } else if (hasCash() && minHit < 1) {
                    return cash;
                } else if (minHit < 1)
                    return prices;
            }
        } else if (hasAmount() && minHit < 1) {
            return getAmount();
        }
        return null;
    }

    /**
     * @author Michelon
     * @date 10-12-17
     * Retrieves all texts from products on the same 'column' as amount. Stricter than the simple list of prices.
     * @param amountText RawText containing possible amount. Not null.
     * @param products   List of RawTexts containing products (both name and price). Not null.
     * @return Ordered list of texts above or under amount with distance from amount (positive = above)
     */
    static List<RawGridResult> getPricesList(@NonNull RawText amountText, @NonNull List<RawText> products) {
        List<RawGridResult> possiblePrices = new ArrayList<>();
        for (RawText productText : products) {
            if (isProductPrice(amountText, productText)) {
                int distanceFromSource = getProductPosition(amountText, productText);
                possiblePrices.add(new RawGridResult(productText, distanceFromSource));
            }
        }
        if (possiblePrices.size() > 0)
            Collections.sort(possiblePrices);
        return possiblePrices;
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * Return true if product is in the same column as amount
     * @param amount  RawText containing possible amount. Not null.
     * @param product RawText containing possible product price. Not null.
     * @return true if product is in the same column (extended by percentage) as amount
     */
    private static boolean isProductPrice(@NonNull RawText amount, @NonNull RawText product) {
        int percentage = 50;
        Rect extendedRect = OcrUtils.extendRect(amount.getBoundingBox(), 0, percentage);
        Rect productRect = product.getBoundingBox();
        return (extendedRect.left < productRect.left) && (extendedRect.right > productRect.right);
    }

    /**
     * @author Michelon
     * @date 9-12-17
     * Return distance from amount: positive if product is above amount
     * @param amount  RawText containing possible amount. Not null.
     * @param product RawText containing possible product price. Not null.
     * @return distance
     */
    private static int getProductPosition(@NonNull RawText amount, @NonNull RawText product) {
        return Math.round(amount.getBoundingBox().top - product.getBoundingBox().top);
    }
}
