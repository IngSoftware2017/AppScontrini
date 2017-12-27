package com.ing.software.ocr;


import java.util.HashMap;

/**
 * Static class containing grids for probability regions (WIP)
 * Note: these grids are note definitive yet
 * More ratios are going to be added for next step
 * As soon as the OcrSchemer will be ready this class will be Deprecated
 * @author Michelon
 */
public class ProbGrid {

    private static final int GRIDCOUNT = 19; //number of grids
    /*
    private static final String RATIO19x16 = "19x16";
    private static final String RATIO18x16 = "18x16";
    private static final String RATIO17x16 = "17x16";
    private static final String RATIO16x16 = "16x16";
    private static final String RATIO15x16 = "15x16";
    private static final String RATIO14x16 = "14x16";
    private static final String RATIO13x16 = "13x16";
    private static final String RATIO12x16 = "12x16";
    private static final String RATIO11x16 = "11x16";
    private static final String RATIO10x16 = "10x16";
    private static final String RATIO9x16 = "9x16";
    private static final String RATIO8x16 = "8x16";
    private static final String RATIO7x16 = "7x16";
    private static final String RATIO6x16 = "6x16";
    private static final String RATIO5x16 = "5x16";
    private static final String RATIO4x16 = "4x16";
    private static final String RATIO3x16 = "3x16";
    private static final String RATIO2x16 = "2x16";
    private static final String RATIO1x16 = "1x16";
    */
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
        /*
        gridMap.put((double)19/16, RATIO19x16);
        gridMap.put((double)18/16, RATIO18x16);
        gridMap.put((double)17/16, RATIO17x16);
        gridMap.put((double)16/16, RATIO16x16);
        gridMap.put((double)15/16, RATIO15x16);
        gridMap.put((double)14/16, RATIO14x16);
        gridMap.put((double)13/16, RATIO13x16);
        gridMap.put((double)12/16, RATIO12x16);
        gridMap.put((double)11/16, RATIO11x16);
        gridMap.put((double)10/16, RATIO10x16);
        gridMap.put((double)9/16, RATIO9x16);
        gridMap.put((double)8/16, RATIO8x16);
        gridMap.put((double)7/16, RATIO7x16);
        gridMap.put((double)6/16, RATIO6x16);
        gridMap.put((double)5/16, RATIO5x16);
        gridMap.put((double)4/16, RATIO4x16);
        gridMap.put((double)3/16, RATIO3x16);
        gridMap.put((double)2/16, RATIO2x16);
        gridMap.put((double)1/16, RATIO1x16);
        amountMap.put(RATIO9x16, AmountGrid16x9);
        amountMap.put(RATIO7x16, AmountGrid16x7);
        dateMap.put(RATIO9x16, DateGrid16x9);
        dateMap.put(RATIO7x16, DateGrid16x7);
        */
        gridMap.put((double)16/9, RATIO16x9);
        gridMap.put((double)16/7, RATIO16x7);
        amountMap.put(RATIO16x9, AmountGrid16x9);
        amountMap.put(RATIO16x7, AmountGrid16x7);
        dateMap.put(RATIO16x9, DateGrid16x9);
        dateMap.put(RATIO16x7, DateGrid16x7);
    }
}
