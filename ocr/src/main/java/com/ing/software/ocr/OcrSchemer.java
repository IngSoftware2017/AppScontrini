package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.ArrayList;
import java.util.List;

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
                    break;
                case 2: rawText.addTag(RIGHT_TAG);
                    OcrUtils.log(2, "prepareScheme", "Text: "+rawText.getDetection() + " is right");
                    break;
                default: OcrUtils.log(1, "prepareScheme", "Could not find position for text: " + rawText.getDetection());
            }
        }
        waveTagger(textList);
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
     * di conseguenza
     */
    private static void waveTagger(List<RawText> texts) {
        int extendHeight = 20;
        for (RawText text : texts) {
            if (!text.getTag().contains(CENTER_TAG)) {
                RectF extendedRect = OcrUtils.extendRect(text.getRect(), extendHeight, -text.getRawImage().getWidth());
                for (RawText text1 : texts) {
                    if (text1.getTag().contains(CENTER_TAG) && extendedRect.contains(text1.getRect())) {
                        if (text1.getTag().contains(INTRODUCTION)) {
                            text.addTag(INTRODUCTION);
                            OcrUtils.log(2, "waveTagger", "Text: " + text.getDetection() + " is introduction");
                        } else {
                            text.addTag(CONCLUSION);
                            OcrUtils.log(2, "waveTagger", "Text: " + text.getDetection() + " is conclusion");
                        }
                        break;
                    }
                }
            }
        }
        for (RawText text : texts) {
            if (text.getTag().getTag().size() < 2 && text.getTag().getTag().get(0).equals(LEFT_TAG)) {
                OcrUtils.log(2, "waveTagger", "Text: "+text.getDetection() + " is possible product");
            } else if (text.getTag().getTag().size() < 2) {
                OcrUtils.log(2, "waveTagger", "Text: "+text.getDetection() + " is possible price");
            }
        }
    }

}
