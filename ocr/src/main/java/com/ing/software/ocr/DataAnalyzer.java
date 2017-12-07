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
 */
public class DataAnalyzer {

    /**
     * @author Michelon
     * Search through results from the research of amount string and retrieves the text with highest
     * probability to contain the amount calculated with (probability from grid - distanceFromTarget*10).
     * If no amount was found in first result iterate through all results following previous ordering.
     * @param amountResults list of RawStringResult from amount search. Not null.
     * @return BigDecimal containing the amount found. Null if nothing found
     */
    static BigDecimal getPossibleAmount(@NonNull List<RawStringResult> amountResults) {
        List<RawGridResult> possibleResults = new ArrayList<>();
        Collections.sort(amountResults);
        for (RawStringResult stringResult : amountResults) {
            //Ignore text with invalid distance (-1) according to findSubstring() documentation
            if (stringResult.getDistanceFromTarget() >= 0) {
                RawText sourceText = stringResult.getSourceText();
                int singleCatch = sourceText.getAmountProbability() - stringResult.getDistanceFromTarget() * 10;
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
            they are inserted.
            */
            Collections.sort(possibleResults);
            BigDecimal amount;
            for (RawGridResult result : possibleResults) {
                String amountString = result.getText().getDetection();
                OcrUtils.log(2,"getPossibleAmount", "Possible amount is: " + amountString);
                amount = analyzeAmount(amountString);
                if (amount != null) {
                    OcrUtils.log(2, "getPossibleAmount", "Decoded value: " + amount);
                    return amount;
                }
            }
        }
        else {
            OcrUtils.log(2,"getPossibleAmount", "No parsable result ");
            return null;
        }
        OcrUtils.log(2,"getPossibleAmount", "No parsable amount ");
        return null;
    }

    /**
     * @author Michelon
     * Tries to find a BigDecimal from string
     * @param amountString string containing possible amount. Length > 0.
     * @return BigDecimal containing the amount, null if no number was found
     */
    static BigDecimal analyzeAmount(@Size(min = 1) String amountString) {
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountString);
        } catch (NumberFormatException e) {
            try {
                amount = new BigDecimal(deepAnalyzeAmount(amountString));
            } catch (Exception e1) {
                amount = null;
            }
        } catch (Exception e2) {
            amount = null;
        }
        if (amount != null)
            amount = amount.setScale(2, RoundingMode.HALF_UP);
        return amount;
    }

    /**
     * @author Michelon
     * Tries to find a number in string that may contain also letters (ex. '€' recognized as 'e')
     * @param targetAmount string containing possible amount. Length > 0.
     * @return string containing the amount, null if no number was found
     */
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

    //Seems that the price under the correct one is usually correct, the manager
    // will have to find it (tip = search under amount rect) and compare with this one and
    // with the result of products search...
    private static String deepAnalyzeAmountV2(@Size(min = 1) String targetAmount){
        targetAmount = targetAmount.replaceAll(",", ".").replaceAll(" ", "")
                .replaceAll("S", "5");
        StringBuilder manipulatedAmount = new StringBuilder();
        StringBuilder reversedAmount = new StringBuilder(targetAmount).reverse();
        OcrUtils.log(3,"deepAnalyzeAmount", "Deep amount analysis for: " + targetAmount);
        //Check if there are at least 2 dec + '.' + 1 num
        if (reversedAmount.length() >= 4) {
            char char0 = reversedAmount.charAt(0);
            char char1 = reversedAmount.charAt(1);
            char char2 = reversedAmount.charAt(2);
            char char3 = reversedAmount.charAt(3);
        } else {
            manipulatedAmount = new StringBuilder(analyzeChars(reversedAmount.toString()));
        }
        return manipulatedAmount.toString();
    }
    /*
    Metti la stringa sottosopra.
    Prendi i primi tre char, se sono 2 number e un '.' allora sono i decimali
        Se il '.' è in posiz 0, verifica di avere almeno un numero dopo, se si allora aggiungi
            due 0 all'inizio. Se non hai numeri dopo, riparti dall'inizio scartando il punto.
        Se il '.' è in posiz 1, verifica che in 0 ci sia un numero, se si controlla che ci almeno un
            num dopo il '.' se no scarta la parte prima
        Se sono 3 num e fino alla fine non c'è alcun punto, supponi che non sia stato individuato
            e aggiungilo dopo i primi due decimali.
        Se sono 3 num e c'è un punto in posiz 3, scarta il primo numero.


        //if not suppose one is missing
            char char0 = '\u0000';
            char char1 = '\u0000';
            char char2 = '\u0000';
            String result = "";
            if (reversedAmount.length() == 1)
                char0 = reversedAmount.charAt(0);
            if (reversedAmount.length() == 2)
                char1 = reversedAmount.charAt(1);
            if (reversedAmount.length() == 3)
                char2 = reversedAmount.charAt(2);
            if (Character.isDigit(char0) && Character.isDigit(char1) && Character.isDigit(char2)) {
                //point is missing = add after char0 and char1
                result = String.valueOf(char0) + String.valueOf(char1) + "." + String.valueOf(char2);
            } else if (Character.isDigit(char0) && char1 == '.' && Character.isDigit(char2)) {
                //one decimal is missing, suppose it's the last one
                result =  "0" + String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2);
            } else if (char0 == '.' && Character.isDigit(char1) && Character.isDigit(char2)) {
                //Missing both decimals (or maybe found a point that shouldn't be there)
                result = "00" + String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2);
            } else if (Character.isDigit(char0) && Character.isDigit(char1) && char2 == '.') {
                //Missing first integer, use 0
                result = String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2) + "0";
            }
     */

    private static String analyzeChars(@Size (max = 3) String source) {
        if (source.length()==3)
            return analyzeChars(source.charAt(0), source.charAt(1), source.charAt(2));
        else if (source.length() == 2)
            return analyzeChars(source.charAt(0), source.charAt(1));
        else
            return source;
    }

    private static String analyzeChars(char char0, char char1, char char2) {
        if (Character.isDigit(char0) && Character.isDigit(char1) && Character.isDigit(char2)) {
            //point is missing = add after char0 and char1
            return String.valueOf(char0) + String.valueOf(char1) + "." + String.valueOf(char2);
        } else if (Character.isDigit(char0) && char1 == '.' && Character.isDigit(char2)) {
            //one decimal is missing, suppose it's the last one
            return  "0" + String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2);
        } else if (char0 == '.' && Character.isDigit(char1) && Character.isDigit(char2)) {
            //Missing both decimals (or maybe found a point that shouldn't be there)
            return "00" + String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2);
        } else if (Character.isDigit(char0) && Character.isDigit(char1) && char2 == '.') {
            //Missing first integer, use 0
            return String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2) + "0";
        } else
            return String.valueOf(char0) + String.valueOf(char1) + String.valueOf(char2);
    }

    private static String analyzeChars(char char0, char char1) {
        if (Character.isDigit(char0) && Character.isDigit(char1)) {
            //point is missing = add after char0 and char1 and add '0' (actually this may be anything)
            return String.valueOf(char0) + String.valueOf(char1) + ".0";
        } else if (Character.isDigit(char0) && char1 == '.') {
            //one decimal is missing, suppose it's the last one
            return  "0" + String.valueOf(char0) + String.valueOf(char1) + "0";
        } else if (char0 == '.' && Character.isDigit(char1)) {
            //Missing both decimals (or maybe found a point that shouldn't be there)
            return "00" + String.valueOf(char0) + String.valueOf(char1);
        } else
            return String.valueOf(char0) + String.valueOf(char1);
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

        //Splits the string into tokens
        String[] pack = text.split("\\s");

        String[] formatDate = {"xx/xx/xxxx", "xx/xx/xxxx", "xxxx/xx/xx","xx-xx-xxxx", "xx-xx-xxxx", "xxxx-xx-xx", "xx.xx.xxxx", "xx.xx.xxxx", "xxxx.xx.xx"};

        //Maximum number of characters in the date format
        int minDistance = 10;
        String dataSearch = null;

        for (String p : pack) {
            for (String d : formatDate) {
                //Convert string to uppercase
                int distanceNow = levDistance(p.toUpperCase(), d.toUpperCase());
                if (distanceNow < minDistance)
                {
                    minDistance = distanceNow;
                    dataSearch = p.toUpperCase();
                }

            }
        }
        return dataSearch;
    }
}
