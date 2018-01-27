package com.ing.software.ocr.OperativeObjects;

import android.util.Pair;

import com.ing.software.common.Scored;
import com.ing.software.ocr.OcrObjects.TicketSchemes.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.annimon.stream.Stream;

/**
 * Class to analyze amount and compare it with a predefined ticket.
 */

public class AmountComparator {

    private BigDecimal amount;
    private List<Scored<Pair<BigDecimal, TicketScheme>>> bestAmounts = new ArrayList<>();
    private List<TicketScheme> acceptedSchemes = new ArrayList<>();

    /**
     * @author Michelon
     * Constructor, check amount against a defined scheme
     * @date 27-1-18
     * @param scheme
     * @param amount BigDecimal containing decoded amount.
     */
    public AmountComparator(TicketScheme scheme, BigDecimal amount) {
        this.amount = amount;
        acceptedSchemes.add(scheme);
    }

    /**
     * @author Michelon
     * Constructor, check amount against a list of schemes
     * @date 27-1-18
     * @param schemes
     * @param amount BigDecimal containing decoded amount.
     */
    public AmountComparator(List<TicketScheme> schemes, BigDecimal amount) {
        this.amount = amount;
        acceptedSchemes.addAll(schemes);
    }

    /**
     * Constructor, check amount against all schemes
     * @param amount
     */
    public AmountComparator(BigDecimal amount) {
        this.amount = amount;
        acceptedSchemes = getAllSchemes(amount, getAboveTotalPrices(), getBelowTotalPrices());
    }

    public Scored<Pair<BigDecimal, TicketScheme>> getBestAmount() {
        bestAmounts = Stream.of(acceptedSchemes)
                .map(scheme -> new Scored<>(scheme.getBestAmount().getScore(), new Pair<>(scheme.getBestAmount().obj(), scheme)))
                .toList();
        if (!bestAmounts.isEmpty()) {
            Collections.sort(bestAmounts, Collections.reverseOrder());
            return bestAmounts.get(0);
        } else
            return null;
    }

    private List<BigDecimal> getAboveTotalPrices() {
        return null;
    }

    private List<BigDecimal> getBelowTotalPrices() {
        return null;
    }

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
