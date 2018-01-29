package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrError;
import com.ing.software.ocr.OcrObjects.OcrOptions;
import com.ing.software.ocr.OcrObjects.OcrTicket;
import com.ing.software.ocr.OcrObjects.TempText;
import com.ing.software.ocr.OcrObjects.TicketSchemes.TicketScheme;
import com.ing.software.ocr.OperativeObjects.AmountComparator;
import com.ing.software.ocr.OperativeObjects.ListAmountOrganizer;
import com.ing.software.ocr.OperativeObjects.RawImage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.annimon.stream.function.Consumer;

import static com.ing.software.ocr.OcrObjects.OcrOptions.REDO_OCR_3;
import static com.ing.software.ocr.OcrVars.*;

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
    public static RawImage mainImage;
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
        operative = (r == 0);
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
     * @param options define which operations to execute
     * @return Ticket. Never null.
     *
     * @author Luca Michelon
     * @author Riccardo Zaglia
     */
    public synchronized OcrTicket getTicket(@NonNull ImageProcessor imgProc, OcrOptions options) {
        long startTime = 0;
        long endTime = 1;
        ImageProcessor procCopy = new ImageProcessor(imgProc);

        //todo: for advanced mode, redo ocr with upside down bitmap.
        // procCopy.rotateUpsideDown();
        // frame = procCopy.undistortForOCR(1.);

        OcrTicket ticket = new OcrTicket();
        ticket.errors = new ArrayList<>();
        if (!operative) {
            ticket.errors.add(OcrError.UNINITIALIZED);
            return ticket;
        }

        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        Bitmap frame;
        if (options.getPrecision() == 0)
            frame = procCopy.undistortForOCR(1. / 3.);
        else
            frame = procCopy.undistortForOCR(options.getPrecision()>1 ? 1. : 1. / 2.);
        if (frame == null) {
            ticket.errors.add(OcrError.INVALID_PROCESSOR);
            return ticket;
        }
        ticket.rectangle = procCopy.getCorners();

        mainImage = new RawImage(frame); //set main properties of image
        OcrUtils.log(1, "MANAGER: ", "Starting image analysis...");
        List<TempText> lines = analyzer.analyze(frame); //Get main texts
        mainImage.setLines(lines); //save rect configuration in rawimage
        OcrSchemer.prepareScheme(lines); //Prepare scheme of the ticket
        mainImage.textFitter(); //save configuration from prepareScheme in rawimage
        OcrUtils.listEverything(lines);

        OcrTicket newTicket = new OcrTicket();
        if (options.isFindDate()) {
            Date newDate = null;
            newTicket.date = newDate;
        }
        if (options.isFindTotal()) {
            /*
            Steps:
            1- search string in texts --> Data Analyzer --> Word Matcher
            2- add score to chosen texts --> Score Func
            3- if enabled redo ocr on source strip --> OcrAnalyzer
            4- add score to target texts --> Score Func
            5- decode amount from target texts --> Data Analyzer --> Word Matcher
            6- decode restored amount from target texts --> Data Analyzer
            7 - if enabled redo ocr on prices strip and update mainImage (take old prices rect and overwrite old texts)
            8- check amount and restored amount against cash and products prices --> Amount Comparator
            9- set restored amount retrieving best amount from amount comparator
             */
            List<Scored<TempText>> amountTexts = DataAnalyzer.getAmountTexts(lines); //1
            List<ListAmountOrganizer> amountList = DataAnalyzer.organizeAmountList(amountTexts); //2
            Collections.sort(amountList, Collections.reverseOrder());
            if (!amountList.isEmpty()) {
                List<Scored<TempText>> amountPrice;
                int i = 0;
                int maxScan = options.getPrecision() >= REDO_OCR_3 ? 3 : 1;
                while (i < amountList.size() && i <= maxScan) {
                    if (options.getPrecision() >= OcrOptions.REDO_OCR_PRECISION) {
                        //Redo ocr on first element of list. todo: add scan for more sources if precision > 4
                        amountPrice = analyzer.getAmountStripTexts(procCopy, amountList.get(i).getSourceText().obj());//3
                    } else {
                        //Use current texts to search target texts (with price)
                        amountPrice = OcrAnalyzer.getAmountOrigTexts(amountList.get(i).getSourceText().obj());
                    }
                    //set target texts (lines) to first source text
                    amountList.get(i).setAmountTargetTexts(amountPrice); //4
                    AmountComparator amountComparator;
                    Pair<TempText, BigDecimal> amountT = DataAnalyzer.getMatchingAmount(amountList.get(i).getTargetTexts()); //5
                    if (amountT != null) {
                        newTicket.amount = amountT.second;
                        amountComparator = new AmountComparator(newTicket.amount, amountT.first); //8
                        Scored<Pair<BigDecimal, TicketScheme>> bestAmount = amountComparator.getBestAmount();
                        OcrUtils.log(2, "MANAGER.Comparator", "Best amount is: " + bestAmount.obj().first.setScale(2, RoundingMode.HALF_UP) +
                                "\nwith scheme: " + bestAmount.obj().second + "\nand score: " + bestAmount.getScore());
                    }
                    Pair<TempText, BigDecimal> restoredAmountT = DataAnalyzer.getRestoredAmount(amountList.get(i).getTargetTexts()); //6
                    //todo: Redo ocr on prices strip if enabled //7

                    //todo: Initialize amount comparator with specific schemes if 'contante'/'resto'/'subtotale' are found
                    if (restoredAmountT != null) {
                        newTicket.restoredAmount = restoredAmountT.second;
                        amountComparator = new AmountComparator(newTicket.restoredAmount, restoredAmountT.first);
                        Scored<Pair<BigDecimal, TicketScheme>> bestRestoredAmount = amountComparator.getBestAmount();
                        OcrUtils.log(2, "MANAGER.Comparator", "Best restored amount is: " + bestRestoredAmount.obj().first.setScale(2, RoundingMode.HALF_UP) +
                                "\nwith scheme: " + bestRestoredAmount.obj().second + "\nand score: " + bestRestoredAmount.getScore());
                        newTicket.restoredAmount = bestRestoredAmount.obj().first; //9
                    }
                    if (newTicket.amount != null || newTicket.restoredAmount != null) //temporary
                        break;
                    ++i;
                }
            }
        }
        if (options.isFindProducts()) {
            List<Pair<String, BigDecimal>> newProducts = null;
            newTicket.products = newProducts;
        }

        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "EXECUTION TIME: ", duration + " seconds");
        }

        ticket.amount = newTicket.amount;
        ticket.restoredAmount = newTicket.restoredAmount;
        ticket.date = newTicket.date;
        ticket.products = newTicket.products;
        if (ticket.amount == null) {
            ticket.errors.add(OcrError.AMOUNT_NOT_FOUND);
        }
        if (ticket.date == null) {
            ticket.errors.add(OcrError.DATE_NOT_FOUND);
        }
        return ticket;
    }

    /**
     * Asynchronous version of getTicket(imgProc). The ticket is passed by the callback parameter.
     * @param imgProc ImageProcessor which has been set an image. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     *
     * @author Riccardo Zaglia
     */
    public void getTicket(@NonNull ImageProcessor imgProc, OcrOptions options, @NonNull Consumer<OcrTicket> ticketCb) {
        new Thread(() -> ticketCb.accept(getTicket(imgProc, options))).start();
    }
}
