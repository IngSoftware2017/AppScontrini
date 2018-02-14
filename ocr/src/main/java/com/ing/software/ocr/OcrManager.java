package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.util.SizeF;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrObjects.TicketSchemes.TicketScheme;
import com.ing.software.ocr.OperativeObjects.*;

import java.math.*;
import java.util.*;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.Stream;

import static com.ing.software.common.CommonUtils.*;
import static com.ing.software.ocr.OcrOptions.REDO_OCR_3;
import static com.ing.software.ocr.OcrVars.*;
import static com.ing.software.ocr.OcrAnalyzer.*;
import static com.ing.software.ocr.DataAnalyzer.*;
import static java.util.Arrays.*;

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
     * @param context Android context
     * @return 0 if everything ok, negative number if an error occurred
     */
    public synchronized int initialize(@NonNull Context context) {
        OcrUtils.log(1, "OcrManager", "Initializing OcrManager");
        int r = analyzer.initialize(context);
        operative = (r == 0);
        return r;
    }

    /**
     * Release internal resources
     */
    public synchronized void release() {
        operative = false;
        analyzer.release();
    }

    /**
     * Single run ocr analysis.
     * @param ticket in-out ticket. Not null.
     * @param processor image processor. Not modified. Not null.
     * @param options ocr options. Not modified. Not null.
     *
     * @author Luca Michelon
     * @author Riccardo Zaglia
     */
    private void analysis(OcrTicket ticket, ImageProcessor processor, OcrOptions options) {
        long startTime;
        long endTime;
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        Bitmap frame = processor.undistortForOCR(options.getResolutionMultiplier());
        if (frame == null) {
            ticket.errors.add(OcrError.INVALID_PROCESSOR);
            return;
        }
        ticket.rectangle = processor.getCorners();

        mainImage = new RawImage(frame); //set main properties of image
        OcrUtils.log(1, "MANAGER: ", "Starting image analysis...");
        List<OcrText> texts = analyzer.analyze(frame); //Get main texts
        mainImage.setLines(texts); //save rect configuration in rawimage
        OcrSchemer.prepareScheme(texts); //Prepare scheme of the ticket
        OcrUtils.listEverything();

        Locale country = null, language = null;
        if (options.suggestedCountry != null
                && asList(Locale.getISOCountries()).contains(options.suggestedCountry.toString())) {
            country = options.suggestedCountry;
            language = COUNTRY_TO_LANGUAGE.get(country);
        }

        {   // if found, currency is a great estimator for the country locale
            Locale currencyCountry = getCurrencyCountry(texts);
            if (currencyCountry != null)
                country = currencyCountry;
        }
        ticket.currency = Currency.getInstance(country);
        //todo: update locale if text locale does not match currency locale

        OcrTicket newTicket = new OcrTicket();
        Scored<TicketScheme> bestTicket = null;
        if (options.shouldFindTotal) {
            /*
            Steps:
            1- search string in texts --> Data Analyzer --> Word Matcher. Find ticket language
            2- add score to chosen texts --> Score Func
            3- if enabled redo ocr on source strip --> OcrAnalyzer
            4- add score to target texts --> Score Func
            5- decode amount from target texts --> Data Analyzer --> Word Matcher
            6- decode restored amount from target texts --> Data Analyzer
            7 - if enabled redo ocr on prices strip and update mainImage (take old prices rect and overwrite old texts)
            8- check amount and restored amount against cash and products prices --> Amount Comparator
            9- set restored amount retrieving best amount from amount comparator
             */
            List<Scored<Pair<OcrText, Locale>>> amountStringsWithLocale = findAmountStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> subtotalStringsWithLocale = findSubtotalStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> cashStringsWithLocale = findCashStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> changeStringsWithLocale = findChangeStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> coverStringsWithLocale = findCoverStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> taxStringsWithLocale = findTaxStringTexts(texts); //1

            language = getBestLanguage(asList(amountStringsWithLocale, subtotalStringsWithLocale,
                    cashStringsWithLocale, changeStringsWithLocale, coverStringsWithLocale,
                    taxStringsWithLocale));

            List<Scored<OcrText>> amountStrings = filterForLanguage(amountStringsWithLocale, language);
            List<Scored<OcrText>> subtotalStrings = filterForLanguage(subtotalStringsWithLocale, language);
            List<Scored<OcrText>> cashStrings = filterForLanguage(cashStringsWithLocale, language);
            List<Scored<OcrText>> changeStrings = filterForLanguage(changeStringsWithLocale, language);
            List<Scored<OcrText>> coverStrings = filterForLanguage(coverStringsWithLocale, language);
            List<Scored<OcrText>> taxStrings = filterForLanguage(taxStringsWithLocale, language);




            List<ListAmountOrganizer> amountList = DataAnalyzer.organizeAmountList(amountStrings); //2
            Collections.sort(amountList, Collections.reverseOrder());
            if (!amountList.isEmpty()) {
                List<Scored<OcrText>> amountPrice; //ZAGLIA: the identifier is misleading, use amountStringTexts instead
                int i = 0;
                int maxScan = options.precision >= REDO_OCR_3 ? 3 : 1;
                while (i < amountList.size()) {
                    OcrText amountStringText = amountList.get(i).getSourceText().obj();
                    RectF stripBoundingBox = getAmountExtendedBox(amountStringText, mainImage.getWidth());
                    if (options.precision >= OcrOptions.REDO_OCR_PRECISION && i <= maxScan) {
                        //Redo ocr on first element of list. todo: add scan for more sources if precision > 4
                        amountPrice = analyzer.getAmountStripTexts(processor,
                                new SizeF(mainImage.getWidth(), mainImage.getHeight()), amountStringText, stripBoundingBox);//3
                    } else {
                        //Use current texts to search target texts (with price)
                        amountPrice = OcrAnalyzer.getAmountOrigTexts(amountStringText, stripBoundingBox);
                    }
                    //set target texts (lines) to first source text
                    amountList.get(i).setAmountTargetTexts(amountPrice); //4
                    AmountComparator amountComparator;
                    Pair<OcrText, BigDecimal> amountT = DataAnalyzer.getMatchingAmount(amountList.get(i).getTargetTexts()); //5
                    if (amountT != null && newTicket.total == null) {
                        newTicket.total = amountT.second;
                        amountComparator = new AmountComparator(newTicket.total, amountT.first); //8
                        bestTicket = amountComparator.getBestAmount(true);
                        if (bestTicket != null) {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best amount is: " + bestTicket.obj().getBestAmount().setScale(2, RoundingMode.HALF_UP) +
                                    "\nwith scheme: " + bestTicket.obj().toString() + "\nand score: " + bestTicket.getScore());
                        }
                    }
                    Pair<OcrText, BigDecimal> restoredAmountT = DataAnalyzer.getRestoredAmount(amountList.get(i).getTargetTexts()); //6
                    //todo: Redo ocr on prices strip if enabled //7

                    //todo: Initialize amount comparator with specific schemes if 'contante'/'resto'/'subtotale' are found
                    if (restoredAmountT != null && newTicket.total == null) {
                        newTicket.total = restoredAmountT.second;
                        amountComparator = new AmountComparator(newTicket.total, restoredAmountT.first);
                        Scored<TicketScheme> bestRestoredTicket = amountComparator.getBestAmount(false);
                        if (bestRestoredTicket != null) {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best restored amount is: " + bestRestoredTicket.obj().getBestAmount().setScale(2, RoundingMode.HALF_UP) +
                                    "\nwith scheme: " + bestRestoredTicket.obj().toString() + "\nand score: " + bestRestoredTicket.getScore());
                            newTicket.total = bestRestoredTicket.obj().getBestAmount(); //9
                            newTicket.errors.add(OcrError.UNCERTAIN_AMOUNT);
                            if (bestTicket == null || bestRestoredTicket.getScore() > bestTicket.getScore()) {
                                bestTicket = bestRestoredTicket;
                            }
                        }
                    }
                    if (newTicket.total != null) //temporary
                        break;
                    ++i;
                }
            }

            ticket.containsCover = coverStrings.size() > 0;
        }

        //todo: from total price text:
        {
            OcrText totalPriceT = null;
            if (totalPriceT != null) {
                ticket.totalRect = ImageProcessor.normalizeCoordinates(totalPriceT.box(), size(frame));
            }
        }

        if (options.shouldFindProducts && false) { // disable for now
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

        ticket.total = newTicket.total;
        ticket.products = newTicket.products;
        ticket.errors.addAll(newTicket.errors);

        //NB: this is a fallback, a language could be associated to multiple countries, only one is chosen.
        if (country == null) {
            country = LANGUAGE_TO_COUNTRY.get(language);
        }
        //update ticket currency
        ticket.currency = Currency.getInstance(country);

        if (options.shouldFindDate) {
            //todo: depending on the level of country certainty, choose suggested or forced country:
            Pair<OcrText, Date> pair = findDate(texts, null, country);
            if (pair != null)
                ticket.date = pair.second;
        }

        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 10e9;
            OcrUtils.log(1, "EXECUTION TIME: ", duration + " seconds");
        }
    }

    /**
     * Get a Ticket from an ImageProcessor. Some fields of the new ticket can be null.
     * <p> All possible errors inside {@link OcrTicket#errors} are listed in {@link OcrError}. </p>
     * @param imgProc {{@link ImageProcessor} which has been set an image. Not modified by this method. Not null.
     * @param options define which operations to execute and detection accuracy. Not Null
     * @return {@link OcrTicket} Never null.
     *
     * @author Riccardo Zaglia
     */
    public synchronized OcrTicket getTicket(@NonNull ImageProcessor imgProc, OcrOptions options) {
        ImageProcessor procCopy = new ImageProcessor(imgProc);

        OcrTicket ticket = new OcrTicket();
        ticket.errors = new HashSet<>();
        if (!operative) {
            ticket.errors.add(OcrError.UNINITIALIZED);
            return ticket;
        }

        analysis(ticket, procCopy, options);

        //if should find total and total is null, or should not find total but should find date and date is null,
        // then redo analysis
        boolean upsideDownAnlaysis = false;
        if (options.canRetryUpsideDown
                && ((options.shouldFindTotal && ticket.total == null)
                || (!options.shouldFindTotal && options.shouldFindDate && ticket.date != null))) {
            procCopy.rotate(2);
            analysis(ticket, procCopy, options);
            upsideDownAnlaysis = true;
        }

        if (options.shouldFindTotal && ticket.total == null) {
            ticket.errors.add(OcrError.AMOUNT_NOT_FOUND);
        }
        if (options.shouldFindDate && ticket.date == null) {
            ticket.errors.add(OcrError.DATE_NOT_FOUND);
        }
        if (upsideDownAnlaysis && !ticket.errors.contains(OcrError.AMOUNT_NOT_FOUND)
                && !ticket.errors.contains(OcrError.AMOUNT_NOT_FOUND)) {
            ticket.errors.add(OcrError.UPSIDE_DOWN);
        }
        return ticket;
    }

    /**
     * Asynchronous version of {@link #getTicket(ImageProcessor, OcrOptions)}.
     * The {@link OcrTicket} is passed by the callback parameter.
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
