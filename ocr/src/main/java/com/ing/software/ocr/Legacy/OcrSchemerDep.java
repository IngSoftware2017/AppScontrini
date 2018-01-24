package com.ing.software.ocr.Legacy;

import android.graphics.Rect;

import com.ing.software.ocr.OperativeObjects.RawImage;
import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Deprecated methods from OcrSchemer
 */

public class OcrSchemerDep {

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
                Rect extendedRect = OcrUtils.extendWidthFromPhoto(block.getRectF(), rawImage);
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
    @Deprecated
    static List<RawText> getPricesTexts(List<RawText> blocks) {
        List<RawText> texts = new ArrayList<>();
        for (RawText text : blocks) {
            if (text.getBoundingBox().centerX() > text.getRawImage().getWidth()*0.75)
                texts.add(text);
        }
        return texts;
    }

    /**
     * Get texts that are on the right part (1/2) of the receipt
     * @param blocks list of texts
     * @return list of texts on right side
     */
    @Deprecated
    static List<RawText> findTextsOnRight(List<RawText> blocks) {
        List<RawText> texts = new ArrayList<>();
        for (RawText text : blocks) {
            if (text.getBoundingBox().centerX() > text.getRawImage().getWidth()/2)
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
    @Deprecated
    static boolean isPossibleCash(RawText amount, RawText cash) {
        int extendWidth = 50;
        Rect extendedRect = OcrUtils.extendRect(amount.getBoundingBox(), -amount.getRawImage().getHeight(), extendWidth);
        extendedRect.set(extendedRect.left, amount.getBoundingBox().top, extendedRect.right, extendedRect.bottom);
        return extendedRect.contains(cash.getBoundingBox());
    }


}
