package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ing.software.common.Ticket;
import com.ing.software.common.TicketError;
import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.annimon.stream.function.Consumer;

import static com.ing.software.ocr.AmountComparator.*;
import static com.ing.software.ocr.DataAnalyzer.*;

/**
 * Class to control ocr analysis
 * <p> This class is thread-safe. </p>
 *
 * <p>USAGE:</p>
 * <ol> Instantiate OcrManager; </ol>
 * <ol> Call initialize(context) until it returns 0;</ol>
 * <ol> Call getTicket(preproc, callback) ad libitum to extract information (Ticket object) from a photo of a ticket.</ol>
 * <ol> Call release() to release internal resources.</ol>
 */


public class OcrManager {

    private final OcrAnalyzer analyzer = new OcrAnalyzer();
    private boolean operative = false;

    /**
     * Initialize OcrAnalyzer
     *
     * @param context Android context
     * @return 0 if everything ok, negative number if an error occurred
     */
    public synchronized int initialize(@NonNull Context context) {
        OcrUtils.log(1, "OcrManager", "Initializing OcrManager");
        int r = analyzer.initialize(context);
        operative = r == 0;
        return r;
    }

    public synchronized void release() {
        operative = false;
        analyzer.release();
    }

    /**
     * Get a Ticket from an ImageProcessor. Some fields of the new ticket can be null.
     * <p> Possible errors inside Ticket.errors can be: </p>
     * <ul> INVALID_STATE: this OcrManager has not been properly initialized. </ul>
     * <ul> INVALID_PROCESSOR: the ImageProcessor passed as parameter is not valid. </ul>
     * <ul> AMOUNT_NOT_FOUND: the amount has not been found. </ul>
     * <ul> DATE_NOT_FOUND: the date has not been found. </ul>
     * @param imgProc ImageProcessor which has been set an image. Not null.
     * @return Ticket. Never null.
     *
     * @author Luca Michelon
     * @author Riccardo Zaglia
     */
    public synchronized Ticket getTicket(@NonNull ImageProcessor imgProc) {
        Ticket ticket = new Ticket();
        ticket.errors = new ArrayList<>();
        ticket.rectangle = imgProc.getCorners();
        if (!operative) {
            ticket.errors.add(TicketError.INVALID_STATE);
            return ticket;
        }

        long startTime = System.nanoTime();
        Bitmap frame = imgProc.undistortForOCR();
        if (frame == null) {
            ticket.errors.add(TicketError.INVALID_PROCESSOR);
            return ticket;
        }
        OcrResult result = analyzer.analyze(frame);
        Ticket newTicket = getTicketFromResult(result);
        long endTime = System.nanoTime();
        double duration = ((double) (endTime - startTime)) / 1000000000;
        OcrUtils.log(1, "EXECUTION TIME: ", duration + " seconds");

        ticket.amount = newTicket.amount;
        ticket.date = newTicket.date;
        if (ticket.amount == null)
            ticket.errors.add(TicketError.AMOUNT_NOT_FOUND);
        if (ticket.date == null)
            ticket.errors.add(TicketError.DATE_NOT_FOUND);
        return ticket;
    }

    /**
     * Asynchronous version of getTicket(imgProc). The ticket is passed by the callback parameter.
     * @param imgProc ImageProcessor which has been set an image. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     *
     * @author Riccardo Zaglia
     */
    public void getTicket(@NonNull ImageProcessor imgProc, @NonNull Consumer<Ticket> ticketCb) {
        new Thread(() -> ticketCb.accept(getTicket(imgProc))).start();
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
