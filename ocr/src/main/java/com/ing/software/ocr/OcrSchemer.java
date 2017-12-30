package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ing.software.ocr.OcrVars.*;

/**
 * Manages a receipt trying to find its elements (products, blocks etc)
 * WIP
 */

class OcrSchemer {

    /**
     * Find all blocks with a corresponding block on its left or right.
     * @param blocks list of blocks
     * @return list of blocks with a block on its left or right
     */
    @Deprecated
    static List<RawBlock> findBlocksOnLeft(List<RawBlock> blocks) {
        List<RawBlock> candidates = new ArrayList<>();
        if (blocks.size() > 0) {
            RawImage rawImage = blocks.get(0).getRawImage();
            for (RawBlock block : blocks) {
                RectF extendedRect = OcrUtils.extendWidthFromPhoto(block.getRectF(), rawImage);
                for (RawBlock block1 : blocks) {
                    if (block1 != block && extendedRect.contains(block1.getRectF())) {
                        candidates.add(block);
                        candidates.add(block1);
                        break;
                    }
                }
            }
        }
        return candidates;
    }

    /**
     * Get texts that are on the right part (3/4) of the receipt
     * @param blocks list of texts
     * @return list of texts on right side
     */
    static List<RawText> getPricesTexts(List<RawText> blocks) {
        List<RawText> texts = new ArrayList<>();
        for (RawText text : blocks) {
            if (text.getRect().centerX() > text.getRawImage().getWidth()*0.75)
                texts.add(text);
        }
        return texts;
    }

    /**
     * Get texts that are on the right part (1/2) of the receipt
     * @param blocks list of texts
     * @return list of texts on right side
     */
    static List<RawText> findTextsOnRight(List<RawText> blocks) {
        List<RawText> texts = new ArrayList<>();
        for (RawText text : blocks) {
            if (text.getRect().centerX() > text.getRawImage().getWidth()/2)
                texts.add(text);
        }
        return texts;
    }

    /**
     * Return true if cash rect is inside extended (on height) amount rect
     * @param amount source rect of amount
     * @param cash amount of possible cash
     * @return true if inside
     */
    static boolean isPossibleCash(RawText amount, RawText cash) {
        int extendHeight = 400;
        int extendWidth = 50;
        RectF extendedRect = OcrUtils.extendRect(amount.getRect(), extendHeight, extendWidth);
        extendedRect.set(extendedRect.left, amount.getRect().top, extendedRect.right, extendedRect.bottom);
        return extendedRect.contains(cash.getRect());
    }

    /**
     * Analizza una lista di RawTexts e associa a ognuno un tag per indicare se è intestazione,
     * lista prodotti, lista prezzi, conclusione. Associa un double che rappresenta la sua posizione (in n/10)
     * all'interno del proprio blocco. Ad esempio se ho k prodotti, il primo inizia (media) in y=10, l'ultimo finisce in
     * y = 50, un prodotto con media 20 avrà posizione 20*10/(50-10)=5.
     */
    static void prepareScheme(List<RawText> textList) {
        for (RawText rawText : textList) {
            switch (getPosition(rawText)) {
                case 0: rawText.addTag(LEFT_TAG);
                OcrUtils.log(2, "prepareScheme", "Text: "+rawText.getDetection() + " is left");
                    break;
                case 1: rawText.addTag(CENTER_TAG);
                    if (isHalfUp(rawText)) {
                        rawText.addTag(INTRODUCTION);
                        OcrUtils.log(2, "prepareScheme", "Text: " + rawText.getDetection() + " is center-introduction");
                    } else {
                        rawText.addTag(CONCLUSION);
                        OcrUtils.log(2, "prepareScheme", "Text: "+rawText.getDetection() + " is center-conclusion");
                    }
                    waveTagger(textList, rawText);
                    break;
                case 2: rawText.addTag(RIGHT_TAG);
                    OcrUtils.log(2, "prepareScheme", "Text: "+rawText.getDetection() + " is right");
                    break;
                default: OcrUtils.log(1, "prepareScheme", "Could not find position for text: " + rawText.getDetection());
            }
        }
        missTagger(textList);
        densityIntroductionSnake(textList);
    }

    /**
     * Indica dov'è il centro del text in coordinata x, dividendo lo scontrino in 3
     * @param text target text
     * @return 0,1,2 according to position
     */
    static private int getPosition(RawText text) {
        int width = text.getRawImage().getWidth();
        return (int)(double)text.getRect().centerX()*3/width;
    }

    /**
     * True se lo scontrino è nella prima metà
     */
    private static boolean isHalfUp(RawText text) {
        int height = text.getRawImage().getHeight();
        return text.getRect().centerY() < height;
    }

    /**
     * Cataloga i text, se sono alla stessa altezza di un introduction o conclusion, taggali
     * di conseguenza, altrimenti considerali products\prices
     */
    private static void waveTagger(List<RawText> texts, RawText source) {
        int extendHeight = 20;
        RectF extendedRect = OcrUtils.extendRect(source.getRect(), extendHeight, -source.getRawImage().getWidth());
        for (RawText text : texts) {
            if (!text.getTags().contains(CENTER_TAG)) { //here we are excluding source
                if (extendedRect.contains(text.getRect())) {
                    if (source.getTags().contains(INTRODUCTION)) {
                        text.addTag(INTRODUCTION);
                        OcrUtils.log(2, "waveTagger", "Text: " + text.getDetection() + " is introduction");
                    } else {
                        text.addTag(CONCLUSION);
                        OcrUtils.log(2, "waveTagger", "Text: " + text.getDetection() + " is conclusion");
                    }
                }
            }
        }
    }

    /**
     * Tagga i rawtext ancora vuoti
     * @param texts
     */
    private static void missTagger(List<RawText> texts) {
        for (RawText text : texts) {
            if (text.getTags().size() < 2 && text.getTags().get(0).equals(LEFT_TAG)) {
                text.addTag(PRODUCTS);
                OcrUtils.log(2, "waveTagger", "Text: "+text.getDetection() + " is possible product");
            } else if (text.getTags().size() < 2) {
                text.addTag(PRICES);
                OcrUtils.log(2, "waveTagger", "Text: "+text.getDetection() + " is possible price");
            }
        }

    }

    /**
     * Get index of rect at witch you want to stop introduction block
     * @param textList list of rawTexts
     * @return -1 if list is empty or there is no introduction block (too low density), else index of last block of introduction
     */
    private static int densityIntroductionSnake(List<RawText> textList) {
        if (textList.size() == 0)
            return -1;
        Collections.sort(textList);
        double targetArea = 0;
        double totalArea = 0;
        SortedMap<Double, Integer> map = new TreeMap<>(); //Key is the density, Value is the position, the bigger the area for same density, the better it is
        for (int i = 0; i < textList.size(); ++i) {
            RawText text = textList.get(i);
            if (text.getTags().contains(INTRODUCTION)) {
                targetArea += getRectArea(text.getRect());
            }
            totalArea += getRectArea(text.getRect());
            OcrUtils.log(1, "densityIntrSnake", "Text: " + text.getDetection() + "\ncurrent density is: "
                + targetArea/totalArea);
            map.put(targetArea/totalArea, i); // if we have same density but bigger area, let's choose biggest area
        }
        //here We have a list of ordered density and area, to choose the best we define a variable (needs to be tuned)
        // N = {(area of 1 rect)*[(n° of rect)-(n° of rect%15)]/(total area)}
        int limitText = Math.round(textList.size()/15);
        if (limitText == 0)
            limitText = 1;
        double limit = ((totalArea/textList.size())*(textList.size() - limitText)/totalArea);
        OcrUtils.log(1, "densityIntrSnake", "Limit density is: " + limit);
        double targetDensity = -1;
        for (Double currentDensity : map.keySet()) {
            OcrUtils.log(1, "densityIntrSnake", "Possible density is: " + currentDensity);
            if (currentDensity > limit) {
                targetDensity = currentDensity;
                break;
            }
        }
        //Now, we may have included some wrong texts (suppose we have all blocks introduction, followed by all products,
        // here we include also the first few products), so we better reduce area to the first introduction rect
        if (targetDensity == -1)
            return -1; //No introduction block found, or density too low
        int introductionPos = map.get(targetDensity);
        for (int i = introductionPos; i > 0; --i) {
            if (textList.get(i).getTags().contains(INTRODUCTION)) {
                introductionPos = i;
                break;
            }
        }
        OcrUtils.log(1, "densityIntrSnake", "Final target text is: " + introductionPos
            + "\nWith text: " + textList.get(introductionPos).getDetection());
        //Now introductionPos is the value of the last rect for introduction
        return introductionPos;
    }

    private static double getRectArea(RectF rect) {
        return rect.width() * rect.height();
    }
}
