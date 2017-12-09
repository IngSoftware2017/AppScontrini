package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ing.software.ocr.DataAnalyzer.*;

/*
USAGE:
1) Instantiate OcrManager;
2) Call initialize(context) until it returns 0;
3) Call getTicket(bitmap, callback) ad libitum to extract information (Ticket object) from a photo of a ticket.
4) Call release() to release internal resources.
*/
/**
 * Class to control ocr analysis
 */

public class OcrManager {

    private final OcrAnalyzer analyzer = new OcrAnalyzer();

    class AnalyzeRequest {
        Bitmap photo;
        OnTicketReadyListener ticketCb;

        AnalyzeRequest(Bitmap bm, OnTicketReadyListener cb) {
            photo = bm;
            ticketCb = cb;
        }
    }

    private Queue<OcrManager.AnalyzeRequest> analyzeQueue = new ConcurrentLinkedQueue<>();
    private boolean analyzing = false;

    /**
     * Initialize OcrAnalyzer
     *
     * @param context Android context
     * @return 0 if everything ok, negative number if an error occurred
     */
    public int initialize(Context context) {
        OcrUtils.log(1, "OcrManager", "Initializing OcrManager");
        return analyzer.initialize(context);
    }

    public void release() {
        analyzer.release();
    }

    /**
     * Get a Ticket from a Bitmap. Some fields of the new ticket can be null.
     *
     * @param photo    Bitmap. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     */
    public void getTicket(@NonNull Bitmap photo, final OnTicketReadyListener ticketCb) {
        analyzeQueue.add(new OcrManager.AnalyzeRequest(photo, ticketCb));
        dispatchAnalysis();
    }

    /**
     * Handle analysis requests
     *
     * @author Michelon
     * @author Zaglia
     */
    private void dispatchAnalysis() {
        if (!analyzing) {
            analyzing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!analyzeQueue.isEmpty()) {
                        final OcrManager.AnalyzeRequest req = analyzeQueue.remove();
                        final long startTime = System.nanoTime();
                        OcrResult result = analyzer.analyze(req.photo);
                        req.ticketCb.onTicketReady(getTicketFromResult(result));
                        long endTime = System.nanoTime();
                        double duration = ((double) (endTime - startTime)) / 1000000000;
                        OcrUtils.log(1, "EXECUTION TIME: ", duration + " seconds");
                    }
                }
            }).start();
            analyzing = false;
        }
    }

    /**
     * Coverts an OcrResult into a Ticket analyzing its data
     *
     * @param result OcrResult to analyze. Not null.
     * @return Ticket. Some fields can be null;
     */
    private static Ticket getTicketFromResult(OcrResult result) {
        Ticket ticket = new Ticket();
        OcrUtils.log(6, "OCR RESULT", result.toString());
        List<RawGridResult> dateList = result.getDateList();
        AmountAnalyzer amountAnalyzer = new AmountAnalyzer();
        ticket.amount = extendedAmountAnalysis(amountAnalyzer, getPossibleAmounts(result.getAmountResults()), result.getProducts());
        return ticket;
    }

    private static BigDecimal extendedAmountAnalysis(AmountAnalyzer amountAnalyzer, List<RawGridResult> possibleResults, List<RawBlock> products) {
        BigDecimal amount = null;
        for (RawGridResult result : possibleResults) {
            String amountString = result.getText().getDetection();
            OcrUtils.log(2, "getPossibleAmount", "Possible amount is: " + amountString);
            amount = analyzeAmount(amountString);
            if (amount != null) {
                OcrUtils.log(2, "getPossibleAmount", "Decoded value: " + amount);
                return amount;
            }
        }
        if (amount != null) {
            //confronta con lista product
        }
        return amount;
    }

    private static void analyzePrices(AmountAnalyzer amountAnalyzer, List<RawGridResult> possiblePrices, BigDecimal decodedAmount) {
        int maxDistance = 1; //only 1 miss in amount detection
        //above amount we can have all prices and a subtotal, so first sum all products with distance > 0
        Collections.sort(possiblePrices);
        if (possiblePrices.size() == 0 && possiblePrices.get(0).getPercentage() > 0)
            return;
        BigDecimal productsSum = null;
        BigDecimal possibleSubTotal = null;
        int index = 0;
        //Search for first parsable product price
        while (productsSum == null && index < possiblePrices.size() && possiblePrices.get(index).getPercentage() > 0) {
            productsSum = analyzeAmount(possiblePrices.get(index).getText().getDetection());
            ++index;
        }
        if (productsSum != null)
            OcrUtils.log(3, "analyzePrices", "List of prices, first value is: " + productsSum.toString());
        while (index < possiblePrices.size()) {
            if (possiblePrices.get(index).getPercentage() <= 0)
                break;
            BigDecimal adder = analyzeAmount(possiblePrices.get(index).getText().getDetection());
            if (adder != null) {
                productsSum = productsSum.add(adder);
                possibleSubTotal = adder;
            }
            OcrUtils.log(3, "analyzePrices", "List of prices, new total value is: " + productsSum.toString());
            ++index;
        }
        if (productsSum != null) {
            //Check if my subtotal is the same as total
            if (decodedAmount.compareTo(possibleSubTotal) == 0) {
                //Accept the value
                amountAnalyzer.flagHasSubtotal(possibleSubTotal);
            }
            //now we may have the same value of decodedAmount, or its double (=*2)
            if (decodedAmount.compareTo(productsSum) == 0) {
                OcrUtils.log(3, "analyzePrices", "List of prices equals decoded amount");
                amountAnalyzer.flagHasPriceList(productsSum);
            } else if (decodedAmount.compareTo(productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP))) == 0) {
                OcrUtils.log(3, "analyzePrices", "List of prices/2 equals decoded amount");
                amountAnalyzer.flagHasPriceList(productsSum);
                //decodedAmount = productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            } else if (OcrUtils.findSubstring(productsSum.toString(), decodedAmount.toString()) <= maxDistance) {
                //It's acceptable a distance of 1 from target
                amountAnalyzer.flagHasPriceList(productsSum);
                OcrUtils.log(3, "analyzePrices", "List of prices diffs from decoded amount by 1");
            } else if (OcrUtils.findSubstring(productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP)).toString(), decodedAmount.toString()) <= maxDistance) {
                OcrUtils.log(3, "analyzePrices", "List of prices/2 diffs from decoded amount by 1");
                amountAnalyzer.flagHasPriceList(productsSum);
                //decodedAmount = productsSum.divide(new BigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            } else {
                OcrUtils.log(3, "analyzePrices", "List of prices id not a valid input");
            }
        }

    }

    //under total we have "contante" or "contante" + "resto"
    private static void analyzeTotals() {

    }

    /**
     * Retrieves all texts from products on the same 'column' as amount
     *
     * @param amountText RawText containing possible amount
     * @param products   List of RawTexts containing products (both name and price)
     * @return List of texts above or under amount with distance from amount (positive = above)
     */
    private static List<RawGridResult> getPricesList(RawText amountText, List<RawBlock> products) {
        List<RawGridResult> possibleAmounts = new ArrayList<>();
        for (RawBlock product : products) {
            for (RawText productText : product.getTexts()) {
                if (isProductPrice(amountText, productText)) {
                    int distanceFromSource = getProductPosition(amountText, productText);
                    possibleAmounts.add(new RawGridResult(productText, distanceFromSource));
                }
            }
        }
        return possibleAmounts;
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
        RectF extendedRect = partialExtendWidthRect(amount.getRect(), percentage);
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

    /**
     * Extends width of rect according to percentage
     *
     * @param originalRect rect containing amount
     * @param percentage   percentage of the width of the rect to extend
     * @return extended rect
     */
    private static RectF partialExtendWidthRect(RectF originalRect, int percentage) {
        float width = originalRect.width();
        float left = originalRect.left - (width * percentage / 2);
        float right = originalRect.right + (width * percentage / 2);
        return new RectF(left, originalRect.top, right, originalRect.bottom);
    }

}
class AmountAnalyzer {

    private int precision = 0;
    private boolean hasSubtotal = false;
    private boolean hasPriceList = false;
    private BigDecimal subTotal = null;
    private BigDecimal priceList = null;

    private void addPrecision() {
        ++precision;
    }

    void flagHasSubtotal(BigDecimal subTotal) {
        hasSubtotal = true;
        this.subTotal = subTotal;
        addPrecision();
    }

    void flagHasPriceList(BigDecimal priceList) {
        hasPriceList = true;
        this.priceList = priceList;
        addPrecision();
    }

    int getPrecision() {
        return precision;
    }
}
