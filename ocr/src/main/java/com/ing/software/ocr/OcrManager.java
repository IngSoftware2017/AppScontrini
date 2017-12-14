package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.annimon.stream.function.Consumer;

import static com.ing.software.ocr.AmountComparator.*;
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

    /**
     * @author Zaglia
     */
    class AnalyzeRequest {
        ImagePreprocessor preproc;
        Consumer<Ticket> cb;

        AnalyzeRequest(ImagePreprocessor preprocessor, Consumer<Ticket> ticketCb) {
            preproc = preprocessor;
            cb = ticketCb;
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
     * @param preprocessor ImagePreprocessor. Not null.
     * @param ticketCb     callback to get the ticket. Not null.
     */
    public void getTicket(@NonNull ImagePreprocessor preprocessor, final Consumer<Ticket> ticketCb) {
        analyzeQueue.add(new OcrManager.AnalyzeRequest(preprocessor, ticketCb));
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
            new Thread(() -> {
                while (!analyzeQueue.isEmpty()) {
                    AnalyzeRequest req = analyzeQueue.remove();
                    long startTime = System.nanoTime();
                    Bitmap bm = req.preproc.undistort(0.05);
                    OcrResult result = analyzer.analyze(bm);
                    req.cb.accept(getTicketFromResult(result));
                    long endTime = System.nanoTime();
                    double duration = ((double) (endTime - startTime)) / 1000000000;
                    OcrUtils.log(1, "EXECUTION TIME: ", duration + " seconds");
                }
            }).start();
            analyzing = false;
        }
    }

    /**
     * @author Michelon
     * Coverts an OcrResult into a Ticket analyzing its data
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

    /**
     * @author Michelon
     * @date 9-12-17
     * Analyze possible RawTexts containing amount. When one is found check if it consistent with list of products,
     * subtotal, cash and change.
     * @param possibleResults List of RawGridResults containing possible amount. Not null.
     * @param products List of RawTexts containing possible prices for products. Not null.
     * @return BigDecimal containing detected amount. null if nothing found.
     */
    private static BigDecimal extendedAmountAnalysis(@NonNull List<RawGridResult> possibleResults, @NonNull List<RawText> products) {
        BigDecimal amount = null;
        RawText amountText = null;
        for (RawGridResult result : possibleResults) {
            String amountString = result.getText().getDetection();
            OcrUtils.log(2, "getPossibleAmount", "Possible amount is: " + amountString);
            amount = DataAnalyzer.analyzeAmount(amountString);
            if (amount != null) {
                OcrUtils.log(2, "getPossibleAmount", "Decoded value: " + amount);
                amountText = result.getText();
                break;
            }
        }
        if (amount != null) {
            AmountComparator amountComparator = new AmountComparator(amountText, amount);
            //check against list of products and cash + change
            List<RawGridResult> possiblePrices = getPricesList(amountText, products);
            amountComparator.analyzePrices(possiblePrices);
            amountComparator.analyzeTotals(possiblePrices);
            amount = amountComparator.getBestAmount();
        }
        return amount;
    }
}
