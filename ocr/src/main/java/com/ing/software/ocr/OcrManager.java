package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ing.software.ocr.AmountAnalyzer.*;
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
        List<RawText> prices = OcrSchemer.getPricesTexts(result.getProducts());
        ticket.amount = extendedAmountAnalysis(getPossibleAmounts(result.getAmountResults()), prices);
        return ticket;
    }

    private static BigDecimal extendedAmountAnalysis(List<RawGridResult> possibleResults, List<RawText> products) {
        BigDecimal amount = null;
        RawText amountText = null;
        for (RawGridResult result : possibleResults) {
            String amountString = result.getText().getDetection();
            OcrUtils.log(2, "getPossibleAmount", "Possible amount is: " + amountString);
            amount = analyzeAmount(amountString);
            if (amount != null) {
                OcrUtils.log(2, "getPossibleAmount", "Decoded value: " + amount);
                amountText = result.getText();
                break;
            }
        }
        if (amount != null) {
            AmountAnalyzer amountAnalyzer = new AmountAnalyzer(amountText, amount);
            //check against list of products and cash + change
            List<RawGridResult> possiblePrices = getPricesList(amountText, products);
            amountAnalyzer.analyzePrices(possiblePrices);
            amountAnalyzer.analyzeTotals(possiblePrices);
        }
        return amount;
    }

    private static BigDecimal getBestAmount(AmountAnalyzer amountAnalyzer) {
        HashMap<String, Boolean> flags = amountAnalyzer.getFlags();
        boolean hasSubtotal = flags.get("hasSubtotal");
        boolean hasPriceList = flags.get("hasPriceList");
        boolean hasCash = flags.get("hasCash");
        boolean hasChange = flags.get("hasChange");
        if (amountAnalyzer.getPrecision() > 0) {
            int distanceFromSubtotal = -1;
            int distanceFromPriceList = -1;
            int distanceFromCash = -1;
            if (hasSubtotal)
                distanceFromSubtotal = OcrUtils.findSubstring(amountAnalyzer.getSubTotal().toString(), amountAnalyzer.getAmount().toString());
            if (hasPriceList)
                distanceFromPriceList = OcrUtils.findSubstring(amountAnalyzer.getPriceList().toString(), amountAnalyzer.getAmount().toString());
            if (hasCash)
                distanceFromCash = OcrUtils.findSubstring(amountAnalyzer.getCash().toString(), amountAnalyzer.getAmount().toString());
            if (hasChange)
                distanceFromCash = OcrUtils.findSubstring(amountAnalyzer.getCash().subtract(amountAnalyzer.getChange()).toString(), amountAnalyzer.getAmount().toString());
            //analyze all possible cases
            BigDecimal subtotal = null;
            BigDecimal cash = null;
            BigDecimal prices = null;
            BigDecimal amount = amountAnalyzer.getAmount();
            if (distanceFromCash > -1 && !hasChange)
                cash = amountAnalyzer.getCash();
            else if (distanceFromCash > -1)
                cash = amountAnalyzer.getCash().subtract(amountAnalyzer.getChange());
            if (distanceFromPriceList > -1)
                prices = amountAnalyzer.getPriceList();
            if (distanceFromSubtotal > -1)
                subtotal = amountAnalyzer.getSubTotal();
            if (distanceFromSubtotal > -1 && distanceFromPriceList > -1 && distanceFromCash > -1) {
                boolean cashSubtotal = cash.compareTo(subtotal)==0;
                boolean cashPrices = cash.compareTo(prices)==0;
                boolean cashAmount = cash.compareTo(amount)==0;
                boolean subtotalPrices = subtotal.compareTo(prices)==0;
                boolean subtotalAmount = subtotal.compareTo(amount)==0;
                //i have all three values + amount, if three of them are equals use that value
                if ((cashSubtotal && cashAmount) || (cashPrices && cashSubtotal) || (cashPrices && cashAmount))
                    return cash;
                else if (subtotalPrices && subtotalAmount)
                    return subtotal;
                //Here i don't have three equal values

            }
            return amountAnalyzer.getAmount();
        } else
            return amountAnalyzer.getAmount();
    }

}
