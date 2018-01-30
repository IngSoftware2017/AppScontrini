package com.ing.software.ocr.OperativeObjects;

import android.util.Pair;

import com.ing.software.common.Scored;
import com.ing.software.ocr.DataAnalyzer;
import com.ing.software.ocr.OcrManager;
import com.ing.software.ocr.OcrObjects.TempText;
import com.ing.software.ocr.OcrObjects.TicketSchemes.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.annimon.stream.Stream;
import com.ing.software.ocr.OcrUtils;
import com.ing.software.ocr.ScoreFunc;

import static com.ing.software.ocr.OcrVars.NUMBER_MIN_VALUE;

/**
 * Class to analyze amount and compare it with a predefined ticket.
 */

public class AmountComparator {

    private List<Scored<Pair<BigDecimal, TicketScheme>>> bestAmounts = new ArrayList<>();
    private List<TicketScheme> acceptedSchemes = new ArrayList<>();

    /**
     * @author Michelon
     * Constructor, check amount against a defined scheme
     * @date 27-1-18
     * @param scheme scheme to use to check amount
     */
    public AmountComparator(TicketScheme scheme) {
        acceptedSchemes.add(scheme);
    }

    /**
     * @author Michelon
     * Constructor, check amount against a list of schemes
     * @date 27-1-18
     * @param schemes list of schemes to use to check amount
     */
    public AmountComparator(List<TicketScheme> schemes) {
        acceptedSchemes.addAll(schemes);
    }

    /**
     * Constructor, check amount against all schemes
     * @param amount BigDecimal containing decoded amount.
     */
    public AmountComparator(BigDecimal amount, TempText amountText) {
        acceptedSchemes = getAllSchemes(amount, getAboveTotalPrices(amountText), getBelowTotalPrices(amountText));
    }

    /**
     * Retrieves amount with highest score from ticket schemes
     * @param
     * @return amount with highest score, it's score and the ticket used. Null if no valid amount was found.
     */
    public Scored<Pair<BigDecimal, TicketScheme>> getBestAmount(boolean strict) {
        bestAmounts = Stream.of(acceptedSchemes)
                .filter(scheme -> scheme.getBestAmount(strict) != null)
                .map(scheme -> new Scored<>(scheme.getBestAmount(strict).getScore(), new Pair<>(scheme.getBestAmount(strict).obj(), scheme)))
                .toList();
        if (!bestAmounts.isEmpty()) {
            Collections.sort(bestAmounts, Collections.reverseOrder());
            return bestAmounts.get(0);
        } else
            return null;
    }

    /**
     * Analyze the list of products from OcrSchemer and convert accepted values as bigdecimal
     * @return list of bigdecimal of numbers above total
     */
    public List<BigDecimal> getAboveTotalPrices(TempText amountText) {
        return Stream.of(OcrManager.mainImage.getPricesTexts())
                .filter(price -> price.box().centerY() < amountText.box().centerY())
                .filter(price -> ScoreFunc.isPossiblePriceNumber(price.textNoSpaces(), price.numNoSpaces()) < NUMBER_MIN_VALUE)
                .map(price -> DataAnalyzer.analyzeAmount(price.numNoSpaces()))
                .withoutNulls()
                .toList();
    }

    /**
     * Analyze the list of products from OcrSchemer and convert accepted values as bigdecimal
     * @return list of bigdecimal of numbers below total
     */
    public List<BigDecimal> getBelowTotalPrices(TempText amountText) {
        return Stream.of(OcrManager.mainImage.getPricesTexts())
                .filter(price -> price.box().centerY() > amountText.box().centerY())
                .filter(price -> ScoreFunc.isPossiblePriceNumber(price.textNoSpaces(), price.numNoSpaces()) < NUMBER_MIN_VALUE)
                .map(price -> DataAnalyzer.analyzeAmount(price.numNoSpaces()))
                .withoutNulls()
                .toList();
    }

    /**
     * Initialize a list of all TicketSchemes
     * @param total bigdecimal with total value
     * @param aboveTotal list of prices above total
     * @param belowTotal list of prices below total
     * @return list of all ticketSchemes
     */
    private static List<TicketScheme> getAllSchemes(BigDecimal total, List<BigDecimal> aboveTotal, List<BigDecimal> belowTotal) {
        TicketScheme[] schemes = {
                new TicketSchemeIT_PC(total, aboveTotal, belowTotal),
                new TicketSchemeIT_PCC(total, aboveTotal, belowTotal),
                new TicketSchemeIT_PSC(total, aboveTotal, belowTotal),
                new TicketSchemeIT_PSCC(total, aboveTotal, belowTotal)
        };
        return Arrays.asList(schemes);
    }
}
