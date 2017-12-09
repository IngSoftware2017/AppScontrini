package com.ing.software.ocr;

import android.graphics.RectF;
import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.ing.software.ocr.DataAnalyzer.analyzeAmount;

/**
 *
 */

class AmountAnalyzer {

    private BigDecimal amount;
    private RawText amountText;
    private int precision = 0;
    private boolean hasSubtotal = false;
    private boolean hasPriceList = false;
    private boolean hasCash = false;
    private boolean hasChange = false;
    private BigDecimal subTotal = null;
    private BigDecimal priceList = null;
    private BigDecimal cash = null;
    private BigDecimal change = null;

    AmountAnalyzer(RawText amountText, BigDecimal amount) {
        this.amountText = amountText;
        this.amount = amount;
    }

    BigDecimal getAmount() {
        return amount;
    }

    private RawText getAmountText() {
        return amountText;
    }

    private void addPrecision() {
        ++precision;
    }

    private void flagHasSubtotal(BigDecimal subTotal) {
        hasSubtotal = true;
        this.subTotal = subTotal;
        addPrecision();
    }

    private void flagHasPriceList(BigDecimal priceList) {
        hasPriceList = true;
        this.priceList = priceList;
        addPrecision();
    }

    private void flagHasCash(BigDecimal cash) {
        hasCash = true;
        this.cash = cash;
        addPrecision();
    }

    private void flagHasChange(BigDecimal change) {
        hasChange = true;
        this.change = change;
        addPrecision();
    }

    int getPrecision() {
        return precision;
    }

    HashMap<String, Boolean> getFlags() {
        HashMap<String, Boolean> flags = new HashMap<>();
        flags.put("hasSubtotal", hasSubtotal);
        flags.put("hasPriceList", hasPriceList);
        flags.put("hasCash", hasCash);
        flags.put("hasChange", hasChange);
        return flags;
    }

    BigDecimal getSubTotal() {
        return subTotal;
    }

    BigDecimal getPriceList() {
        return priceList;
    }

    BigDecimal getChange() {
        return change;
    }

    BigDecimal getCash() {
        return cash;
    }

    void analyzePrices(List<RawGridResult> possiblePrices) {
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
            String s = possiblePrices.get(index).getText().getDetection();
            if (isPossibleNumber(s))
                productsSum = analyzeAmount(s);
            ++index;
        }
        if (productsSum != null)
            OcrUtils.log(3, "analyzePrices", "List of prices, first value is: " + productsSum.toString());
        while (index < possiblePrices.size()) {
            if (possiblePrices.get(index).getPercentage() <= 0)
                break;
            String s = possiblePrices.get(index).getText().getDetection();
            if (isPossibleNumber(s)) {
                BigDecimal adder = analyzeAmount(s);
                if (adder != null) {
                    productsSum = productsSum.add(adder);
                    possibleSubTotal = adder;
                }
            }
            OcrUtils.log(3, "analyzePrices", "List of prices, new total value is: " + productsSum.toString());
            ++index;
        }
        if (productsSum != null) {
            //Check if my subtotal is the same as total
            if (possibleSubTotal != null && decodedAmount.compareTo(possibleSubTotal) == 0) {
                //Accept the value
                flagHasSubtotal(possibleSubTotal);
            }
            //now we may have the same value of decodedAmount, or its double (=*2)
            if (decodedAmount.compareTo(productsSum) == 0) {
                OcrUtils.log(3, "analyzePrices", "List of prices equals decoded amount");
                flagHasPriceList(productsSum);
            } else if (decodedAmount.compareTo(productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP))) == 0) {
                OcrUtils.log(3, "analyzePrices", "List of prices/2 equals decoded amount");
                flagHasPriceList(productsSum);
                //decodedAmount = productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            } else if (OcrUtils.findSubstring(productsSum.toString(), decodedAmount.toString()) <= maxDistance) {
                //It's acceptable a distance of 1 from target
                flagHasPriceList(productsSum);
                OcrUtils.log(3, "analyzePrices", "List of prices diffs from decoded amount by 1");
            } else if (OcrUtils.findSubstring(productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP)).toString(), decodedAmount.toString()) <= maxDistance) {
                OcrUtils.log(3, "analyzePrices", "List of prices/2 diffs from decoded amount by 1");
                flagHasPriceList(productsSum);
                //decodedAmount = productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            } else {
                OcrUtils.log(3, "analyzePrices", "List of prices is not a valid input");
            }
        }

    }

    //under total we have "contante" or "contante" + "resto"
    //ignore for now tips
    void analyzeTotals(List<RawGridResult> possiblePrices) {
        BigDecimal decodedAmount = getAmount();
        int maxDistance = 2; //only 2 miss in amount detection
        //under amount we accept only 'contante' and 'resto' (if present)
        BigDecimal cash = null;
        BigDecimal change = null;
        RawText cashText = null;
        int index = 0;
        //Search for first parsable product price
        while (cash == null && index < possiblePrices.size()) {
            if (possiblePrices.get(index).getPercentage() < 0)
                if (OcrSchemer.isPossibleCash(getAmountText(), possiblePrices.get(index).getText())) {
                    String s = possiblePrices.get(index).getText().getDetection();
                    if (isPossibleNumber(s)) {
                        cash = analyzeAmount(s);
                        cashText = possiblePrices.get(index).getText();
                    }
                }
            ++index;
        }
        if (cash != null) {
            OcrUtils.log(3, "analyzeTotals", "Cash is: " + cash.toString());
            while (index < possiblePrices.size() && change == null) {
                if (OcrSchemer.isPossibleCash(cashText, possiblePrices.get(index).getText())) {
                    String s = possiblePrices.get(index).getText().getDetection();
                    if (isPossibleNumber(s))
                        change = analyzeAmount(s);
                }
                ++index;
            }
            if (change != null)
                OcrUtils.log(3, "analyzeTotals", "Change is: " + change.toString());
            //Check if cash is the same as total
            if (decodedAmount.compareTo(cash) == 0) {
                //Accept the value
                flagHasCash(cash);
                OcrUtils.log(3, "analyzeTotals", "Cash equals decoded amount");
            } else if (OcrUtils.findSubstring(cash.toString(), decodedAmount.toString()) <= maxDistance) {
                //It's acceptable a distance of 1 from target
                flagHasCash(cash);
                OcrUtils.log(3, "analyzeTotals", "Cash diffs from decoded amount by 1");
            } else if (change != null) {
                //Check if cash - change = decoded amount
                if (cash.subtract(change).compareTo(decodedAmount) == 0) {
                    flagHasCash(cash);
                    flagHasChange(change);
                    OcrUtils.log(3, "analyzeTotals", "decoded amount is cash - change");
                } else if (OcrUtils.findSubstring(cash.subtract(change).toString(), decodedAmount.toString()) <= maxDistance) {
                    flagHasCash(cash);
                    flagHasChange(change);
                    OcrUtils.log(3, "analyzeTotals", "decoded amount diffs from cash - change by 1");
                }
            }
        }

    }

    /**
     * Retrieves all texts from products on the same 'column' as amount
     *
     * @param amountText RawText containing possible amount
     * @param products   List of RawTexts containing products (both name and price)
     * @return List of texts above or under amount with distance from amount (positive = above)
     */
    static List<RawGridResult> getPricesList(RawText amountText, List<RawText> products) {
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
     * Return true if product is in the same column as amount
     *
     * @param amount  RawText containing possible amount
     * @param product RawText containing possible product price
     * @return true if product is in the same column (extended by 50%) as amount
     */
    private static boolean isProductPrice(RawText amount, RawText product) {
        int percentage = 50;
        RectF extendedRect = OcrUtils.partialExtendWidthRect(amount.getRect(), percentage);
        RectF productRect = product.getRect();
        return extendedRect.left < productRect.left && extendedRect.right > productRect.right;
    }

    /**
     * Return distance from amount: positive if product is above amount
     *
     * @param amount  RawText containing possible amount
     * @param product RawText containing possible product price
     * @return distance
     */
    private static int getProductPosition(RawText amount, RawText product) {
        return Math.round(amount.getRect().top - product.getRect().top);
    }

    private static boolean isPossibleNumber(String s) {
        int counter = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isDigit(s.charAt(i)))
                ++counter;
        }
        return counter > s.length()/2;
    }
}
