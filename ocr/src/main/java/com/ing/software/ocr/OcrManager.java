package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
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
import java.util.*;
import com.annimon.stream.function.Consumer;

import com.annimon.stream.Stream;

import static com.ing.software.ocr.OcrVars.*;
import static java.util.Arrays.asList;
import static com.ing.software.common.CommonUtils.*;
import static com.ing.software.ocr.DataAnalyzer.*;
import static com.ing.software.ocr.OcrOptions.*;

/**
 * Class to control ocr analysis
 * <p> This class is thread-safe. </p>
 *
 * <p>USAGE:</p>
 * <ol> Instantiate {@link OcrManager}; </ol>
 * <ol> Call {@link #initialize(Context)} until it returns 0;</ol>
 * <ol> Call {@link #getTicket(ImageProcessor, OcrOptions)} ad libitum to extract information (Ticket object) from a photo of a ticket.</ol>
 * <ol> Call {@link #release()} to release internal resources.</ol>
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
        operative = (r == 0);
        return r;
    }

    public synchronized void release() {
        operative = false;
        analyzer.release();
    }

    /**
     * Single run ocr analysis.
     * @param ticket in-out ticket. Not null.
     * @param imgProc image processor. Not modified. Not null.
     * @param options ocr options. Not modified. Not null.
     *
     * @author Luca Michelon
     * @author Riccardo Zaglia
     */
    private void extractTicket(OcrTicket ticket, ImageProcessor imgProc, OcrOptions options) {
        long startTime;
        long endTime;
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();
        Bitmap frame = imgProc.undistortForOCR(options.getResolutionMultiplier());
        if (frame == null) {
            ticket.errors.add(OcrError.INVALID_PROCESSOR);
            return;
        }
        ticket.rectangle = imgProc.getCorners();

        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 1000000000;
            OcrUtils.log(1, "EXECUTION TIME: ", "Processor: " + duration + " seconds");
        }

        ticket.errors = new HashSet<>();
        if (IS_DEBUG_ENABLED)
            startTime = System.nanoTime();

        RawImage mainImage = new RawImage(frame);
        analyzer.setMainImage(mainImage);
        OcrUtils.log(1, "MANAGER: ", "Starting image analysis...");
        List<OcrText> texts = analyzer.analyze(frame); //Get main texts
        mainImage.setLines(texts); //save rect configuration in rawimage and Prepare scheme of the ticket
        OcrUtils.listEverything(mainImage);

        Locale country = null, language = null;
        if (options.suggestedCountry != null
                && COUNTRY_TO_LANGUAGE.keySet().contains(options.suggestedCountry)) {
            country = options.suggestedCountry;
            language = COUNTRY_TO_LANGUAGE.get(country);
        }

        { // if found, currency is a great estimator for the country locale
            Locale currencyCountry = getCurrencyCountry(texts);
            if (currencyCountry != null) {
                country = currencyCountry;
            }
        }

        OcrTicket newTicket = new OcrTicket();

        BigDecimal restoredAmount = null;
        OcrText amountPriceText = null;
        OcrText restoredAmountPriceText = null;
        Scored<TicketScheme> bestTicket = null;
        if (options.totalSearch != TotalSearch.SKIP) {
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

            List<Scored<Pair<OcrText, Locale>>> amountStringsWithLocale = DataAnalyzer.findAmountStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> subtotalStringsWithLocale = DataAnalyzer.findSubtotalStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> cashStringsWithLocale = DataAnalyzer.findCashStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> changeStringsWithLocale = DataAnalyzer.findChangeStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> coverStringsWithLocale = DataAnalyzer.findCoverStringTexts(texts); //1
            List<Scored<Pair<OcrText, Locale>>> taxStringsWithLocale = DataAnalyzer.findTaxStringTexts(texts); //1

            language = DataAnalyzer.getBestLanguage(asList(amountStringsWithLocale, subtotalStringsWithLocale,
                    cashStringsWithLocale, changeStringsWithLocale, coverStringsWithLocale,
                    taxStringsWithLocale));

            List<Scored<OcrText>> amountStrings = DataAnalyzer.filterForLanguage(amountStringsWithLocale, language);
            List<Scored<OcrText>> subtotalStrings = DataAnalyzer.filterForLanguage(subtotalStringsWithLocale, language);
            List<Scored<OcrText>> cashStrings = DataAnalyzer.filterForLanguage(cashStringsWithLocale, language);
            List<Scored<OcrText>> changeStrings = DataAnalyzer.filterForLanguage(changeStringsWithLocale, language);
            List<Scored<OcrText>> coverStrings = DataAnalyzer.filterForLanguage(coverStringsWithLocale, language);
            List<Scored<OcrText>> taxStrings = DataAnalyzer.filterForLanguage(taxStringsWithLocale, language);

            List<ListAmountOrganizer> amountList = DataAnalyzer.organizeAmountList(amountStrings, mainImage); //2
            Collections.sort(amountList, Collections.reverseOrder());
            if (!amountList.isEmpty()) {
                List<Scored<OcrText>> amountPrice;
                int i = 0;
                int maxScan = (options.totalSearch == TotalSearch.EXTENDED_SEARCH ? 3 : 1);
                while (i < amountList.size()) {
                    OcrText amountStringText = amountList.get(i).getSourceText().obj();
                    RectF stripBoundingBox = OcrAnalyzer.getAmountExtendedBox(amountStringText, mainImage.getWidth());
                    if (options.totalSearch.ordinal() >= TotalSearch.DEEP.ordinal() && i <= maxScan) {
                        //Redo ocr on i element of list.
                        amountPrice = analyzer.getTextsInStrip(imgProc, OcrUtils.imageToSize(mainImage), amountStringText, stripBoundingBox);//3
                    } else {
                        //Use current texts to search target texts (with price)
                        amountPrice = analyzer.getOrigTexts(amountStringText, stripBoundingBox);
                    }
                    //set target texts (lines) to first source text
                    amountList.get(i).setAmountTargetTexts(amountPrice); //4
                    AmountComparator amountComparator;
                    Pair<OcrText, BigDecimal> amountT = DataAnalyzer.getMatchingAmount(amountList.get(i).getTargetTexts(), false); //5
                    if (amountT != null && newTicket.total == null) {
                        newTicket.total = amountT.second;
                        amountPriceText = amountT.first;
                        amountComparator = new AmountComparator(newTicket.total, amountT.first, mainImage); //8
                        bestTicket = amountComparator.getBestAmount(true);
                        if (bestTicket != null) {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best amount is: " + bestTicket.obj().getBestAmount().setScale(2, RoundingMode.HALF_UP) +
                                    "\nwith scheme: " + bestTicket.obj().toString() + "\nand score: " + bestTicket.getScore());
                        } else {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best amount is: " + newTicket.total.setScale(2, RoundingMode.HALF_UP));
                        }
                    }
                    Pair<OcrText, BigDecimal> restoredAmountT = DataAnalyzer.getRestoredAmount(amountList.get(i).getTargetTexts()); //6
                    //todo: Redo ocr on prices strip if enabled //7

                    //todo: Initialize amount comparator with specific schemes if 'contante'/'resto'/'subtotale' are found
                    if (restoredAmountT != null && restoredAmount == null) {
                        restoredAmount = restoredAmountT.second;
                        restoredAmountPriceText = restoredAmountT.first;
                        amountComparator = new AmountComparator(restoredAmount, restoredAmountT.first, mainImage);
                        Scored<TicketScheme> bestRestoredTicket = amountComparator.getBestAmount(false);
                        if (bestRestoredTicket != null) {
                            OcrUtils.log(2, "MANAGER.Comparator", "Best restored amount is: " + bestRestoredTicket.obj().getBestAmount().setScale(2, RoundingMode.HALF_UP) +
                                    "\nwith scheme: " + bestRestoredTicket.obj().toString() + "\nand score: " + bestRestoredTicket.getScore());
                            if (bestTicket == null || bestRestoredTicket.getScore() > bestTicket.getScore()) {
                                bestTicket = bestRestoredTicket; //choose total with highest score
                            }
                        }
                    }
                    if (bestTicket != null)
                        restoredAmount = bestTicket.obj().getBestAmount(); //9
                    if (newTicket.total != null)
                        break;
                    ++i;
                }
                if (options.allowEdit == Edit.ALLOW_TOTAL_EDIT) {
                    newTicket.total = restoredAmount;
                    amountPriceText = restoredAmountPriceText;
                }
            }
            ticket.containsCover = coverStrings.size() > 0;
            if (amountPriceText != null) {
                ticket.totalRect = ImageProcessor.normalizeCoordinates(amountPriceText.box(), size(frame));
            }
        }

        if (options.productsSearch != ProductsSearch.SKIP) {
            List<Pair<String, BigDecimal>> newProducts = null;
            if (bestTicket != null) {
                List<Pair<OcrText, BigDecimal>> prices = bestTicket.obj().getPricesList();
                if (prices != null) {
                    newProducts = Stream.of(prices)
                            .map(price -> new Pair<>(Stream.of(analyzer.getTextsOnleft(price.first)) //get texts for product name and concatenate these texts
                                    .reduce("", (string, text) -> string + text.text() + " "), price.second))
                            .toList();
                }
            }
            newTicket.products = newProducts;
        }

        ticket.total = newTicket.total;
        ticket.products = newTicket.products;
        ticket.errors.addAll(newTicket.errors);

        if (options.dateSearch != DateSearch.SKIP) {
            Pair<OcrText, Date> pair = findDate(texts, language != null
                    ? LANGUAGE_TO_COUNTRY.get(language) : null, country);
            if (pair != null)
                ticket.date = pair.second;
        }

        //NB: this is a fallback, a language could be associated to multiple countries, only one is chosen.
        if (country == null && language != null) {
            country = LANGUAGE_TO_COUNTRY.get(language);
        }
        //update ticket currency
        if (country != null)
            ticket.currency = Currency.getInstance(country);


        if (IS_DEBUG_ENABLED) {
            endTime = System.nanoTime();
            double duration = ((double) (endTime - startTime)) / 10e9;
            OcrUtils.log(1, "EXECUTION TIME: ", "Core: " + duration + " seconds");
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

        if (options.orientation != Orientation.FORCE_UPSIDE_DOWN)
            extractTicket(ticket, procCopy, options);

        boolean upsideDownAnlaysis = false;
        //if should find total and total is null, or should not find total but should find date and date is null
        boolean retryNeeded = (options.totalSearch != TotalSearch.SKIP && ticket.total == null)
                || (options.totalSearch == TotalSearch.SKIP
                && options.dateSearch != DateSearch.SKIP && ticket.date != null);
        if (options.orientation != Orientation.NORMAL && retryNeeded) {
            procCopy.rotate(2);
            extractTicket(ticket, procCopy, options);
            upsideDownAnlaysis = true;
        }

        if (options.totalSearch != TotalSearch.SKIP && ticket.total == null) {
            ticket.errors.add(OcrError.AMOUNT_NOT_FOUND);
        }
        if (options.dateSearch != DateSearch.SKIP && ticket.date == null) {
            ticket.errors.add(OcrError.DATE_NOT_FOUND);
        }
        if (upsideDownAnlaysis && !ticket.errors.contains(OcrError.AMOUNT_NOT_FOUND)
                && !ticket.errors.contains(OcrError.DATE_NOT_FOUND)) {
            ticket.errors.add(OcrError.UPSIDE_DOWN);
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
