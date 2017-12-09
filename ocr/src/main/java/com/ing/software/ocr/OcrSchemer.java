package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawText;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a receipt trying to find its elements (products, blocks etc)
 */

class OcrSchemer {

    /*
    Un prodotto deve stare a sinistra e avere un corrispondente blocco con i prezzi a destra:
    - Tieni tutti i blocchi con corrispondenze in un lato
     */
    static List<RawBlock> findBlocksOnLeft(List<RawBlock> blocks) {
        List<RawBlock> candidates = new ArrayList<>();
        if (blocks.size() > 0) {
            RawImage rawImage = blocks.get(0).getRawImage();
            for (RawBlock block : blocks) {
                RectF extendedRect = OcrUtils.getExtendedRect(block.getRectF(), rawImage);
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
     * Get texts that are on the right part of the receipt
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
     * Get texts that are on the right part of the receipt
     * @param blocks list of blocks containing texts
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
     * Return true if cash rect is inside extended amount rect
     * @param amount source rect of amount
     * @param cash amount of possible cash
     * @return true if inside
     */
    static boolean isPossibleCash(RawText amount, RawText cash) {
        RectF extendedRect = OcrUtils.partialExtendWidthRect(amount.getRect(), 50);
        extendedRect = OcrUtils.partialExtendHeightRect(extendedRect, 400);
        extendedRect = new RectF(extendedRect.left, amount.getRect().top, extendedRect.right, extendedRect.bottom);
        return extendedRect.contains(cash.getRect());
    }
}
