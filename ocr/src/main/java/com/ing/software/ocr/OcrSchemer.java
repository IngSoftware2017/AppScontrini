package com.ing.software.ocr;

import android.graphics.RectF;

import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Associa a uno scotrino uno schema contenente le disposizioni teoriche dei suoi elementi
 */

class OcrSchemer {

    private List<RawBlock> blocks = new ArrayList<>();
    private RawImage rawImage;

    /*
    Un prodotto deve stare a sinistra e avere un corrispondente blocco con i prezzi a destra:
    - Tieni tutti i blocchi con corrispondenze a destra
     */
    static List<RawBlock> findBlocksOnLeft(List<RawBlock> blocks) {
        RawImage rawImage = blocks.get(0).getRawImage();
        List<RawBlock> candidates = new ArrayList<>();
        for(RawBlock block : blocks) {
            RectF extendedRect = OcrUtils.getExtendedRect(block.getRectF(), rawImage);
            for (RawBlock block1 : blocks) {
                if (block1 != block && extendedRect.contains(block1.getRectF())) {
                    candidates.add(block);
                    break;
                }
            }
        }
        return candidates;
    }
}
