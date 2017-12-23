package com.ing.software.ocr;

import java.math.RoundingMode;
import java.util.List;

import android.support.annotation.NonNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawStringResult;
import com.ing.software.ocr.OcrObjects.RawText;

import android.support.annotation.IntRange;
import android.support.annotation.Size;

import static com.ing.software.ocr.OcrUtils.levDistance;


/**
 * Class used to extract informations from raw data
 * todo: fallback, if no amount is present try to decode a possible pricelist
 * todo: remove rectangle-probability and use block-specific-probability
 */
class DataAnalyzer {

    /**
     * @author Michelon
     * Search through results from the research of amount string and retrieves the text with highest
     * probability to contain the amount calculated with (probability from grid - distanceFromTarget*distanceMultiplier).
     * If no amount was found in first result iterate through all results following previous order.
     * @param amountResults list of RawStringResult from amount search. Not null.
     * @return List of ordered possible amounts as RawGridResults.
     */
    static List<RawGridResult> getPossibleAmounts(@NonNull List<RawStringResult> amountResults) {
        int distanceMultiplier = 15;
        List<RawGridResult> possibleResults = new ArrayList<>();
        Collections.sort(amountResults);
        for (RawStringResult stringResult : amountResults) {
            //Ignore text with invalid distance (-1) according to findSubstring() documentation
            if (stringResult.getDistanceFromTarget() > -1) {
                RawText sourceText = stringResult.getSourceText();
                int singleCatch = sourceText.getAmountProbability() - stringResult.getDistanceFromTarget() * distanceMultiplier;
                if (stringResult.getDetectedTexts() != null) {
                    //Here we order texts according to their distance (position) from source rect
                    List<RawText> orderedDetectedTexts = OcrUtils.orderRawTextFromRect(stringResult.getDetectedTexts(), stringResult.getSourceText().getRect());
                    for (RawText rawText : orderedDetectedTexts) {
                        if (!rawText.equals(sourceText)) {
                            possibleResults.add(new RawGridResult(rawText, singleCatch));
                            OcrUtils.log(3, "getPossibleAmount", "Analyzing source text: " + sourceText.getDetection() +
                                    " where target is: " + rawText.getDetection() + " with probability: " + sourceText.getAmountProbability() +
                                    " and distance: " + stringResult.getDistanceFromTarget());
                        }
                    }
                }
            } else {
                OcrUtils.log(3, "getPossibleAmount", "Ignoring text: " + stringResult.getSourceText().getDetection());
            }
        }
        if (possibleResults.size() > 0) {
            /* Here we order considering their final probability to contain the amount:
            If the probability is the same, the fallback is their previous order, so based on when
            they are inserted (=their distance (position) from source rect).
            */
            Collections.sort(possibleResults);
        }
        return possibleResults;
    }

    /**
     * @author Michelon
     * @date 23-12-17
     * Tries to find a BigDecimal from string
     * @param amountString string containing possible amount. Length > 0.
     * @return BigDecimal containing the amount, null if no number was found
     */
    static BigDecimal analyzeAmount(@Size(min = 1) String amountString) {
        BigDecimal amount = null;
        if (OcrUtils.isPossibleNumber(amountString)) {
            try {
                String decoded = deepAnalyzeAmountChars(amountString);
                if (!decoded.equals(""))
                    amount = new BigDecimal(decoded);
            } catch (Exception e1) {
                amount = null;
            }
            if (amount != null)
                amount = amount.setScale(2, RoundingMode.HALF_UP);
        }
        return amount;
    }

    /**
     * @author Michelon
     * Tries to find a number in string that may contain also letters (ex. '€' recognized as 'e')
     * @param targetAmount string containing possible amount. Length > 0.
     * @return string containing the amount, null if no number was found
     */
    @Deprecated
    private static String deepAnalyzeAmount(@Size(min = 1) String targetAmount){
        targetAmount = targetAmount.replaceAll(",", ".").replaceAll("S", "5");
        StringBuilder manipulatedAmount = new StringBuilder();
        OcrUtils.log(2,"deepAnalyzeAmount", "Deep amount analysis for: " + targetAmount);
        boolean numberPresent = false; //used because length can be > 0 if '.' was found but no number
        for (int i = 0; i < targetAmount.length(); ++i) {
            char singleChar = targetAmount.charAt(i);
            if (Character.isDigit(singleChar)) {
                manipulatedAmount.append(singleChar);
                numberPresent = true;
            } else if (singleChar=='.') {
                //Should be replaced with a better analysis
                if (targetAmount.length()-1 != i) { //bad way to check if it's last '.'
                    String temp = manipulatedAmount.toString().replaceAll("\\.", ""); //Replace previous '.' so only last '.' is saved
                    manipulatedAmount = new StringBuilder(temp);
                }
                manipulatedAmount.append(singleChar);
            //} else if (isExp(targetAmount, i)) { //Removes previous exponents
            //    String temp = manipulatedAmount.toString().replaceAll("E", "");
            //    temp = temp.replaceAll("\\+", "");
            //    temp = temp.replaceAll("-", "");
            //    manipulatedAmount = new StringBuilder(temp);
            //    manipulatedAmount.append(getExp(targetAmount, i));
            } else if (singleChar == '-' && manipulatedAmount.length() == 0) { //If negative number
                manipulatedAmount.append(singleChar);
            }
        }
        if (manipulatedAmount.toString().length() == 0 || !numberPresent)
            return null;
        //If last char is '.' remove it
        if (manipulatedAmount.toString().charAt(manipulatedAmount.length()-1) == '.')
            manipulatedAmount.setLength(manipulatedAmount.length()-1);
        return manipulatedAmount.toString();
    }

    /**
     * @author Michelon
     * @date 8-12-17
     * Analyze a string (reversed by this method) looking for a number (with two decimals).
     * Uses arbitrary decisions.
     * @param targetAmount string containing possible amount. Length > 0.
     * @return string containing the amount, empty stringBuilder if nothing found
     */
    private static String deepAnalyzeAmountChars(@Size(min = 1) String targetAmount){
        StringBuilder manipulatedAmount;
        StringBuilder reversedAmount = new StringBuilder(targetAmount.replaceAll(",", ".")
                .replaceAll(" ", "").replaceAll("S", "5")).reverse();
        OcrUtils.log(3,"deepAnalyzeAmount", "Deep amount analysis for: " + targetAmount);
        OcrUtils.log(3,"deepAnalyzeAmount", "Reversed amount is: " + reversedAmount.toString());
        manipulatedAmount = analyzeCharsLong(reversedAmount.toString());
        return manipulatedAmount.reverse().toString();
    }

    /**
     * @author Michelon
     * @date 9-12-17
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
     * @date 8-12-17
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
     * @date 8-12-17
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
     * @date 8-12-17
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
     * @date 8-12-17
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
     * @date 8-12-17
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
     * @date 8-12-17
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

    /**
     * @author Michelon
     * Check if at chosen index the string contains an exponential form.
     * Exp are recognized if they are in the form:
     * E'num'
     * E+'num'
     * E-'num'
     * where 'num' is a number
     * @param text source string. Length > 0.
     * @param startingPoint position of 'E' (from 0 to text.length-1). Int >= 0.
     * @return true if it's a valid exponential form
     */
    static boolean isExp(@Size(min = 1) String text, @IntRange(from = 0) int startingPoint) {
        if (text.length() <= startingPoint + 2) //There must be at least E'num'
            return false;
        if (text.charAt(startingPoint)!='E')
            return false;
        else
            if (Character.isDigit(text.charAt(startingPoint + 1)))
                return true;
            else if (text.charAt(startingPoint + 1) == '+' || text.charAt(startingPoint + 1) == '-')
                return Character.isDigit(text.charAt(startingPoint + 2));
        return false;
    }

    /**
     * @author Michelon
     * Get exponential form from chosen string.
     * Note: isExp() must return true for these same text and startingPoint
     * @param text source text. Length > 0.
     * @param startingPoint position of 'E'. Int >= 0.
     * @return String containing the exponential form (only 'E' and, if present, '+' or '-')
     */
    static String getExp(@Size(min = 1) String text, @IntRange(from = 0) int startingPoint) {
        if (Character.isDigit(text.charAt(startingPoint + 1)))
            return String.valueOf(text.charAt(startingPoint));
        else if (text.charAt(startingPoint + 1) == '+' || text.charAt(startingPoint + 1) == '-')
            if (Character.isDigit(text.charAt(startingPoint + 2)))
                return text.substring(startingPoint, startingPoint + 2);
        return "";
    }

    /**
     * @author Salvagno
     * Accept a text and check if there is a combination of date format.
     * Controllo per tutte le combinazioni simili a
     * xx/xx/xxxx o xx/xx/xxxx o xxxx/xx/xx
     * xx-xx-xxxx o xx-xx-xxxx o xxxx-xx-xx
     * xx.xx.xxxx o xx.xx.xxxx xxxx.xx.xx
     *
     * @param text The text to find the date format
     * @return the absolute value of the minimum distance found between all combinations,
     * if the distance is >= 10 or the inserted text is empty returns -1
     */
    static int findDate(String text) {
        if (text.length() == 0)
            return -1;

        //Splits the string into tokens
        String[] pack = text.split("\\s");

        String[] formatDate = {"xx/xx/xxxx", "xx/xx/xxxx", "xxxx/xx/xx","xx-xx-xxxx", "xx-xx-xxxx", "xxxx-xx-xx", "xx.xx.xxxx", "xx.xx.xxxx", "xxxx.xx.xx"};

        //Maximum number of characters in the date format
        int minDistance = 10;
        //Th eminimum of number combinations of date format without symbols like '/' or '.' or '-'
        int minCharaterDate = 8;

        for (String p : pack) {
            for (String d : formatDate) {
                //Convert string to uppercase
                int distanceNow = levDistance(p.toUpperCase(), d.toUpperCase());
                if (distanceNow < minDistance)
                    minDistance = distanceNow;
            }
        }

        if(minDistance==10)
            return -1;
        else
            //Returns the absolute value of the distance by subtracting the minimum character
            return Math.abs(minCharaterDate-minDistance);
    }


    /**
     * @author Salvagno
     * It takes a text and returns the date if a similarity is found with a date format
     *
     * @param text The text to find the date
     * @return date or null if the date is not there
     */
    static String getDate(String text) {
        if (text.length() == 0)
            return null;

        //Possible date formats
        String[] formatDate = {"xx/xxxx/xx", "xxxx/xx/xx","xx/xx/xxxx", "xx-xxxx-xx", "xxxx-xx-xx","xx-xx-xxxx","xx.xxxx.xx","xxxx.xx.xx" ,"xx.xx.xxxx"};

        //Analyze the text by removing the spaces
        String text_w_o_space =  text.replace(" ", "");

        //Set the maximum length of the string as the minimum distance
        int minDistance = text_w_o_space.length();
        String dataSearch = null;

        //Search a piece of string as long as the length of the searched string in the text
        int start;
        for (String d : formatDate) {
            int subLength = d.length();
            start = 0;
            int tokenLength = 6; //Set at 6 the minimum number of characters that a date can have (x-x-xx)
            for (int finish = subLength; finish <= (text_w_o_space.length()); finish++) {
                String token = text_w_o_space.substring(start, finish);
                token = token.toUpperCase();
                String tokenNUmber = "";
                //Check if there are letters or 'S', in this case the change in 5
                char[] string = token.toCharArray();
                for (char c : string){
                    boolean isLetter = Character.isDigit(c);
                    if(isLetter)
                        tokenNUmber = tokenNUmber+c;
                    else
                    {
                        if(c == 'S')
                            tokenNUmber = tokenNUmber+'5';
                        else if (c == '-' || c == '.' || c == '/')
                            tokenNUmber = tokenNUmber+c;
                    }

                }
                //Check if the length of characters is greater than the last one found
                if(tokenNUmber.length()>=tokenLength) {
                    int distanceNow = levDistance(tokenNUmber, d.toUpperCase());
                    if (distanceNow <= minDistance) {
                        minDistance = distanceNow;
                        dataSearch = tokenNUmber;
                        tokenLength = tokenNUmber.length();
                    }
                }
                start++;
            }
        }

        //If the distance is greater than 10 which is the maximum number of characters that a date can take, return null
        if(minDistance>=10)
            return null;
        else
            return dataSearch;

    }

    /**
     * @author Michelon
     * Search through results from the research of date string and retrieves the text with highest
     * probability to contain the date calculated with (probability from grid - distanceFromTarget*distanceMultiplier).
     * If no date was found in first result iterate through all results following previous order.
     * @param dateResults list of RawGridResult from date search. Not null.
     * @return List of possible dates ordered
     */
    static List<RawGridResult> getPossibleDates(@NonNull List<RawGridResult> dateResults) {
        int distanceMultiplier = 15;
        List<RawGridResult> possibleResults = new ArrayList<>();
        Collections.sort(dateResults);
        for (RawGridResult gridResult : dateResults) {
            //Ignore text with invalid distance (-1) according to findDate() documentation
            int distanceFromDate = findDate(gridResult.getText().getDetection());
            if (distanceFromDate > -1) {
                int singleCatch = gridResult.getPercentage() - distanceFromDate * distanceMultiplier;
                possibleResults.add(new RawGridResult(gridResult.getText(), singleCatch));
            } else {
                OcrUtils.log(3, "getPossibleDate", "Ignoring text: " + gridResult.getText().getDetection());
            }
        }
        if (possibleResults.size() > 0) {
            Collections.sort(possibleResults);
        }
        return possibleResults;
    }
}
