package com.ing.software.ocr.Legacy;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.google.android.gms.vision.text.Text;
import com.ing.software.ocr.DataAnalyzer;
import com.ing.software.ocr.OcrUtils;
import com.ing.software.ocr.OperativeObjects.AmountComparator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.ing.software.ocr.OcrVars.NUMBER_MIN_VALUE_ALTERNATIVE;
import static com.ing.software.ocr.OperativeObjects.AmountComparator.getPricesList;

/**
 *
 */
@Deprecated
public class OcrManagerDep {

    /**
     * @author Michelon
     * @date 3-1-18
     * Analyze possible RawTexts containing amount. When one is found check if it consistent with list of products,
     * subtotal, cash and change.
     * @param possibleResults List of RawGridResults containing possible amount. Not null.
     * @param products List of RawTexts containing possible prices for products. Not null.
     * @return BigDecimal containing detected amount. null if nothing found.
     */
    private static BigDecimal extendedAmountAnalysis(@NonNull List<RawGridResult> possibleResults, @NonNull List<RawText> products) {
        BigDecimal amount = null;
        RawText amountText = null;
        if (possibleResults.size() == 0)
            return null;
        for (RawGridResult result : possibleResults) {
            String amountString = result.getText().getValue();
            OcrUtils.log(2, "getPossibleAmount", "Possible amount is: " + amountString);
            amount = DataAnalyzerDep.analyzeAmount(amountString);
            if (amount != null) {
                OcrUtils.log(2, "getPossibleAmount", "Decoded value: " + amount);
                amountText = result.getText();
                break;
            }
        }
        if (amount == null) {
            //Create a sample rawText to emulate an amount, using first result as source
            amountText = getDummyAmountText(possibleResults.get(0).getText());
        }
        AmountComparator amountComparator = new AmountComparator(amountText, amount);
        //check against list of products and cash + change
        List<RawGridResult> possiblePrices = getPricesList(amountText, products);
        amountComparator.analyzeTotals(possiblePrices);
        amountComparator.analyzePrices(possiblePrices);
        amount = amountComparator.getBestAmount(0);
        return amount;
    }

    /**
     * Extracts first not null date from list of ordered dates
     * @param dateList list of ordered RawGridResults containing possible dates. Not null
     * @return First possible date. Null if nothing found
     */
    private static Date getDateFromList(@NonNull List<RawGridResult> dateList) {
        for (RawGridResult gridResult : dateList) {
            String possibleDate = gridResult.getText().getValue();
            OcrUtils.log(2, "getDateFromList", "Possible date is: " + possibleDate);
            Date evaluatedDate = DataAnalyzerDep.getDate(possibleDate);
            if (evaluatedDate != null) {
                OcrUtils.log(2, "getDateFromList", "Possible extended date is: " + evaluatedDate.toString());
                return evaluatedDate;
            }
        }
        return null;
    }

    /**
     * @author Michelon
     * @date 3-1-18
     * Get a dummy RawText on right side of image at the same height of source rect
     * @param source source RawText. Not null.
     * @return a dummy RawText
     */
    private static RawText getDummyAmountText(@NonNull RawText source) {
        Rect amountRect = new Rect(source.getBoundingBox());
        amountRect.set(source.getRawImage().getWidth()/2, source.getBoundingBox().top,
                source.getRawImage().getWidth(), source.getBoundingBox().bottom);
        Text text = new Text() {
            @Override
            public String getValue() {
                return "";
            }

            @Override
            public Rect getBoundingBox() {
                return amountRect;
            }

            @Override
            public Point[] getCornerPoints() {
                Point a = new Point(amountRect.left, amountRect.top);
                Point b = new Point(amountRect.right, amountRect.top);
                Point c = new Point(amountRect.left, amountRect.bottom);
                Point d = new Point(amountRect.right, amountRect.bottom);
                return new Point[]{a, b, c, d};
            }

            @Override
            public List<? extends Text> getComponents() {
                return null;
            }
        };
        return new RawText(text, source.getRawImage());
    }

    /**
     * We have no valid amount from string search. Try to decode the amount only from products prices.
     * @param texts List of prices
     * @return possible amount. Null if nothing found.
     */
    private static BigDecimal analyzeAlternativeAmount(List<RawText> texts) {
        if (texts.size() == 0)
            return null;
        OcrUtils.log(2, "AlternativeAmount", "No amount was found, use brute search");
        RawText currentText = null;
        int i = 0;
        while (currentText == null && i < texts.size()) {
            RawText text = texts.get(i);
            if (OcrUtils.isPossiblePriceNumber(text.getValue()) < NUMBER_MIN_VALUE_ALTERNATIVE) {
                currentText = text;
            }
            ++i;
        }
        while (i < texts.size()) {
            RawText text = texts.get(i);
            if (text.getAmountProbability() > currentText.getAmountProbability() && OcrUtils.isPossiblePriceNumber(text.getValue()) < NUMBER_MIN_VALUE_ALTERNATIVE)
                currentText = text;
            ++i;
        }
        if (currentText == null)
            return null; //If no rawText pass the above if, we still have a valid text in currentText, so we must recheck it
        OcrUtils.log(2, "AlternativeAmount", "Possible amount is: " + currentText.getValue());
        BigDecimal amount = DataAnalyzerDep.analyzeAmount(currentText.getValue());
        if (amount == null) {
            OcrUtils.log(2, "AlternativeAmount", "No decoded value");
            return null;
        }
        AmountComparator amountComparator = new AmountComparator(currentText, amount);
        //check against list of products and cash + change
        List<RawGridResult> possiblePrices = getPricesList(currentText, texts);
        amountComparator.analyzeTotals(possiblePrices);
        amountComparator.analyzePrices(possiblePrices);
        amount = amountComparator.getBestAmount(1);
        if (amount != null)
            OcrUtils.log(2, "AlternativeAmount", "Maximized amount is: " + amount.toString());
        else
            OcrUtils.log(2, "AlternativeAmount", "No amount found.");
        return amount;
    }
}
