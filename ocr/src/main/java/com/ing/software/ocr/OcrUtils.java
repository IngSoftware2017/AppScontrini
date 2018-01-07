package com.ing.software.ocr;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Log;

import com.google.android.gms.vision.text.TextBlock;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.ing.software.ocr.OcrVars.LOG_LEVEL;

/**
 * Util class to manage rects, blocks, images
 *
 */

public class OcrUtils {

    /**
     * @author Michelon
     * Crop image (values start from top left)
     * @param photo original photo not null. Not null.
     * @param startX x coordinate of top left point, int >= 0
     * @param startY y coordinate of top left point, int >= 0
     * @param endX x coordinate of bottom right point, int >= 0
     * @param endY y coordinate of bottom right point, int >= 0
     * @return cropped image, null if invalid coordinates
     */
    static Bitmap cropImage(@NonNull Bitmap photo, @IntRange(from = 0) int startX, @IntRange(from = 0) int startY, @IntRange(from = 0) int endX, @IntRange(from = 0) int endY) {
        log(4,"OcrUtils.cropImage","Received crop: left " + startX + " top: " + startY + " right: " + endX + " bottom: " + endY);
        if (endX < startX || endY < startY)
            return null;
        int width = Math.abs(endX - startX);
        int height = Math.abs(endY - startY);
        return Bitmap.createBitmap(photo, startX, startY, width, height);
    }

    /**
     * @author Michelon
     * Get rect containing all blocks detected (Temporary method)
     * Note: counting starts from left and from top
     * @param orderedTextBlocks blocks detected. Not null.
     * @param photo original photo. Not null.
     * @return array of int where int[0] = left border, int[1] = top border, int[2] = right border, int[3] = bottom border
     */
    @Deprecated
    static int[] getRectBorders(@NonNull List<TextBlock> orderedTextBlocks, @NonNull RawImage photo) {
        int numberOfBorders = 4; //it's a rect
        int[] borders = new int[numberOfBorders];
        //Extreme borders for chosen photo (will be overwritten in foreach)
        int left = photo.getWidth();
        int right = 0;
        int top = photo.getHeight();
        int bottom = 0;
        for (TextBlock textBlock : orderedTextBlocks) {
            RectF rectF = new RectF(textBlock.getBoundingBox());
            if (rectF.left<left)
                left = Math.round(rectF.left);
            if (rectF.right>right)
                right = Math.round(rectF.right);
            if (rectF.bottom>bottom)
                bottom = Math.round(rectF.bottom);
            if (rectF.top<top)
                top = Math.round(rectF.top);
            log(6,"OcrUtils.getRectBorder","Value: " + textBlock.getValue());
            log(6,"OcrUtils.getRectBorder","Temp rect: (left, top, right, bottom): " + rectF.left + "; " + rectF.top + "; " + rectF.right + "; " + rectF.bottom);
        }
        borders[0] = left;
        borders[1] = top;
        borders[2] = right;
        borders[3] = bottom;
        log(4,"OcrUtils.getRectBorder","New rect: (left, top, right, bottom): " + left + "; " + top + "; " + right + "; " + bottom);
        return borders;
    }

    /**
     * @author Michelon
     * Order a list of TextBlock from top to bottom, left to right
     * @param textBlocks original list. Not null.
     * @return ordered list
     */
    static List<TextBlock> orderTextBlocks(@NonNull List<TextBlock> textBlocks) {
        Collections.sort(textBlocks, new Comparator<TextBlock>() {
            @Override
            public int compare(TextBlock block1, TextBlock block2) {
                int diffTops = block1.getBoundingBox().top - block2.getBoundingBox().top;
                int diffLefts = block1.getBoundingBox().left - block2.getBoundingBox().left;
                int diffBottoms = block1.getBoundingBox().bottom - block2.getBoundingBox().bottom;
                int diffRights = block1.getBoundingBox().right - block2.getBoundingBox().right;
                if (diffTops != 0)
                    return diffTops;
                else if (diffLefts != 0)
                    return diffLefts;
                else if (diffBottoms != 0)
                    return diffBottoms;
                else
                    return diffRights;
            }
        });
        return textBlocks;
    }

    /**
     * @author Michelon
     * @date 18-12-2017
     * Order a list of rawText following its distance (of its center) from the center of another rect.
     * Order is based only on y coordinate.
     * @param rawTexts original list. Not null.
     * @param sourceRect Rect from which check the distance
     * @return ordered list
     */
    static List<RawText> orderRawTextFromRect(@NonNull List<RawText> rawTexts, final Rect sourceRect) {
        Collections.sort(rawTexts, new Comparator<RawText>() {
            @Override
            public int compare(RawText text1, RawText text2) {
                int centerPoint = sourceRect.centerY();
                int center1 = text1.getBoundingBox().centerY();
                int center2 = text2.getBoundingBox().centerY();
                int diff1 = Math.abs(center1 - centerPoint);
                int diff2 = Math.abs(center2 - centerPoint);
                if (diff1 - diff2 == 0) {
                    //same distance from center, return one on top
                    return center1 - center2;
				}
                return diff1 - diff2;
            }
        });
        return rawTexts;
    }

    /**
     * @author Michelon
     * Extends the width of a rect to the max allowed for chosen photo
     * @param rect source rect. Not null.
     * @param photo source rawImage (to get max width). Not null.
     * @return rect with max width
     */
    static Rect extendWidthFromPhoto(@NonNull Rect rect, @NonNull RawImage photo) {
        int top = rect.top;
        int bottom = rect.bottom;
        int left = 0;
        int right = photo.getWidth();
        Rect rectF = new Rect(left, top, right, bottom);
        log(6,"OcrUtils.getExtendRect","Extended rect: left " + rectF.left + " top: "
                + rectF.top + " right: " + rectF.right + " bottom: " + rectF.bottom);
        return rectF;
    }

    /**
     * @author Michelon
     * Logs messages only if debug is enabled
     * @param level int >= 0, only messages with level <= OcrVars.LOG_LEVEL are logged
     * @param tag tag of the message to log, must be less than 24 chars long
     * @param message message to log
     */
    public static void log(@IntRange(from = 0) int level, @Size(max = 23) String tag, String message) {
        if (OcrVars.IS_DEBUG_ENABLED && level <= LOG_LEVEL)
            Log.d(tag, message);
    }


    /**
     * @author Salvagno
     * Returns the maximum between |i| e |j|
     *
     * @param i The first integer not null to be compared
     * @param j The second integer not null to be compared
     * @return maximum number
     */
    private static int maxLengthStrings(int i, int j)
    {
        return (i>j ? i : j);
    }

    /**
     * @author Salvagno
     * Returns the minimum between |i|, |j| e |k|
     *
     * @param i The first integer not null to be compared
     * @param j The second integer not null to be compared
     * @param k The third integer not null to be compared
     * @return minimum number
     */
    private static int minLengthStrings(int i, int j, int k)
    {
        int result = i;
        if (j < result) result = j;
        if (k < result) result = k;
        return result;
    }

    /**
     * @author Salvagno
     * Returns the distance of Levenshtein between two strings | S | e | T |.
     * The Levenshtein distance is a string metric for measuring the difference between two sequences.
     * Informally, the Levenshtein distance between two words is the minimum number of single-character
     * edits (insertions, deletions or substitutions) required to change one word into the other.
     * The distance is an integer between 0 and the maximum length of the two strings.
     * If only one string is null then return -1
     *
     * @param S The first string to be compared
     * @param T The second string to be compared
     * @return distance between two strings
     */
    static int levDistance(String S, String T)
    {
        if(S == null || T == null)
            return -1;

        int i, j;
        final int n = S.length(), m = T.length();
        int L[][] = new int[n+1][m+1];
        for ( i=0; i<n+1; i++ ) {
            for ( j=0; j<m+1; j++ ) {
                if ( i==0 || j==0 ) {
                    L[i][j] = maxLengthStrings(i, j);
                } else {
                    L[i][j] = minLengthStrings(L[i-1][j] + 1, L[i][j-1] + 1,
                            L[i-1][j-1] + (S.charAt(i-1) != T.charAt(j-1) ? 1 : 0) );
                }
            }
        }

        return L[n][m];
    }

    /**
     * @author Salvagno
     * Returns the distance of Levenshtein between two strings | S | e | T |.
     * The Levenshtein distance is a string metric for measuring the difference between two sequences.
     * Informally, the Levenshtein distance between two words is the minimum number of single-character
     * edits (insertions, deletions or substitutions) required to change one word into the other.
     * The distance is an integer between 0 and the maximum length of the two strings.
     * If only one string is null then return -1
     * If the distance exceeds the maximum input distance, the function stops and returns the maximum value
     *
     * @param S The first string to be compared
     * @param T The second string to be compared
     * @param max maximum distance accepted
     * @return distance between two strings or the maximum value if it has been exceeded
     */
    static int levDistance(String S, String T, Integer max)
    {
        if(S == null || T == null)
            return -1;

        int i, j;
        final int n = S.length(), m = T.length();
        int L[][] = new int[n+1][m+1];
        for ( i=0; i<n+1; i++ ) {
            for ( j=0; j<m+1; j++ ) {
                if ( i==0 || j==0 ) {
                    L[i][j] = maxLengthStrings(i, j);
                } else {
                    L[i][j] = minLengthStrings(L[i-1][j] + 1, L[i][j-1] + 1,
                            L[i-1][j-1] + (S.charAt(i-1) != T.charAt(j-1) ? 1 : 0) );
                }

                if(L[n][m] > max)
                {
                    return max;
                }


            }

        }

        return L[n][m];
    }

    /**
     * @author Salvagno
     * Check if there is a substring in the text
     * The text is subdivided into tokens and each token is checked
     * If only one string is null then return -1
     * If the token length is less than 2 then returns -1
     *
     * @param text The text to be compared
     * @param substring The second string to be compared
     * @return the slightest difference between strings and text
     */
    public static int findSubstring(String text, String substring)
    {
        if(text == null || substring == null || text.length() == 0)
            return -1;
        int minDistance = substring.length();
        int subLength = minDistance;

        //Analyze the text by removing the spaces
        String text_w_o_space =  text.replace(" ", "");
        //If the text is smaller than the searched string, invert the strings
        if(text_w_o_space.length() < minDistance)
        {
            String temp_text = text_w_o_space;
            text_w_o_space = substring;
            substring = temp_text;
        }

        //Search a piece of string as long as the length of the searched string in the text
        int start=0;
        for (int finish = subLength; finish<=(text_w_o_space.length()); finish++) {
            String token = text_w_o_space.substring( start, finish);
            int distanceNow = levDistance(token.toUpperCase(), substring.toUpperCase());
            if (distanceNow < minDistance)
                minDistance = distanceNow;
            //Lucky case
            if(distanceNow == 0)
                break;
            start++;
        }

        return minDistance;
    }


    /**
     * @author Salvagno
     * Check if there is a substring in the text
     * The text is subdivided into tokens and each token is checked
     * If only one string is null then return -1
     * If the token length is less than 2 then returns -1
     * If the distance exceeds the maximum input distance, returns the maximum value
     *
     * @param text The text to be compared
     * @param substring The second string to be compared
     * @param max maximum distance accepted
     * @return the slightest difference between strings and text
     */
    public static int findSubstring(String text, String substring, Integer max)
    {
        if(text == null || substring == null || text.length() == 0)
            return -1;

        int minDistance = substring.length();
        int subLength = minDistance;

        //Analyze the text by removing the spaces
        String text_w_o_space =  text.replace(" ", "");
        //If the text is smaller than the searched string, invert the strings
        if(text_w_o_space.length() < minDistance)
        {
            String temp_text = text_w_o_space;
            text_w_o_space = substring;
            substring = temp_text;
        }

        //Search a piece of string as long as the length of the searched string in the text
        int start=0;
        for (int finish = subLength; finish<=(text_w_o_space.length()); finish++) {
            String token = text_w_o_space.substring( start, finish);
            int distanceNow = levDistance(token.toUpperCase(), substring.toUpperCase(), max);
            if (distanceNow < minDistance)
                minDistance = distanceNow;
            //Lucky case
            if(distanceNow == 0)
                break;
            //maximum value accepted
            if(distanceNow >= max)
                break;

            start++;
        }

        return minDistance;
    }



    /**
     * @author Michelon
     * Create a new rect extending source rect with chosen percentage (on width and height of chosen rect)
     * Or extending of chosen pixels on both sides (if param is negative)
     * Note: Min value for top and left is 0
     * @param rect source rect. Not null
     * @param percentHeight chosen percentage for height. \pixels if negative
     * @param percentWidth chosen percentage for width. \pixels if negative
     * @return new extended rectangle
     */
    static Rect extendRect(@NonNull Rect rect, int percentHeight, int percentWidth) {
        int top;
        int bottom;
        int right;
        int left;
        if (percentHeight > 0) {
            int extendedHeight = rect.height() * percentHeight / 100;
            top = rect.top - extendedHeight / 2;
            bottom = rect.bottom + extendedHeight/2;
        } else {
            top = rect.top - Math.abs(percentHeight);
            bottom = rect.bottom + Math.abs(percentHeight);
        }
        if (percentWidth > 0) {
            int extendedWidth = rect.width() * percentWidth / 100;
            left = rect.left - extendedWidth / 2;
            right = rect.right + extendedWidth/2;
        } else {
            left = rect.left - Math.abs(percentWidth);
            right = rect.right + Math.abs(percentWidth);
        }
        if (left<0)
            left = 0;
        if (top < 0)
            top = 0;
        //Doesn't matter if bottom and right are outside the photo
        return new Rect(left, top, right, bottom);
    }

    /**
     * @author Michelon
     * @date 26-12-17
     * Check if a string may be a number.
     * Removes spaces, 'S', 'O','o', '.', ',' before analysis.
     * If string is longer than maxlength default is Integer.MAX_VALUE (allowed numbers up to nn.nnn,nn)
     * return is decreased if one '.' or ',' is present, increased if more than one are present.
     * @param s string to analyze
     * @return Integer.MAX_VALUE if less than 2/3 of the string are not numbers; number of non-digit chars (*0.5 if special (see above))/length
	 */
    static double isPossiblePriceNumber(String s) {
        int counter = 0;
        s = s.replaceAll(" ", "");
        int initialLength = s.length();
        int reducedLength = 0;
        if (s.contains(".") || s.contains(","))
            ++reducedLength;
        int maxLength = 8; //Assume we can't have prices longer than 8 chars (so nn.nnn,nn)
        s = s.replaceAll(",", "").replaceAll("O", "") //sometimes '0' are recognized as 'O'
                .replaceAll("o", "").replaceAll("\\.", "").replaceAll("S", ""); //sometimes '5' are recognized as 'S'
        if (s.length() >= maxLength)
            return Integer.MAX_VALUE;
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isDigit(s.charAt(i)))
                ++counter;
        }
        reducedLength += s.length();
        //return counter >= Math.round((double)s.length()*3/4);
        if (counter < Math.round((double)s.length()*2/3))
            return Integer.MAX_VALUE;
        return ((initialLength - reducedLength)*0.5 + s.length() - counter)/reducedLength;
    }

    /**
     * @author Michelon
     * Get rect containing all rawTexts detected
     * @param rawTexts texts detected. Not null.
     * @return Rect containing all rects passed
     */
    public static Rect getMaxRectBorders(@NonNull List<RawText> rawTexts) {
        //Extreme borders for chosen photo (will be overwritten in foreach)
        if (rawTexts.size() == 0)
            return null;
        int left = rawTexts.get(0).getRawImage().getWidth();
        int right = 0;
        int top = rawTexts.get(0).getRawImage().getHeight();
        int bottom = 0;
        for (RawText rawText : rawTexts) {
            if (rawText.getBoundingBox().left<left)
                left = rawText.getBoundingBox().left;
            if (rawText.getBoundingBox().right>right)
                right = rawText.getBoundingBox().right;
            if (rawText.getBoundingBox().bottom>bottom)
                bottom = rawText.getBoundingBox().bottom;
            if (rawText.getBoundingBox().top<top)
                top = rawText.getBoundingBox().top;
        }
        return new Rect(left, top, right, bottom);
    }
}
