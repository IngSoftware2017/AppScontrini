package com.ing.software.ocr;


import java.util.HashMap;

/**
 * Static class containing grids for probability regions (WIP)
 * Note: these grids are note definitive yet
 * More ratios are going to be added for next step
 * @author Michelon
 */
public class ProbGrid {

    private static final int GRIDCOUNT = 2; //number of grids
    private static final String RATIO16x9 = "16x9";
    private static final String RATIO16x7 = "16x7";
    static HashMap<Double, String> gridMap = new HashMap<>(GRIDCOUNT);
    public static HashMap<String, Integer[][]> amountMap = new HashMap<>(GRIDCOUNT);
    public static HashMap<String, Integer[][]> dateMap = new HashMap<>(GRIDCOUNT);
    private static final Integer[][] AmountGrid16x9 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {10, 10, 10, 0, 0, 0, 0, 0, 0},
            {20, 20, 10, 5, 0, 0, 0, 0, 0},
            {20, 20, 10, 5, 0, 0, 0, 0, 0},
            {20, 20, 10, 5, 0, 0, 0, 0, 0},
            {20, 20, 10, 5, 0, 0, 0, 0, 0},
            {30, 30, 20, 10, 0, 0, 0, 0, 0},
            {40, 50, 40, 10, 0, 0, 0, 0, 0},
            {40, 50, 40, 20, 0, 0, 0, 0, 0},
            {60, 70, 60, 40, 20, 0, 0, 0, 0},
            {60, 70, 60, 40, 10, 0, 0, 0, 0},
            {30, 40, 30, 10, 0, 0, 0, 0, 0},
            {10, 20, 20, 10, 0, 0, 0, 0, 0},
            {10, 5, 5, 0, 0, 0, 0, 0, 0}
    };
    private static final Integer[][] AmountGrid16x7 = {
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {10, 10, 10, 0, 0, 0, 0},
            {20, 20, 10, 0, 0, 0, 0},
            {20, 20, 10, 0, 0, 0, 0},
            {20, 20, 10, 0, 0, 0, 0},
            {20, 20, 10, 0, 0, 0, 0},
            {30, 30, 10, 0, 0, 0, 0},
            {40, 40, 10, 0, 0, 0, 0},
            {40, 40, 10, 0, 0, 0, 0},
            {70, 50, 10, 0, 0, 0, 0},
            {70, 50, 10, 0, 0, 0, 0},
            {40, 30, 20, 0, 0, 0, 0},
            {20, 20, 10, 0, 0, 0, 0},
            {10, 10, 0, 0, 0, 0, 0}
    };
    private static final Integer[][] DateGrid16x9 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {5, 5, 5, 5, 5, 5, 5, 5, 5},
            {5, 5, 5, 5, 5, 5, 5, 5, 5},
            {5, 5, 5, 5, 5, 5, 5, 5, 5},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {5, 5, 5, 5, 5, 5, 5, 5, 5},
            {10, 20, 30, 30, 30, 20, 10, 10, 5},
            {20, 40, 50, 40, 40, 40, 30, 20, 10},
            {20, 40, 50, 50, 40, 40, 30, 20, 10},
            {20, 40, 50, 40, 40, 30, 30, 20, 10},
            {10, 10, 10, 10, 10, 10, 10, 10, 10}
    };
    private static final Integer[][] DateGrid16x7 = {
            {0, 0, 0, 0, 0, 0, 0},
            {5, 5, 5, 5, 5, 5, 5},
            {5, 5, 5, 5, 5, 5, 5},
            {5, 5, 5, 5, 5, 5, 5},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {5, 5, 5, 5, 5, 5, 5},
            {10, 20, 30, 30, 30, 10, 5},
            {20, 40, 50, 40, 40, 20, 10},
            {20, 40, 50, 50, 40, 20, 10},
            {20, 40, 50, 40, 40, 20, 10},
            {10, 10, 10, 10, 10, 10, 10}
    };

    static {
        gridMap.put((double)16/9, RATIO16x9);
        gridMap.put((double)16/7, RATIO16x7);
        amountMap.put(RATIO16x9, AmountGrid16x9);
        amountMap.put(RATIO16x7, AmountGrid16x7);
        dateMap.put(RATIO16x9, DateGrid16x9);
        dateMap.put(RATIO16x7, DateGrid16x7);
    }
}
