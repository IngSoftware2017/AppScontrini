package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.util.SizeF;
import android.util.SparseArray;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.ing.software.common.CommonUtils.rectFromSize;
import static com.ing.software.common.CommonUtils.size;
import static com.ing.software.ocr.OcrVars.*;
import static java.lang.Math.abs;
import static java.util.Collections.max;

//todo use new functions instead
public class TestFunctions {

    // level of detail of image to be passed to OCR engine
    private static final double OCR_NORMAL_SCALE = 1.;
    private static final double OCR_ADVANCED_SCALE = 1. / 3.;

    // Extended rectangle vertical multiplier
    private static final float EXT_RECT_V_MUL = 3;

    private TextRecognizer ocrEngine = null;

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
    private static List<Scored<Pair<OcrText, Locale>>> findAllScoredAmountStrings(List<OcrText> lines, SizeF bmSize) {
        List<Scored<Pair<OcrText, Locale>>> matchedLines = DataAnalyzer.findAmountStringTexts(lines);
        // modify score for each matched line
        for (Scored<Pair<OcrText, Locale>> line : matchedLines) {
            double score = line.getScore();
            score *= line.obj().first.charWidth() + line.obj().first.charHeight(); // <- todo normalize with all texts average
            score *= 1. - line.obj().first.box().centerY() / bmSize.getHeight(); // <- todo find a better way
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
        return new RectF(amountStr.box().centerX(), amountStr.box().centerY() - halfHeight,
                bmSize.getWidth(), amountStr.box().centerY() + halfHeight);
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
                srcRect.width() / srcRect.height() * CHAR_ASPECT_RATIO
                        / (amountStr.charWidth() / amountStr.charHeight()));
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
        List<Pair<OcrText, BigDecimal>> prices = DataAnalyzer.findAllPricesRegex(lines);
        BigDecimal price = null;
        double bestScore = 0;
        for (Pair<OcrText, BigDecimal> priceLine : prices) {
            OcrText line = priceLine.first;
            double score = 1. - abs(line.box().centerY() - stripRect.centerY()) / stripRect.height(); // y position diff
            score *= 1. - abs(line.charWidth() - amountStr.charWidth()) / amountStr.charWidth(); // char width diff
            score *= 1. - abs(line.charHeight() - amountStr.charHeight()) / amountStr.charHeight(); // char height diff
            if (score > bestScore && priceLine.second != null) {
                bestScore = score;
                price = priceLine.second;
            }
        }
        return price;
    }

//    /**
//     * Extract a Ticket from an ImageProcessor loaded with a bitmap.
//     * @param imgProc ImagePreprocessor with at least an image assigned (corners can be set manually).
//     * @return Ticket containing any information found, and/or a list of errors occurred.
//     *
//     * @author Riccardo Zaglia
//     */
//    // todo: integrate schemer and other heuristics
//    public synchronized OcrTicket analyzeTicket(@NonNull ImageProcessor imgProc, boolean advanced) {
//        OcrTicket ticket = new OcrTicket();
//        ticket.errors = new ArrayList<>();
//
//        Bitmap bm = imgProc.undistortForOCR(advanced ? OCR_ADVANCED_SCALE : OCR_NORMAL_SCALE);
//        if (bm == null) {
//            ticket.errors.add(OcrError.INVALID_PROCESSOR);
//            return ticket;
//        }
//        ticket.rectangle = imgProc.getCorners();
//        List<OcrText> lines = runOCR(bm, ocrEngine);
//
//        //find amount
//        List<Scored<OcrText>> amountStrs = findAllScoredAmountStrings(lines, size(bm));
//        if (amountStrs.size() > 0) {
//            OcrText amountStr = max(amountStrs).obj();
//            RectF srcAmountStripRect = getAmountStripRect(amountStr, size(bm));
//            Bitmap amountStrip = getAmountStrip(imgProc, size(bm), amountStr, srcAmountStripRect);
//            RectF dstAmountStripRect = rectFromSize(size(amountStrip));
//            List<OcrText> amountLinesStripSpace = runOCR(amountStrip, ocrEngine);
//
//            // transform texts from destination to source space (I swap source and destination rect).
//            List<OcrText> amountLinesBmSpace = Stream.of(amountLinesStripSpace)
//                    .map(line -> new OcrText(line, dstAmountStripRect, srcAmountStripRect)).toList();
//            ticket.total = findAmountPrice(amountLinesBmSpace, amountStr, srcAmountStripRect);
//        }
//        if (ticket.total == null)
//            ticket.errors.add(OcrError.AMOUNT_NOT_FOUND);
//
//        List<Pair<OcrText, Date>> dates = invoke((lines);
//        if (dates.size() == 1) {
//            ticket.date = dates.get(0).second;
//        } else {
//            ticket.errors.add(OcrError.DATE_NOT_FOUND);
//        }
//
//        return ticket;
//    }
//
//    /**
//     * Asynchronous version of analyzeTicket(imgProc). The ticket is passed by the callback parameter.
//     * @param imgProc ImagePreprocessor
//     * @param ticketCb Callback
//     */
//    public void analyzeTicket(
//            @NonNull ImageProcessor imgProc, boolean advanced, @NonNull Consumer<OcrTicket> ticketCb) {
//        new Thread(() -> ticketCb.accept(analyzeTicket(imgProc, advanced))).start();
//    }
}
