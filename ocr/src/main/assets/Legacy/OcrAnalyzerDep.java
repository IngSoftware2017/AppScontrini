package com.ing.software.ocr.Legacy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.util.SizeF;
import android.util.SparseArray;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ing.software.common.Scored;
import com.ing.software.ocr.ImageProcessor;
import com.ing.software.ocr.OcrObjects.OcrError;
import com.ing.software.ocr.OcrObjects.OcrTicket;
import com.ing.software.ocr.OcrSchemer;
import com.ing.software.ocr.OcrUtils;
import com.ing.software.ocr.OperativeObjects.RawImage;
import com.ing.software.ocr.OperativeObjects.WordMatcher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;

import static com.ing.software.common.CommonUtils.size;
import static com.ing.software.ocr.OcrUtils.log;
import static com.ing.software.ocr.OcrVars.AMOUNT_MATCHERS;
import static com.ing.software.ocr.OcrVars.DATE_DMY;
import static com.ing.software.ocr.OcrVars.DMY_DAY;
import static com.ing.software.ocr.OcrVars.DMY_MONTH;
import static com.ing.software.ocr.OcrVars.DMY_YEAR;
import static com.ing.software.ocr.OcrVars.IS_DEBUG_ENABLED;
import static com.ing.software.ocr.OcrVars.MAX_STRING_DISTANCE;
import static com.ing.software.ocr.OcrVars.PRICE_NO_THOUSAND_MARK;
import static com.ing.software.ocr.OcrVars.YEAR_CUT;
import static java.util.Collections.max;

/**
 * NOTE: TO AVOID ERRORS SOME LINES HAVE BEEN CHANGED FROM ORIGINAL CODE (e.g. all 'new RectF')
 */
@Deprecated
public class OcrAnalyzerDep {

    private TextRecognizer ocrEngine = null;
    private final int targetPrecision = 130; //Should be passed with image. todo

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
                RectF newRect = OcrUtilsDep.extendRect(new RectF(rawTextSource.getBoundingBox()), precision, -rawTextSource.getRawImage().getWidth()); //negative width to use pixels
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
        texts = OcrSchemerDep.findTextsOnRight(texts);
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
        orderedTextBlocks = OcrUtilsDep.orderTextBlocks(orderedTextBlocks);
        log(2,"OcrAnalyzer.analyze:" , "Blocks ordered");
        int[] borders = OcrUtilsDep.getRectBorders(orderedTextBlocks, new RawImage(photo));
        int left = borders[0];
        int right = borders[2];
        int top = borders[1];
        int bottom = borders[3];
        return OcrUtilsDep.cropImage(photo, left, top, right, bottom);
    }



    // Extended rectangle vertical multiplier
    private static final float EXT_RECT_V_MUL = 3;

    /**
     * This function runs the ocr detection on the given bitmap.
     * @param bm input bitmap
     * @param ocrEngine TextRecognizer
     * @return list of OcrText
     * @author Riccardo Zaglia
     */
    private static List<OcrText> bitmapToLines(Bitmap bm, TextRecognizer ocrEngine) {
        SparseArray<TextBlock> blocks = ocrEngine.detect(new Frame.Builder().setBitmap(bm).build());
        List<OcrText> lines = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++)
            for (Text txt : blocks.valueAt(i).getComponents())
                lines.add(new OcrText((Line)txt));
        return lines;
    }

    /**
     * Find all TextLines which text is matched by any of the list of matchers.
     * @param lines list of TextLines. Can be empty
     * @return TextLines matched. Can be empty if no match is found.
     * @author Riccardo Zaglia
     */
    // todo: find most meaningful way to combine score criteria.
    private static List<Scored<OcrText>> findAllMatchedStrings(List<OcrText> lines, List<WordMatcher> matchers) {
        return Stream.of(lines)
                .map(line -> new Scored<>(max(Stream.of(matchers).map(m -> m.match(line)).toList()), line))
                .filter(s -> s.getScore() != 0).toList();
    }

    /**
     * Choose the OcrText that most probably contains the amount string.
     * Criteria: AMOUNT_MATCHER score; character size; higher in the photo
     * @param lines list of TextLines. Can be empty
     * @return OcrText with higher score. Can be null if no match is found.
     * @author Riccardo Zaglia
     */
    // todo: find most meaningful way to combine score criteria.
    private static OcrText findAmountString(List<OcrText> lines, SizeF bmSize) {
        List<Scored<OcrText>> matchedLines = findAllMatchedStrings(lines, AMOUNT_MATCHERS);
        // modify score for each matched line
        for (Scored<OcrText> line : matchedLines) {
            double score = line.getScore();
            score *= line.obj().charWidth() + line.obj().charHeight();
            score *= 1. - line.obj().centerY() / bmSize.getHeight();
            line.setScore(score);
        }
        //return best line
        return !matchedLines.isEmpty() ? max(matchedLines).obj() : null;
    }

    //todo: find

    /**
     * Get a strip rectangle where the amount price should be found.
     * @param amountStr amount string line.
     * @param bmSize bitmap size.
     * @return RectF rectangle in bitmap space.
     * @author Riccardo Zaglia
     */
    @NonNull
    private static RectF getAmountStripRect(OcrText amountStr, SizeF bmSize) {
        float halfHeight = (float)amountStr.charHeight() * EXT_RECT_V_MUL / 2f;
        // here I account that the amount number could be in the same OcrText as the amount string.
        // I use the center of the OcrText as a left boundary.
        return new RectF(amountStr.centerX(), amountStr.centerY() - halfHeight,
                bmSize.getWidth(), amountStr.centerY() + halfHeight);
    }

    private static Bitmap getAmountStrip(
            ImageProcessor imgProc, SizeF bmSize, OcrText amountStr, RectF srcRect) {
        return imgProc.undistortedSubregion(bmSize, srcRect,
                srcRect.width() / srcRect.height() * CHAR_ASPECT_RATIO / amountStr.charAspectRatio());
    }

    /**
     * Choose the OcrText that most probably contains the amount price and return it as a BigDecimal.
     * Criteria: lower distance from center of strip; least character size difference from amount string
     * @param lines all lines contained inside amount strip.
     * @param amountStr amount string line.
     * @param srcStripSize amount strip size in the original bitmap space.
     * @param dstStripSize actual amount strip size.
     * @return BigDecimal containing price, or null if no price found.
     */
    // todo: find most meaningful way to combine score criteria.
    // todo: reject false positives adding a lower limit to the score > 0.
    @Nullable
    private static BigDecimal findAmountPrice(
            List<OcrText> lines, OcrText amountStr, SizeF srcStripSize, SizeF dstStripSize) {
        double dstAmountStrWidth = amountStr.charWidth() * dstStripSize.getWidth() / srcStripSize.getWidth();
        double dstAmountStrHeight = amountStr.charHeight() * dstStripSize.getHeight() / srcStripSize.getHeight();

        String priceStr = null;
        double bestScore = 0;
        for (OcrText line : lines) {
            // remove spaces between words, apply sanitize substitutions and try matching with the price matcher
            Matcher matcher = PRICE_NO_THOUSAND_MARK.matcher(line.numNoSpaces());
            boolean matched = matcher.find();
            if (!matched) { // try again using a dot to concatenate words
                matcher = PRICE_NO_THOUSAND_MARK.matcher(line.numConcatDot());
                matched = matcher.find();
            }
            if (matched) {
                double score = 1 - 0.5 * abs(1 - 2 * line.centerY() / dstStripSize.getHeight());
                score *= 2 - abs(1 - line.charWidth() / dstAmountStrWidth)
                        - abs(1 - line.charHeight() / dstAmountStrHeight);
                if (score > bestScore) {
                    bestScore = score;
                    priceStr = matcher.group();
                }
            }
        }
        return priceStr != null ? new BigDecimal(priceStr) : null;
    }

    @Nullable
    private static Date findDate(List<OcrText> lines) {
        List<Date> dates = new ArrayList<>();
        for (OcrText line : lines) {
            for (OcrText w : line.childs()) {
                Matcher matcher = DATE_DMY.matcher(w.textSanitizedNum());
                if (matcher.find()) {
                    int day = Integer.valueOf(matcher.group(DMY_DAY));
                    int month = Integer.valueOf(matcher.group(DMY_MONTH));
                    int year = Integer.valueOf(matcher.group(DMY_YEAR));
                    //todo check if day is compatible with month (29-30-31)
                    if (year < 100)
                        year += year > YEAR_CUT ? 1900 : 2000;
                    // correct for 0 based month
                    dates.add(new GregorianCalendar(year, month - 1, day).getTime());
                    break;
                }
            }
            // It's better to avoid word concatenation because it could match a wrong date.
            // Ex: 1/1/20 14:30 -> 1/1/2014:30
        }
        return dates.size() == 1 ? dates.get(0) : null;
    }

    /**
     * Extract a Ticket from an ImageProcessor loaded with a bitmap.
     * @param imgProc ImagePreprocessor with at least an image assigned (corners can be set manually).
     * @return Ticket containing any information found, and/or a list of errors occurred.
     * @author Riccardo Zaglia
     */
    // not tested
    // todo: integrate schemer and other heuristics
    public synchronized OcrTicket analyzeTicket(@NonNull ImageProcessor imgProc) {
        OcrTicket ticket = new OcrTicket();
        ticket.errors = new ArrayList<>();

        Bitmap bm = imgProc.undistortForOCR(0.5);
        if (bm == null) {
            ticket.errors.add(OcrError.INVALID_PROCESSOR);
            return ticket;
        }
        ticket.rectangle = imgProc.getCorners();
        List<OcrText> lines = bitmapToLines(bm, ocrEngine);

        //find amount
        OcrText amountStr = findAmountString(lines, size(bm));
        if (amountStr != null) {
            RectF srcStripRect = getAmountStripRect(amountStr, size(bm));
            Bitmap amountStrip = getAmountStrip(imgProc, size(bm), amountStr, srcStripRect);
            List<OcrText> amountLines = bitmapToLines(amountStrip, ocrEngine);
            ticket.amount = findAmountPrice(amountLines, amountStr, size(srcStripRect), size(amountStrip));
        }
        if (ticket.amount == null)
            ticket.errors.add(OcrError.AMOUNT_NOT_FOUND);

        ticket.date = findDate(lines);
        if (ticket.date == null)
            ticket.errors.add(OcrError.DATE_NOT_FOUND);

        return ticket;
    }

    /**
     * Asynchronous version of analyzeTicket(imgProc). The ticket is passed by the callback parameter.
     * @param imgProc ImagePreprocessor
     * @param ticketCb Callback
     */
    public void analyzeTicket(@NonNull ImageProcessor imgProc, @NonNull Consumer<OcrTicket> ticketCb) {
        new Thread(() -> ticketCb.accept(analyzeTicket(imgProc))).start();
    }

    /**
     * @author Michelon
     * Scale a bitmap to 1/2 its height and width
     * @param b bitmap not null
     * @return scaled bitmap
     */
    @Deprecated
    private Bitmap scaleBitmap(Bitmap b) {
        int reqWidth = b.getWidth()/2;
        int reqHeight = b.getHeight()/2;
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, reqWidth, reqHeight), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
    }


}
