package com.ing.software.ocr.OperativeObjects;

import java.util.*;
import com.annimon.stream.Stream;
import com.ing.software.ocr.OcrObjects.OcrText;

import static java.util.Collections.*;
import static java.lang.Math.*;

/**
 * The scope of this object is to apply the Levenshtein distance algorithm to a subset of regex.
 */
public class WordMatcher {

   //this compiled regex format represent a string where in each character position, multiple chars are accepted
    private List<List<Character>> compiledRegex;
    private int maxDist;

    /**
     * Create a WordMatcher. The regex must match only alphabetic characters.
     * @param regex regular expression (supports only square bracket operator).
     * @param maxLevDist maximum levenshtein distance
     * @author Riccardo Zaglia
     */
    public WordMatcher(String regex, int maxLevDist) {
        compiledRegex = compile(regex);
        maxDist = maxLevDist;
    }

    /**
     * Get a score of how well the input line matches with the previously passed regex.
     * @param line OcrText to match
     * @return value in range [0, maxScore]. maxScore is the value passed in the constructor.
     *         0 means that there was no match.
     * @author Riccardo Zaglia
     */
    public double match(OcrText line) {
        //scanning each word, find the least levenshtein distance of regex to the current word
        int wordsLeastLoss = min(Stream.of(line.children())
                .map(w -> levDistance(compiledRegex, w.textUppercase(), maxDist)).toList());
        //scan also the whole line without spaces and return the least loss.
        //sometimes, if the characters are spread horizontally, a word can be split in multiple words.
        int finalLoss = min(wordsLeastLoss, levDistance(compiledRegex, line.textNoSpaces(), maxDist));
        // the final score is the length of a word that perfectly matches regex minus the least loss.
        return max(compiledRegex.size() - finalLoss, 0);
    }

    /**
     * @author Salvagno
     * @author EDIT: Riccardo Zaglia
     * Returns the distance of Levenshtein between a compiled regex and a target string.
     * The Levenshtein distance is a string metric for measuring the difference between two sequences.
     * Informally, the Levenshtein distance between two words is the minimum number of single-character
     * edits (insertions, deletions or substitutions) required to change one word into the other.
     * If the distance exceeds the maximum input distance, the function stops and returns Integer.MAX_VALUE.
     *
     * @param compRegex compiled regex expression list
     * @param target Target string from which to calculate the distance from regex
     * @param max maximum distance accepted
     * @return distance of target from regex or Integer.MAX_VALUE if the maximum value has been exceeded
     */
    //todo: find a criteria to stop after hitting max distance
    private static int levDistance(List<List<Character>> compRegex, String target, int max)
    {
        int i, j;
        final int n = compRegex.size(), m = target.length();
        int L[][] = new int[n+1][m+1];
        for ( i=0; i<n+1; i++ ) {
            for ( j=0; j<m+1; j++ ) {
                if ( i==0 || j==0 ) {
                    L[i][j] = max(i, j);
                } else {
                    L[i][j] = min(min(L[i-1][j] + 1, L[i][j-1] + 1),
                            L[i-1][j-1] + (compRegex.get(i-1).contains(target.charAt(j-1)) ? 0 : 1));
                }
            }
        }

        if(L[n][m] > max)
        {
            return Integer.MAX_VALUE;
        }
        return L[n][m];
    }

    /**
     * Compile a regular expression (supports only square bracket operator and no escaping).
     * This algorithm does not catch any error in the regex, it's beyond of its scope.
     * @param regex regular expression
     * @return compiled list structure
     * @author Riccardo Zaglia
     */
    private static List<List<Character>> compile(String regex) {
        List<List<Character>> compiled = new ArrayList<>();
        boolean charListMode = false;
        List<Character> currentCharList = null;
        for (char c : regex.toCharArray()) {
            if (c == '[') {
                currentCharList = new ArrayList<>();
                charListMode = true;
            }
            else if (c == ']') {
                charListMode = false;
                compiled.add(currentCharList);
            }
            else {
                if (charListMode)
                    currentCharList.add(c);
                else
                    compiled.add(singletonList(c));
            }
        }
        return compiled;
    }
}
