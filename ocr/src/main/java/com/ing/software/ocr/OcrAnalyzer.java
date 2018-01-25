package com.ing.software.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Pair;
import android.util.SizeF;
import android.util.SparseArray;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;

import com.ing.software.common.*;
import com.ing.software.ocr.OcrObjects.*;

import static com.ing.software.common.CommonUtils.rectFromSize;
import static com.ing.software.common.CommonUtils.size;
import static java.util.Collections.*;
import static com.ing.software.ocr.OcrUtils.log;
import static com.ing.software.ocr.OcrVars.*;
import static java.lang.Math.*;

/**
 * Class containing different methods to analyze a picture
 * @author Michelon
 * @author Zaglia
 */
public class OcrAnalyzer {

    private TextRecognizer ocrEngine = null;
    private final int targetPrecision = 130; //Should be passed with image. todo


    /**
     * @author Michelon
     * @author Zaglia
     * Initialize the component.
     * If this method returns -1, check if the device has enough free disk space.
     * In this case, try to call this method again.
     * When this method returns 0, it is possible to call getOcrResult.
     * @param ctx Android context.
     * @return 0 if successful, negative otherwise.
     */
    int initialize(Context ctx) {
        ocrEngine = new TextRecognizer.Builder(ctx).build();
        return ocrEngine.isOperational() ? 0 : -1;
        //failure causes: GSM package is not yet downloaded due to lack of time or lack of space.
    }

    void release() {
        ocrEngine.release();
    }

    /**
     * @author Michelon
     * @author Zaglia
     * Get an OcrResult from a Bitmap
     * @param frame Bitmap used to create an OcrResult. Not null.
     * @return OcrResult containing raw data to be further analyzed.
     */
    OcrResult analyze(@NonNull Bitmap frame){
        RawImage mainImage = new RawImage(frame);

        //ocrEngine analysis
        long startTime = System.nanoTime();
        SparseArray<TextBlock> tempArray = ocrEngine.detect(new Frame.Builder().setBitmap(frame).build());
        long endTime = System.nanoTime();
        double duration = ((double) (endTime - startTime)) / 1000000000;
        OcrUtils.log(1, "DETECTOR: ", "EXECUTION TIME: "+ duration + " seconds");
        long startTime2 = System.nanoTime();
        List<RawText> rawOrigTexts = orderBlocks(mainImage, tempArray);
        mainImage.setRects(rawOrigTexts); //save rect configuration in rawimage
        OcrSchemer.prepareScheme(rawOrigTexts);
        mainImage.textFitter(); //save configuration from prepareScheme in rawimage
        listEverything(rawOrigTexts);
        List<RawStringResult> valuedTexts = new ArrayList<>();
        for (String amountString : AMOUNT_STRINGS) {
        	valuedTexts.addAll(searchContinuousString(rawOrigTexts, amountString)); //contains texts which match amountString
        }
        valuedTexts = searchContinuousStringExtended(rawOrigTexts, valuedTexts, targetPrecision); //add possible amounts to source string
        List<RawGridResult> dateList = getDateList(rawOrigTexts);
        long endTime2 = System.nanoTime();
        double duration2 = ((double) (endTime2 - startTime2)) / 1000000000;
        OcrUtils.log(1, "OCR ANALYZER: ", "EXECUTION TIME: "+ duration2 + " seconds");
        return new OcrResult(valuedTexts, dateList, mainImage);
    }

    /**
     * List in debug log blocks parsed (detection + grid)
     * @param texts List of texts. Not null.
     */
    private static void listEverything(@NonNull List<RawText> texts) {
        if (IS_DEBUG_ENABLED) {
            for (RawText text : texts) {
                //OcrUtils.log(2, "FULL LIST: ", text.getValue());
                //OcrUtils.log(2, "FULL LIST: ","In cell: " + text.getGridBox()[1] + ";" + text.getGridBox()[0]);
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nINTRODUCTION");
            for (RawText text : texts) {
                if (text.getTags().contains(INTRODUCTION_TAG))
                    OcrUtils.log(2, "introduction", text.getValue());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nPRODUCTS");
            for (RawText text : texts) {
                if (text.getTags().contains(PRODUCTS_TAG))
                    OcrUtils.log(2, "products", text.getValue());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nPRICES");
            for (RawText text : texts) {
                if (text.getTags().contains(PRICES_TAG))
                    OcrUtils.log(2, "prices", text.getValue());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################\nCONCLUSION");
            for (RawText text : texts) {
                if (text.getTags().contains(CONCLUSION_TAG))
                    OcrUtils.log(2, "conclusion", text.getValue());
            }
            OcrUtils.log(2, "LIST EVERYTHING", "###########################");
        }
    }

    /**
     * @author Michelon
     * Orders TextBlock decoded by detector in a list of RawTexts
     * Order is from top to bottom, from left to right
     * @param photo RawImage of the photo. Not null.
     * @param origTextBlocks detected texts. Not null.
     * @return list of ordered RawTexts
     */
    private static List<RawText> orderBlocks(@NonNull RawImage photo, @NonNull SparseArray<TextBlock> origTextBlocks) {
        List<TextBlock> newOrderedTextBlocks = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); i++) {
            newOrderedTextBlocks.add(origTextBlocks.valueAt(i));
        }
        newOrderedTextBlocks = OcrUtils.orderTextBlocks(newOrderedTextBlocks);
        log(3,"OcrAnalyzer.analyzeST:" , "New Blocks ordered");
        List<RawText> rawTexts = new ArrayList<>();
        for (TextBlock textBlock : newOrderedTextBlocks) {
            for (Text currentText : textBlock.getComponents()) {
                rawTexts.add(new RawText(currentText, photo));
            }
        }
        return rawTexts;
    }

    /**
     * @author Michelon
     * Find first (RawTexts in RawBlocks are ordered from top to bottom, left to right)
     * occurrence (exact match) of chosen string in a list of RawBlocks
     * @param rawBlocks list of RawBlocks. Not null.
     * @param testString string to find. Length > 0.
     * @return First RawText in first RawBlock with target string
     */
    private static RawText searchFirstExactString(@NonNull List<RawBlock> rawBlocks, @Size(min = 1) String testString) {
        RawText targetText = null;
        for (RawBlock rawBlock : rawBlocks) {
            targetText = rawBlock.findFirstExact(testString);
            if (targetText != null)
                break;
        }
        if (targetText != null) {
            log(5,"OcrAnalyzer.analyzeBFS", "Found first target string: "+ testString + " \nat: " + targetText.getValue());
            log(9,"OcrAnalyzer.analyzeBFS", "Target text is at (left, top, right, bottom): "+ targetText.getBoundingBox().left + "; "
                    + targetText.getBoundingBox().top + "; " + targetText.getBoundingBox().right + "; "+ targetText.getBoundingBox().bottom + ".");
        }
        return targetText;
    }

    /**
     * @author Michelon
     * Search for all occurrences of target string in detected (and ordered) RawTexts according to OcrVars.MAX_STRING_DISTANCE
     * @param rawTexts list of RawTexts. Not null.
     * @param testString string to find. Length > 0.
     * @return list of RawStringResult containing only source RawText where string is present. Note: this list is not ordered.
     */
    private static List<RawStringResult> searchContinuousString(@NonNull List<RawText> rawTexts, @Size(min = 1) String testString) {
        List<RawStringResult> targetTextList = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            RawStringResult tempTextList = rawText.findContinuous(testString, MAX_STRING_DISTANCE);
            if (tempTextList != null) {
                targetTextList.add(tempTextList);
            }
        }
        if (targetTextList.size() >0 && IS_DEBUG_ENABLED) {
            for (RawStringResult stringText : targetTextList) {
                RawText text = stringText.getSourceText();
                log(5,"OcrAnalyzer", "Found target string: " + testString + " \nat: " + text.getValue()
                        + " with distance: " + stringText.getDistanceFromTarget());
                log(9,"OcrAnalyzer", "Target text is at (left, top, right, bottom): " + text.getBoundingBox().left
                        + "; " + text.getBoundingBox().top + "; " + text.getBoundingBox().right + "; " + text.getBoundingBox().bottom + ".");
            }
        }
        return targetTextList;
    }

    /**
     * @author Michelon
     * From a list of RawTexts, retrieves also RawTexts with similar distance from top and bottom of the photo.
     * 'Similar' is defined by precision. See {@link OcrUtils extendWidthFromPhoto()} for details.
     * @param rawTexts list of RawBlocks from original photo. Not null.
     * @param targetStringList list of target RawTexts. Not null.
     * @param precision precision to extend rect. See OcrUtils.extendRect()
     * @return list of RawStringResults. Adds to the @param targetStringList objects the detected RawTexts
     * in proximity of source RawTexts. Note: this list is not ordered.
     */
    /*How search works::
    1- Take a RawText
    2- search in extended rect of the source of a stringResult if it contains this RawText
    3- if yes, adds it to the detected texts of the stringResults
    4- repeat from 2 until you have no more stringResults left
    5- repeat from 1 until you have no more RawText left
    */
    private static List<RawStringResult> searchContinuousStringExtended(@NonNull List<RawText> rawTexts, @NonNull List<RawStringResult> targetStringList, int precision) {
        List<RawStringResult> results = targetStringList; //redundant but easier to understand
        log(2,"OcrAnalyzer.SCSE", "StringResult list size is: " + results.size());
        for (RawText rawText : rawTexts) {
            for (RawStringResult singleResult : results) {
                RawText rawTextSource = singleResult.getSourceText();
                log(6,"OcrAnalyzer.SCSE", "Extending rect: " + rawTextSource.getValue());
                Rect newRect = OcrUtils.extendRect(rawTextSource.getBoundingBox(), precision, -rawTextSource.getRawImage().getWidth()); //negative width to use pixels
                if (rawText.isInside(newRect)) {
                    singleResult.addDetectedTexts(rawText);
                    log(5,"OcrAnalyzer", "Found target string: " + singleResult.getSourceString() + "\nfrom extended: " + rawTextSource.getValue());
                }
                else
                    log(7,"OcrAnalyzer.SCSE", "Nothing found"); //Nothing in this block
            }
        }
        if (results.size() == 0) {
            log(2,"OcrAnalyzer", "Nothing found ");
        }
        else if (IS_DEBUG_ENABLED){
            log(4,"OcrAnalyzer", "Final list: " + results.size());
            for (RawStringResult rawStringResult : results) {
                List<RawGridResult> textList = rawStringResult.getDetectedTexts();
                if (textList == null)
                    log(3,"OcrAnalyzer.SCSE", "Value not found.");
                else {
                    for (RawGridResult rawText : textList) {
                        log(4,"OcrAnalyzer.SCSE", "Value: " + rawText.getText().getValue());
                        log(4,"OcrAnalyzer.SCSE", "Source: " + rawStringResult.getSourceText().getValue());
                    }
                }
            }
        }
        return results;
    }

    /**
     * @author Michelon
     * Merges Lists with RawTexts + probability to find date from all blocks.
     * And orders it according to their probability (fallback is position).
     * @param rawTexts RawTexts from which retrieve lists. Not null.
     * @return List of RawGridResults containing RawTexts + probability
     */
    private List<RawGridResult> getDateList(@NonNull List<RawText> rawTexts) {
        List<RawGridResult> fullList = new ArrayList<>();
        for (RawText rawText : rawTexts) {
            fullList.add(new RawGridResult(rawText, rawText.getDateProbability()));
        }
        log(6,"FINAL_LIST_SIZE_IS", " " + fullList.size());
        Collections.sort(fullList);
        return fullList;
    }

    /**
     * @author Michelon
     * Get a list of prices of products. It actually finds all texts which center is on right side of the receipt
     * @param texts list of texts. Not null.
     * @return list of texts on right side of receipt
     */
    @Deprecated
    private static List<RawText> getProductPrices(@NonNull List<RawText> texts) {
        //blocks = OcrSchemer.findBlocksOnLeft(blocks);
        texts = OcrSchemer.findTextsOnRight(texts);
        if (IS_DEBUG_ENABLED)
            //for (RawBlock block : blocks)
                for (RawText text : texts)
                    OcrUtils.log(5,"getProductPrices", "Product found: " + text.getValue());
        return texts;
    }

    /**
     * @author Michelon
     * Performs a quick detection on chosen photo and returns blocks detected
     * @param photo photo to analyze. Not null.
     * @param context context to run analyzer
     * @return list of all blocks found (ordered from top to bottom, left to right)
     */
    @Deprecated
    private static List<TextBlock> quickAnalysis(@NonNull Bitmap photo, Context context) {
        SparseArray<TextBlock> origTextBlocks;
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        try {
            Frame frame = new Frame.Builder().setBitmap(photo).build();
            origTextBlocks = textRecognizer.detect(frame);
        }
        finally {
            textRecognizer.release();
        }
        List<TextBlock> orderedTextBlocks = new ArrayList<>();
        for (int i = 0; i < origTextBlocks.size(); i++) {
            orderedTextBlocks.add(origTextBlocks.valueAt(i));
        }
        return orderedTextBlocks;
    }

    /**
     * NOTE: This is a temporary method.
     * @author Michelon
     * Crops photo using the smallest rect that contains all TextBlock in source photo
     * @param photo source photo. Not null.
     * @param context context to run first analyzer
     * @return smallest cropped photo containing all TextBlocks
     */
    @Deprecated
    public static Bitmap getCroppedPhoto(@NonNull Bitmap photo, Context context) {
        List<TextBlock> orderedTextBlocks = quickAnalysis(photo, context);
        log(2,"OcrAnalyzer.analyze:" , "Blocks detected");
        orderedTextBlocks = OcrUtils.orderTextBlocks(orderedTextBlocks);
        log(2,"OcrAnalyzer.analyze:" , "Blocks ordered");
        int[] borders = OcrUtils.getRectBorders(orderedTextBlocks, new RawImage(photo));
        int left = borders[0];
        int right = borders[2];
        int top = borders[1];
        int bottom = borders[3];
        return OcrUtils.cropImage(photo, left, top, right, bottom);
    }





    /**
     * Run the ocr detection on the given bitmap.
     * @param bm input bitmap
     * @param ocrEngine TextRecognizer
     * @return list of OcrText
     *
     * @author Riccardo Zaglia
     */
    private static List<OcrText> runOCR(Bitmap bm, TextRecognizer ocrEngine) {
        SparseArray<TextBlock> blocks = ocrEngine.detect(new Frame.Builder().setBitmap(bm).build());
        List<OcrText> lines = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++)
            for (Text txt : blocks.valueAt(i).getComponents())
                lines.add(new OcrText(txt));
        return lines;
    }

    /**
     * Find all OcrLines which text is matched by any of the list of matchers.
     * @param lines list of OcrLines. Can be empty
     * @return OcrLines matched. Can be empty if no match is found.
     *
     * @author Riccardo Zaglia
     */
    // todo: find most meaningful way to combine score criteria.
    private static List<Scored<OcrText>> findAllMatchingTexts(List<OcrText> lines, List<WordMatcher> matchers) {
        return Stream.of(lines)
                .map(line -> new Scored<>(max(Stream.of(matchers).map(m -> m.match(line)).toList()), line))
                .filter(s -> s.getScore() != 0).toList();
    }

    /**
     * Choose the OcrText that most probably contains the amount string.
     * Criteria: AMOUNT_MATCHER score; character size; higher in the photo
     * @param lines list of OcrLines. Can be empty
     * @return OcrText with higher score. Can be null if no match is found.
     *
     * @author Riccardo Zaglia
     */
    // todo: find most meaningful way to combine score criteria.
    // Do not order. Use max() to get best scored Text, otherwise if you have to read all these texts anyway,
    // ordering it's useless.
    private static List<Scored<OcrText>> findAllScoredAmountStrings(List<OcrText> lines, SizeF bmSize) {
        List<Scored<OcrText>> matchedLines = findAllMatchingTexts(lines, IT_AMOUNT_MATCHERS);
        // modify score for each matched line
        for (Scored<OcrText> line : matchedLines) {
            double score = line.getScore();
            score *= line.obj().charWidth() + line.obj().charHeight(); // <- todo normalize with all texts average
            score *= 1. - line.obj().centerY() / bmSize.getHeight(); // <- todo find a better way
                                                                     // (relative position with cash and change)
            line.setScore(score);
        }
        return matchedLines;
    }

    //todo: find cash, change, subtotal, indoor.

    /**
     * Get a strip rectangle where the amount price should be found.
     * @param amountStr amount string line.
     * @param bmSize bitmap size.
     * @return RectF rectangle in bitmap space.
     *
     * @author Riccardo Zaglia
     */
    private static RectF getAmountStripRect(OcrText amountStr, SizeF bmSize) {
        //I use box height because if text is crooked, I search for the amount price in a larger area.
        // todo: take into account slope to get more accurate results
        float halfHeight = amountStr.box().height() * EXT_RECT_V_MUL / 2f;
        // here I account that the amount number could be in the same OcrText as the amount string.
        // I use the center of the OcrText as a left boundary.
        return new RectF(amountStr.centerX(), amountStr.centerY() - halfHeight,
                bmSize.getWidth(), amountStr.centerY() + halfHeight);
    }

    /**
     * Create a new bitmap optimized for amount price, from the amount rectangle
     * @param imgProc
     * @param bmSize
     * @param amountStr
     * @param srcRect
     * @return
     */
    private static Bitmap getAmountStrip(
            ImageProcessor imgProc, SizeF bmSize, OcrText amountStr, RectF srcRect) {
        return imgProc.undistortedSubregion(bmSize, srcRect,
                srcRect.width() / srcRect.height() * CHAR_ASPECT_RATIO / amountStr.charAspectRatio());
    }

    /**
     * Get all texts that are potentially prices, but could be corrupt
     * @param lines
     * @return
     */
    private static List<OcrText> findAllPotentialPrices(List<OcrText> lines) {
        //for each line, keep line if matches price for textNoSpaces.
        //I do not use sanitized strings because they easily matches at random.
        // So there could be some texts that are matched by certainPrices but not by this
        //todo: discuss decision
        return Stream.of(lines).filter(line -> POTENTIAL_PRICE.matcher(line.textNoSpaces()).find()).toList();
    }

    /**
     * Get all texts that matches an upside down price
     * @param lines
     * @return
     */
    private static List<OcrText> findAllUpsideDownPrices(List<OcrText> lines) {
        return Stream.of(lines).filter(line -> PRICE_UPSIDEDOWN.matcher(line.textNoSpaces()).find()).toList();
    }

    private static String removeSpaces(String str) {
        return str.replace(" ", "");
    }

    /**
     * Find all OcrTexts that certainly matches a price
     * @param lines List of OcrTexts
     * @return list of pairs of OcrText and associated price string
     */
    private static List<Pair<OcrText, String>> findAllCertainPrices(List<OcrText> lines) {
        List<Pair<OcrText, String>> prices = new ArrayList<>();
        for (OcrText line : lines) {
            Matcher matcher = PRICE_NO_THOUSAND_MARK.matcher(line.textSanitizedNum());
            boolean matched = matcher.find();
            int childsTot = line.childs().size();
            if (!matched && childsTot >= 2) { // merge only last two words and try again
                matcher = PRICE_NO_THOUSAND_MARK.matcher(
                        line.childs().get(childsTot - 1).textSanitizedNum()
                        + line.childs().get(childsTot - 2).textSanitizedNum());
                matched = matcher.find();
            }
            if (matched) {
                // I convert spaces to dots because the matcher is designed to accept spaces as dots
                prices.add(new Pair<>(line, removeSpaces(matcher.group())));
            }
        }
        return prices;
    }

    /**
     * Choose the OcrText that most probably contains the amount price and return it as a BigDecimal.
     * Criteria: lower distance from center of strip; least character size difference from amount string
     * @param lines all lines contained inside amount strip, converted to original bitmap space
     * @param amountStr amount string line.
     * @param stripRect amount strip rect in the original bitmap space.
     * @return BigDecimal containing price, or null if no price found.
     */
    // todo: find most meaningful way to combine score criteria.
    // todo: reject false positives adding a lower limit to the score > 0.
    private static BigDecimal findAmountPrice (List<OcrText> lines, OcrText amountStr, RectF stripRect) {
        List<Pair<OcrText, String>> prices = findAllCertainPrices(lines);
        BigDecimal price = null;
        double bestScore = 0;
        for (Pair<OcrText, String> priceLine : prices) {
            OcrText line = priceLine.first;
            double score = 1. - abs(line.centerY() - stripRect.centerY()) / stripRect.height(); // y position diff
            score *= 1. - abs(line.charWidth() - amountStr.charWidth()) / amountStr.charWidth(); // char width diff
            score *= 1. - abs(line.charHeight() - amountStr.charHeight()) / amountStr.charHeight(); // char height diff
            if (score > bestScore) {
                bestScore = score;
                //todo check behaviour if a the string cannot be converted to BigDecimal for some reason
                price = new BigDecimal(priceLine.second);
            }
        }
        return price;
    }

    /**
     * Find all dates in the format DMY.
     * @param lines list of OcrLines
     * @return date instance or null if not found or multiple
     */
    private static List<Pair<OcrText, Date>> findAllDates(List<OcrText> lines) {
        List<Pair<OcrText, Date>> dates = new ArrayList<>();
        for (OcrText line : lines) {
            Matcher matcher = DATE_DMY.matcher(line.textSanitizedNum());
            if (matcher.find()) {
                int day = Integer.valueOf(matcher.group(DMY_DAY));
                int month = Integer.valueOf(matcher.group(DMY_MONTH));
                int year = Integer.valueOf(matcher.group(DMY_YEAR));

                //todo check if day is compatible with month (29-30-31)

                if (year < 100)
                    year += year > YEAR_CUT ? 1900 : 2000;
                // correct for 0 based month
                dates.add(new Pair<>(line, new GregorianCalendar(year, month - 1, day).getTime()));
            }
            // It's better to avoid word concatenation because it could match a wrong date.
            // Ex: 1/1/20 14:30 -> 1/1/2014:30
        }
        return dates;
    }

    /**
     * Extract a Ticket from an ImageProcessor loaded with a bitmap.
     * @param imgProc ImagePreprocessor with at least an image assigned (corners can be set manually).
     * @return Ticket containing any information found, and/or a list of errors occurred.
     *
     * @author Riccardo Zaglia
     */
    // todo: integrate schemer and other heuristics
    public synchronized OcrTicket analyzeTicket(@NonNull ImageProcessor imgProc, boolean advanced) {
        OcrTicket ticket = new OcrTicket();
        ticket.errors = new ArrayList<>();

        Bitmap bm = imgProc.undistortForOCR(advanced ? OCR_ADVANCED_SCALE : OCR_NORMAL_SCALE);
        if (bm == null) {
            ticket.errors.add(OcrError.INVALID_PROCESSOR);
            return ticket;
        }
        ticket.rectangle = imgProc.getCorners();
        List<OcrText> lines = runOCR(bm, ocrEngine);

        //find amount
        List<Scored<OcrText>> amountStrs = findAllScoredAmountStrings(lines, size(bm));
        if (amountStrs.size() > 0) {
            OcrText amountStr = max(amountStrs).obj();
            RectF srcAmountStripRect = getAmountStripRect(amountStr, size(bm));
            Bitmap amountStrip = getAmountStrip(imgProc, size(bm), amountStr, srcAmountStripRect);
            RectF dstAmountStripRect = rectFromSize(size(amountStrip));
            List<OcrText> amountLinesStripSpace = runOCR(amountStrip, ocrEngine);

            // transform texts from destination to source space (I swap source and destination rect).
            List<OcrText> amountLinesBmSpace = Stream.of(amountLinesStripSpace)
                    .map(line -> new OcrText(line, dstAmountStripRect, srcAmountStripRect)).toList();
            ticket.amount = findAmountPrice(amountLinesBmSpace, amountStr, srcAmountStripRect);
        }
        if (ticket.amount == null)
            ticket.errors.add(OcrError.AMOUNT_NOT_FOUND);

        List<Pair<OcrText, Date>> dates = findAllDates(lines);
        if (dates.size() == 1) {
            ticket.date = dates.get(0).second;
        } else {
            ticket.errors.add(OcrError.DATE_NOT_FOUND);
        }

        return ticket;
    }

    /**
     * Asynchronous version of analyzeTicket(imgProc). The ticket is passed by the callback parameter.
     * @param imgProc ImagePreprocessor
     * @param ticketCb Callback
     */
    public void analyzeTicket(
            @NonNull ImageProcessor imgProc, boolean advanced, @NonNull Consumer<OcrTicket> ticketCb) {
        new Thread(() -> ticketCb.accept(analyzeTicket(imgProc, advanced))).start();
    }
}
