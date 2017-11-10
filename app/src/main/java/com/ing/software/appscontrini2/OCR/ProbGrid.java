package com.ing.software.appscontrini2.OCR;


import java.util.HashMap;

/**
 * Static class containing grids for probability regions (WIP)
 */

public class ProbGrid {

    static final int GRIDCOUNT = 2;
    static final String ratio16x9 = "16x9";
    static final String ratio16x7 = "16x7";
    static HashMap<Double, String> gridMap = new HashMap(GRIDCOUNT);
    static HashMap<String, int[][]> amountMap = new HashMap(GRIDCOUNT);
    static final int[][] AmountGrid16x9 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {40, 50, 30, 10, 0, 0, 0, 0, 0},
            {60, 60, 50, 30, 10, 0, 0, 0, 0},
            {60, 70, 60, 40, 20, 0, 0, 0, 0},
            {40, 50, 30, 20, 0, 0, 0, 0, 0},
            {30, 40, 20, 10, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0}
    };
    static final int[][] AmountGrid16x7 = {
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {40, 50, 30, 10, 0, 0, 0},
            {60, 60, 50, 30, 10, 0, 0},
            {60, 70, 60, 40, 20, 0, 0},
            {40, 50, 30, 20, 0, 0, 0},
            {30, 40, 20, 10, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0}
    };

    static {
        gridMap.put(1.78, ratio16x9);
        gridMap.put(2.28, ratio16x7);
        amountMap.put(ratio16x9, AmountGrid16x9);
        amountMap.put(ratio16x7, AmountGrid16x7);
    }
}
