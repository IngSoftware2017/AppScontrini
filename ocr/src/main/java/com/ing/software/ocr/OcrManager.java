package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawText;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.annimon.stream.function.Consumer;

import static com.ing.software.ocr.AmountComparator.*;
import static com.ing.software.ocr.DataAnalyzer.*;

/*
*/
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
     * Get a Ticket from an ImagePreprocessor. Some fields of the new ticket can be null.
     * @param preprocessor ImagePreprocessor. Not null.
     * @param ticketCb     callback to get the ticket. Not null.
     *
     * @author Luca Michelon
     * @author Riccardo Zaglia
     */
    public void getTicket(@NonNull ImagePreprocessor preprocessor, @NonNull Consumer<Ticket> ticketCb) {
        new Thread(() -> {
            synchronized (this) {
                if (!operative)
                    return;
                long startTime = System.nanoTime();
                Bitmap bm = preprocessor.undistort(0.05);
                bm = scaleBitmap(bm); //for tests
                OcrResult result = analyzer.analyze(bm);
                ticketCb.accept(getTicketFromResult(result));
                long endTime = System.nanoTime();
                double duration = ((double) (endTime - startTime)) / 1000000000;
                OcrUtils.log(1, "EXECUTION TIME: ", duration + " seconds");
            }
        }).start();
    }

    /**
     * Scale a bitmap to 1/2 its height and width
     * @param b bitmap not null
     * @return scaled bitmap
     */
    private Bitmap scaleBitmap(Bitmap b) {
        int reqWidth = b.getWidth()/2;
        int reqHeight = b.getHeight()/2;
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
    }

    /**
     * @author Michelon
     * Coverts an OcrResult into a Ticket analyzing its data
     * @param result OcrResult to analyze. Not null.
     * @return Ticket. Some fields can be null;
     */
    private static Ticket getTicketFromResult(OcrResult result) {
        long startTime = System.nanoTime();
        Ticket ticket = new Ticket();
        OcrUtils.log(6, "OCR RESULT", result.toString());
        List<RawGridResult> dateList = result.getDateList();
        List<RawText> prices = OcrSchemer.getPricesTexts(result.getProducts());
        ticket.amount = extendedAmountAnalysis(getPossibleAmounts(result.getAmountResults()), prices);
        ticket.date = getDateFromList(getPossibleDates(result.getDateList()));
        long endTime = System.nanoTime();
        double duration = ((double) (endTime - startTime)) / 1000000000;
        OcrUtils.log(1, "getTicketFromResult", "EXECUTION TIME: " + duration + " sec");
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

    /**
     * Extracts first not null date from list of ordered dates
     * @param dateList list of ordered RawGridResults containing possible dates. Not null
     * @return First possible date. Null if nothing found
     */
    private static Date getDateFromList(@NonNull List<RawGridResult> dateList) {
        for (RawGridResult gridResult : dateList) {
            String possibleDate = gridResult.getText().getDetection();
            OcrUtils.log(2, "getDateFromList", "Possible date is: " + possibleDate);
            Date evaluatedDate = DataAnalyzer.getDate(possibleDate);
            if (evaluatedDate != null) {
                OcrUtils.log(2, "getDateFromList", "Possible amount is: " + evaluatedDate.toString());
                return evaluatedDate;
            }
        }
        return null;
    }
}
