package com.ing.software.ocr;

import java.math.RoundingMode;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawStringResult;
import com.ing.software.ocr.OcrObjects.RawText;
import com.ing.software.common.Ticket;

import android.support.annotation.IntRange;
import android.support.annotation.Size;

import static com.ing.software.ocr.OcrUtils.levDistance;


/*
USAGE:
1) Instantiate DataAnalyzer;
2) Call initialize() until it returns 0;
3) Call getTicket ad libitum to extract information (Ticket object) from a photo of a ticket.
4) Call release() to release internal resources.
 */

/**
 * Class used to extract informations from raw data
 */
public class DataAnalyzer {

    private final OcrAnalyzer analyzer = new OcrAnalyzer();

    class AnalyzeRequest {
        Bitmap photo;
        OnTicketReadyListener ticketCb;

        AnalyzeRequest(Bitmap bm, OnTicketReadyListener cb) {
            photo = bm;
            ticketCb = cb;
        }
    }

    private Queue<AnalyzeRequest> analyzeQueue = new ConcurrentLinkedQueue<>();
    private boolean analyzing = false;

    /**
     * Initialize OcrAnalyzer
     * @param context Android context
     * @return 0 if everything ok, negative number if an error occurred
     */
    public int initialize(Context context) {
		OcrUtils.log(1, "DataAnalyzer", "Initializing DataAnalyzer");
        return analyzer.initialize(context);
    }

    public void release() {
        analyzer.release();
    }

    /**
     * Get a Ticket from a Bitmap. Some fields of the new ticket can be null.
     * @param photo Bitmap. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     */
    public void getTicket(@NonNull Bitmap photo, final OnTicketReadyListener ticketCb) {
        analyzeQueue.add(new AnalyzeRequest(photo, ticketCb));
        dispatchAnalysis();
    }

    /**
     * Handle analysis requests
     * @author Michelon
     * @author Zaglia
     */
    private void dispatchAnalysis() {
        if (!analyzing){
            analyzing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!analyzeQueue.isEmpty()) {
                        final AnalyzeRequest req = analyzeQueue.remove();
                        final long startTime = System.nanoTime();
                        OcrResult result = analyzer.analyze(req.photo);
                        req.ticketCb.onTicketReady(getTicketFromResult(result));
                        long endTime = System.nanoTime();
                		double duration = ((double)(endTime - startTime))/1000000000;
                        OcrUtils.log(1,"EXECUTION TIME: ", duration + " seconds");
                    }
                }
            }).start();
            analyzing = false;
        }
    }

    /**
     * Coverts an OcrResult into a Ticket analyzing its data
     * @param result OcrResult to analyze. Not null.
     * @return Ticket. Some fields can be null;
     */
    private static Ticket getTicketFromResult(OcrResult result) {
        Ticket ticket = new Ticket();
        List<RawGridResult> dateList = result.getDateList();
        ticket.amount = getPossibleAmount(result.getAmountResults());
        return ticket;
    }

    /**
     * @author Michelon
     * Search through results from the research of amount string and retrieves the text with highest
     * probability to contain the amount calculated with (probability from grid - distanceFromTarget*10).
     * If no amount was found in first result iterate through all results following previous ordering.
     * @param amountResults list of RawStringResult from amount search. Not null.
     * @return BigDecimal containing the amount found. Null if nothing found
     */
    private static BigDecimal getPossibleAmount(@NonNull List<RawStringResult> amountResults) {
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
                            OcrUtils.log(2, "getPossibleAmount", "Analyzing source text: " + sourceText.getDetection() +
                                    " where target is: " + rawText.getDetection() + " with probability: " + sourceText.getAmountProbability() +
                                    " and distance: " + stringResult.getDistanceFromTarget());
                        }
                    }
                }
            } else {
                OcrUtils.log(2, "getPossibleAmount", "Ignoring text: " + stringResult.getSourceText().getDetection());
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
    private static BigDecimal analyzeAmount(@Size(min = 1) String amountString) {
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
        targetAmount = targetAmount.replaceAll(",", ".");
        StringBuilder manipulatedAmount = new StringBuilder();
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
    private static boolean isExp(@Size(min = 1) String text, @IntRange(from = 0) int startingPoint) {
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
    private static String getExp(@Size(min = 1) String text, @IntRange(from = 0) int startingPoint) {
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
    private static int findDate(String text) {
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
    private static String getDate(String text) {
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
