package com.ing.software.ocr;


import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.TempText;
import com.ing.software.ocr.OperativeObjects.ListAmountOrganizer;
import com.ing.software.ocr.OperativeObjects.WordMatcher;

import java.util.List;
import com.annimon.stream.Stream;

import static com.ing.software.ocr.OcrVars.AMOUNT_MATCHERS;
import static java.util.Collections.max;

/**
 * Class used to extract information from raw data
 */
public class DataAnalyzer {

    /**
     * Get a list of texts where amount string is present
     * @param texts
     * @return
     */
    public static List<Scored<TempText>> getAmountTexts(List<TempText> texts) {
        List<Scored<TempText>> amounts = findAllMatchedStrings(texts, AMOUNT_MATCHERS);
        return amounts;
    }

    /**
     * Add detected amount texts in a list with source + target
     * @param texts
     * @return
     */
    public static List<ListAmountOrganizer> organizeAmountList(List<Scored<TempText>> texts) {
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
    private static List<Scored<TempText>> findAllMatchedStrings(List<TempText> lines, List<WordMatcher> matchers) {
        return Stream.of(lines)
                .map(line -> new Scored<>(max(Stream.of(matchers).map(m -> m.match(line)).toList()), line))
                .filter(s -> s.getScore() != 0).toList();
    }
}
