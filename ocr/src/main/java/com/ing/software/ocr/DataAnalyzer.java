package com.ing.software.ocr;


import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Pair;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OperativeObjects.ListAmountOrganizer;
import com.ing.software.ocr.OperativeObjects.WordMatcher;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;

import com.annimon.stream.Stream;

import static com.ing.software.ocr.OcrVars.*;
import static java.util.Collections.max;

/**
 * Class used to extract information from raw data
 */
public class DataAnalyzer {

    /**
     * Get a list of texts where amount string is present
     * @param texts list of texts to analyze. Not null.
     * @return list of texts containing amount string with its score
     */
    static List<Scored<OcrText>> getAmountTexts(@NonNull List<OcrText> texts) {
        return findAllMatchedStrings(texts, AMOUNT_MATCHERS);
    }

    /**
     * @author Michelon
     * Insert detected amount texts in a listAmountOrganizer
     * @param texts list of scored source texts
     * @return list of listAmountOrganizer containing source texts
     */
    static List<ListAmountOrganizer> organizeAmountList(@NonNull List<Scored<OcrText>> texts) {
        return Stream.of(texts)
                    .map(ListAmountOrganizer::new)
                    .toList();
    }

    /**
    * Find all TextLines which text is matched by any of the list of matchers.
    * @param lines list of TextLines. Can be empty
    * @return TextLines matched. Can be empty if no match is found.
    * @author Riccardo Zaglia
    */
    private static List<Scored<OcrText>> findAllMatchedStrings(List<OcrText> lines, List<WordMatcher> matchers) {
        return Stream.of(lines)
                .map(line -> new Scored<>(max(Stream.of(matchers).map(m -> m.match(line)).toList()), line))
                .filter(s -> s.getScore() != 0).toList();
    }

    /**
     * Get possible amount from word matcher and regex
     * @param texts list of scored target texts (prices). Not null.
     * @return text containing amount price and it's decoded value
     */
    static Pair<OcrText, BigDecimal>  getMatchingAmount(@NonNull List<Scored<OcrText>> texts) {
        for (Scored<OcrText> singleText : texts) {
            BigDecimal amount = trySingleMatch(singleText.obj());
            if (amount != null)
                return new Pair<>(singleText.obj(), amount);
        }
        return null;
    }

    /**
     * Get amount for single text from word matcher and regex
     * @param line text containing amount price. Not null.
     * @return Decoded value. Null if nothing found.
     * @author Zaglia
     */
    private static BigDecimal trySingleMatch(@NonNull OcrText line) {
        /*
        Matcher matcher = PRICE_NO_THOUSAND_MARK.matcher(line.numNoSpaces());
        if (!matcher.find()) { // try again using a dot to concatenate words
            matcher = PRICE_NO_THOUSAND_MARK.matcher(line.numConcatDot());
           if (matcher.find())
               return new BigDecimal(matcher.group());
        } else
            return new BigDecimal(matcher.group());
        return null;
        */
        Matcher matcher = PRICE_NO_THOUSAND_MARK.matcher(line.textSanitizedNum());
        boolean matched = matcher.find();
        int childsTot = line.children().size();
        if (!matched && childsTot >= 2) { // merge only last two words and try again
            matcher = PRICE_NO_THOUSAND_MARK.matcher(
                    line.children().get(childsTot - 1).textSanitizedNum()
                            + line.children().get(childsTot - 2).textSanitizedNum());
            matched = matcher.find();
        }
        if (matched) {
            return new BigDecimal(matcher.group().replaceAll(" ", ""));
        }
        return null;
    }

    /**
     * @author Michelon
     * Get restored amount without regex
     * @param texts list of scored target texts (prices). Not null.
     * @return text containing amount price and it's decoded value
     */
    static Pair<OcrText, BigDecimal> getRestoredAmount(@NonNull List<Scored<OcrText>> texts) {
        for (Scored<OcrText> singleText : texts) {
            if (ScoreFunc.isPossiblePriceNumber(singleText.obj().textNoSpaces(), singleText.obj().numNoSpaces()) < NUMBER_MIN_VALUE) {
                BigDecimal amount = analyzeAmount(singleText.obj().numNoSpaces());
                if (amount != null)
                    return new Pair<>(singleText.obj(), amount);
            }
        }
        return null;
    }

    /*
    Old analysis. Used alongside regex.
     */

    /**
     * @author Michelon
     * Tries to find a BigDecimal from string
     * @param amountString string containing possible amount. Length > 0.
     * @return BigDecimal containing the amount, null if no number was found
     */
    public static BigDecimal analyzeAmount(@Size(min = 1) String amountString) {
        BigDecimal amount = null;
        try {
            String decoded = deepAnalyzeAmountChars(amountString);
            if (!decoded.equals(""))
                amount = new BigDecimal(decoded);
        } catch (Exception e1) {
            amount = null;
        }
        return amount;
    }

    /**
     * @author Michelon
     * Analyze a string (reversed by this method) looking for a number (with two decimals).
     * Uses arbitrary decisions.
     * @param targetAmount string containing possible amount. Length > 0.
     * @return string containing the amount, empty stringBuilder if nothing found
     */
    private static String deepAnalyzeAmountChars(@Size(min = 1) String targetAmount){
        StringBuilder manipulatedAmount;
        StringBuilder reversedAmount = new StringBuilder(targetAmount).reverse();
        OcrUtils.log(4,"deepAnalyzeAmount", "Deep amount analysis for: " + targetAmount);
        OcrUtils.log(5,"deepAnalyzeAmount", "Reversed amount is: " + reversedAmount.toString());
        manipulatedAmount = analyzeCharsLong(reversedAmount.toString());
        OcrUtils.log(4,"deepAnalyzeAmount", "Analyzed amount is: " + manipulatedAmount.toString());
        return manipulatedAmount.reverse().toString();
    }

    /**
     * @author Michelon
     * Analyze a string looking for a number (with two decimals)
     * @param source string containing possible amount. Length > 0.
     * @return stringBuilder containing the amount, empty stringBuilder if nothing found
     */
    private static StringBuilder analyzeCharsLong(@Size (min = 1) String source) {
        boolean isNegative = (source.charAt(source.length()-1) == '-');
        source = removeLetters(source);
        StringBuilder manipulatedAmount = new StringBuilder();
        //Check if there are at least 2 dec + '.' + 1 num = 4 chars
        if (source.length() >= 4) {
            char char0 = source.charAt(0);
            char char1 = source.charAt(1);
            char char2 = source.charAt(2);
            char char3 = source.charAt(3);
            if (char0 == '.' && char1 != '.' && char2 != '.' && char3 != '.')
                manipulatedAmount.append("00").append(char0).append(char1).append(removeRedundantPoints(source.substring(2)));
            else if (char0 == '.') //There is another '.' remove the first one
                return analyzeCharsLong(source.substring(1));
            else if (char1 == '.' && Character.isDigit(char2) && Character.isDigit(char3)) //now char0 must be digit
                manipulatedAmount.append("0").append(char0).append(char1).append(removeRedundantPoints(source.substring(2)));
            else if (char1 == '.') //char0 must be digit and char2 or char3 must be '.' = remove char1
                return analyzeCharsLong(String.valueOf(char0) + source.substring(2));
            else if (char2 == '.') //char0 and char1 must be digit = if char2 is '.' everything is ok
                manipulatedAmount.append(char0).append(char1).append(char2).append(removeRedundantPoints(source.substring(3)));
            else if (char3 == '.') //char0, 1, 2 must be digit = if char3 is '.' char0 was added by detector = remove it
                return analyzeCharsLong(source.substring(1));
            else //we have 4 digits, suppose we did not find the '.' = add it after char0 and char1
                manipulatedAmount.append(char0).append(char1).append('.').append(removeRedundantPoints(source.substring(2)));
            if (isNegative)
                manipulatedAmount.append("-");
            return manipulatedAmount;
        } else if (source.length() > 0 && isNegative)
            return analyzeChars(source).append("-");
        else if (source.length() > 0)
            return analyzeChars(source);
        else
            return manipulatedAmount;
    }

    /**
     * @author Michelon
     * Keep only digits and points in a string
     * @param string source string. Length > 0.
     * @return string with only digits and '.'
     */
    private static String removeLetters(@Size(min = 1) String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); ++i)
            if (Character.isDigit(string.charAt(i)) || string.charAt(i) == '.')
                result.append(string.charAt(i));
        return result.toString();
    }

    /**
     * @author Michelon
     * Removes '.' from a string
     * @param string source string. Length > 0
     * @return string with no '.'
     */
    private static String removeRedundantPoints(@Size(min = 1) String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); ++i)
            if (string.charAt(i) != '.')
                result.append(string.charAt(i));
        return result.toString();
    }

    /**
     * @author Michelon
     * Analyze a string looking for a number with two decimals
     * @param source source string @Size(min = 1, max = 3)
     * @return StringBuilder with decoded number (empty if nothing found)
     */
    private static StringBuilder analyzeChars(@Size(min = 1) String source) {
        if (source.length()==3)
            return analyzeChars(source.charAt(0), source.charAt(1), source.charAt(2));
        else if (source.length() == 2)
            return analyzeChars(source.charAt(0), source.charAt(1));
        else
            return analyzeChars(source.charAt(0));
    }

    /**
     * @author Michelon
     * Analyze three chars, uses arbitrary decisions to extract a number with two decimals.
     * @param char0 first char. Not null.
     * @param char1 second char. Not null.
     * @param char2 third char. Not null.
     * @return StringBuilder with decoded number (empty if nothing found)
     */
    private static StringBuilder analyzeChars(char char0, char char1, char char2) {
        StringBuilder result = new StringBuilder();
        if (Character.isDigit(char0) && Character.isDigit(char1) && Character.isDigit(char2)) {
            //point is missing = add it after char0 and char1
            return result.append(char0).append(char1).append(".").append(char2);
        } else if (Character.isDigit(char0) && char1 == '.' && Character.isDigit(char2)) {
            //one decimal is missing, suppose it's the last one
            return  result.append("0").append(char0).append(char1).append(char2);
        } else if (char0 == '.' && Character.isDigit(char1) && Character.isDigit(char2)) {
            //Missing both decimals (or maybe found a point that shouldn't be there)
            return result.append("00").append(char0).append(char1).append(char2);
        } else if (Character.isDigit(char0) && Character.isDigit(char1) && char2 == '.') {
            //Missing first integer, use 0
            return result.append(char0).append(char1).append(char2).append("0");
        } else if (Character.isDigit(char0)) //char1 and char2 must be points
            return analyzeChars(char0, char1);
        else //char0 is point, can be removed as char1 or char2 is also a point
            return analyzeChars(char1, char2);
    }

    /**
     * @author Michelon
     * Analyze two chars, uses arbitrary decisions to extract a number with two decimals.
     * @param char0 first char. Not null.
     * @param char1 second char. Not null.
     * @return StringBuilder with decoded number (empty if nothing found)
     */
    private static StringBuilder analyzeChars(char char0, char char1) {
        StringBuilder result = new StringBuilder();
        if (Character.isDigit(char0) && Character.isDigit(char1)) {
            //point is missing = add after char0 and char1 and add '0' (actually this may be anything)
            return result.append(char0).append(char1).append(".0");
        } else if (Character.isDigit(char0) && char1 == '.') {
            //one decimal is missing, suppose it's the last one
            return  result.append("0").append(char0).append(char1).append("0");
        } else if (char0 == '.' && Character.isDigit(char1)) {
            //Missing both decimals (or maybe found a point that shouldn't be there)
            return result.append("00").append(char0).append(char1);
        } else //Both are points
            return result;
    }

    /**
     * @author Michelon
     * Analyze single char, uses arbitrary decisions to extract a number with two decimals.
     * @param char0 first char. Not null.
     * @return StringBuilder with decoded number (empty if nothing found)
     */
    private static StringBuilder analyzeChars(char char0) {
        StringBuilder result = new StringBuilder();
        if (Character.isDigit(char0))
            return result.append("00.").append(char0);
        else
            return result;
    }
}
