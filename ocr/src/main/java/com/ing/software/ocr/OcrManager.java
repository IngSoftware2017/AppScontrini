package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrObjects.TicketSchemes.TicketScheme;
import com.ing.software.ocr.OperativeObjects.AmountComparator;
import com.ing.software.ocr.OperativeObjects.ListAmountOrganizer;
import com.ing.software.ocr.OperativeObjects.RawImage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.Stream;

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
     * <p> All possible errors inside {@link OcrTicket#errors} are listed in {@link OcrError}. </p>
     * @param imgProc {{@link ImageProcessor} which has been set an image. Not modified by this method. Not null.
     * @param options define which operations to execute and detection accuracy. Not Null
     * @return {@link OcrTicket} Never null.
     *
     * @author Luca Michelon
     * @author Riccardo Zaglia
     */
    public synchronized OcrTicket getTicket(@NonNull ImageProcessor imgProc, OcrOptions options) {
        ImageProcessor procCopy = new ImageProcessor(imgProc);
        OcrTicket ticket = extractTicket(procCopy, options);
        if (options.redoUpsideDown() && ticket.amount == null && ticket.restoredAmount == null) {
            procCopy.rotateUpsideDown();
            ticket = extractTicket(procCopy, options);
        }
        return ticket;
    }

    /**
     * Main core of ocrManager
     * @param imgProc copy of source image processor
     * @param options original options
     * @return result ticket
     */
    private OcrTicket extractTicket(@NonNull ImageProcessor imgProc, OcrOptions options) {
        long startTime = 0;
        long endTime = 1;
        OcrTicket ticket = new OcrTicket();
        ticket.errors = new ArrayList<>();
        if (!operative) {
            ticket.errors.add(OcrError.UNINITIALIZED);
            return ticket;
        }

        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        Bitmap frame = imgProc.undistortForOCR(options.getResolutionMultiplier());
        if (frame == null) {
            ticket.errors.add(OcrError.INVALID_PROCESSOR);
            return ticket;
        }
        ticket.rectangle = imgProc.getCorners();

        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "EXECUTION TIME: ", "Processor: " + duration + " seconds");
        }

        ticket.errors = new ArrayList<>();
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();

        mainImage = new RawImage(frame); //set main properties of image
        OcrUtils.log(1, "MANAGER: ", "Starting image analysis...");
        List<OcrText> lines = analyzer.analyze(frame); //Get main texts
        mainImage.setLines(lines); //save rect configuration in rawimage
        OcrSchemer.prepareScheme(lines); //Prepare scheme of the ticket
        OcrUtils.listEverything();

        OcrTicket newTicket = new OcrTicket();
        if (options.findDate()) {
            newTicket.date = DataAnalyzer.findDate(lines);
        }
        Scored<TicketScheme> bestTicket = null;
        if (options.findTotal()) {
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
            List<Scored<OcrText>> amountTexts = DataAnalyzer.getAmountTexts(lines); //1
            List<ListAmountOrganizer> amountList = DataAnalyzer.organizeAmountList(amountTexts); //2
            Collections.sort(amountList, Collections.reverseOrder());
            if (!amountList.isEmpty()) {
                List<Scored<OcrText>> amountPrice;
                int i = 0;
                int maxScan = options.contains(OcrLevels.EXTENDED_SEARCH) ? 3 : 1;
                while (i < amountList.size()) {
                    if (options.contains(OcrLevels.AMOUNT_DEEP) && i <= maxScan) {
                        //Redo ocr on first element of list.
                        amountPrice = analyzer.getAmountStripTexts(imgProc, amountList.get(i).getSourceText().obj());//3
                    } else {
                        //Use current texts to search target texts (with price)
                        amountPrice = OcrAnalyzer.getAmountOrigTexts(amountList.get(i).getSourceText().obj());
                    }
                    //set target texts (lines) to first source text
                    amountList.get(i).setAmountTargetTexts(amountPrice); //4
                    AmountComparator amountComparator;
                    Pair<OcrText, BigDecimal> amountT = DataAnalyzer.getMatchingAmount(amountList.get(i).getTargetTexts()); //5
                    if (amountT != null && newTicket.amount == null) {
                        newTicket.amount = amountT.second;
                        amountComparator = new AmountComparator(newTicket.amount, amountT.first); //8
                        bestTicket = amountComparator.getBestAmount(true);
                        if (bestTicket != null) {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best amount is: " + bestTicket.obj().getBestAmount().setScale(2, RoundingMode.HALF_UP) +
                                    "\nwith scheme: " + bestTicket.obj().toString() + "\nand score: " + bestTicket.getScore());
                        }
                    }
                    Pair<OcrText, BigDecimal> restoredAmountT = DataAnalyzer.getRestoredAmount(amountList.get(i).getTargetTexts()); //6
                    //todo: Redo ocr on prices strip if enabled //7

                    //todo: Initialize amount comparator with specific schemes if 'contante'/'resto'/'subtotale' are found
                    if (restoredAmountT != null && newTicket.restoredAmount == null) {
                        newTicket.restoredAmount = restoredAmountT.second;
                        amountComparator = new AmountComparator(newTicket.restoredAmount, restoredAmountT.first);
                        Scored<TicketScheme> bestRestoredTicket = amountComparator.getBestAmount(false);
                        if (bestRestoredTicket != null) {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best restored amount is: " + bestRestoredTicket.obj().getBestAmount().setScale(2, RoundingMode.HALF_UP) +
                                    "\nwith scheme: " + bestRestoredTicket.obj().toString() + "\nand score: " + bestRestoredTicket.getScore());
                            if (bestTicket == null || bestRestoredTicket.getScore() > bestTicket.getScore()) {
                                bestTicket = bestRestoredTicket;
                            }
                        }
                    }
                    if (bestTicket != null)
                        newTicket.restoredAmount = bestTicket.obj().getBestAmount(); //9
                    if (newTicket.amount != null && newTicket.restoredAmount != null) //temporary
                        break;
                    ++i;
                }
            }
        }
        if (options.findProducts()) {
            List<Pair<String, BigDecimal>> newProducts = null;
            if (bestTicket != null) {
                List<Pair<OcrText, BigDecimal>> prices = bestTicket.obj().getPricesList();
                newProducts = Stream.of(prices)
                        .map(price -> new Pair<>(Stream.of(OcrAnalyzer.getTextsOnleft(price.first)) //get texts for product name and concatenate these texts
                                .reduce("", (string, text) -> string + text.text() + " "), price.second))
                        .toList();
            }
            newTicket.products = newProducts;
        }

        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "EXECUTION TIME: ", "Core: " + duration + " seconds");
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
     * Asynchronous version of {@link #getTicket(ImageProcessor, OcrOptions)}.
     * The ticket is passed by the callback parameter.
     * @param imgProc {{@link ImageProcessor} which has been set an image. Not modified by this method. Not null.
     * @param options define which operations to execute and detection accuracy. Not Null
     * @param ticketCb callback to get the ticket. Not null.
     *
     * @author Riccardo Zaglia
     */
    public void getTicket(@NonNull ImageProcessor imgProc, OcrOptions options, @NonNull Consumer<OcrTicket> ticketCb) {
        new Thread(() -> ticketCb.accept(getTicket(imgProc, options))).start();
    }
}
