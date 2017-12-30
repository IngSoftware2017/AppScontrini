package com.ing.software.ocr;

import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.ing.software.ocr.OcrObjects.RawBlock;
import com.ing.software.ocr.OcrObjects.RawImage;
import com.ing.software.ocr.OcrObjects.RawTag;
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
        int extendWidth = 50;
        RectF extendedRect = OcrUtils.extendRect(amount.getRect(), -amount.getRawImage().getHeight(), extendWidth);
        extendedRect.set(extendedRect.left, amount.getRect().top, extendedRect.right, extendedRect.bottom);
        return extendedRect.contains(cash.getRect());
    }

    /**
     * Organize a list of rawTexts adding tags according to their position.
     * @param textList list of rawTexts to organize. Not null
     */
    static void prepareScheme(@NonNull List<RawText> textList) {
        for (RawText rawText : textList) {
            switch (getPosition(rawText)) {
                case 0: rawText.addTag(LEFT_TAG);
                OcrUtils.log(2, "prepareScheme", "Text: "+rawText.getDetection() + " is left");
                    break;
                case 1: rawText.addTag(CENTER_TAG);
                    if (isHalfUp(rawText)) {
                        rawText.addTag(INTRODUCTION_TAG);
                        OcrUtils.log(2, "prepareScheme", "Text: " + rawText.getDetection() + " is center-introduction");
                    } else {
                        rawText.addTag(CONCLUSION_TAG);
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
        Collections.sort(textList);
        int endingIntroduction = densitySnake(textList, INTRODUCTION_TAG);
        if (endingIntroduction != -1)
            intervalTagger(textList, INTRODUCTION_TAG, 0, endingIntroduction);
        else
            endingIntroduction = 0;
        List<RawText> reversedTextList = new ArrayList<>(textList);
        Collections.reverse(reversedTextList);
        int startingConclusion = densitySnake(reversedTextList, CONCLUSION_TAG);
        if (startingConclusion != -1) {
            startingConclusion = textList.size() - startingConclusion - 1;
            intervalTagger(textList, CONCLUSION_TAG, startingConclusion, textList.size() - 1);
        } else
            startingConclusion = textList.size() - 1;
        intervalTagger(textList, PRODUCTS_TAG, PRODUCTS_TAG, PRICES_TAG, endingIntroduction + 1, startingConclusion -1);
    }

    /**
     * Find column where center of rawText is, dividing the image in 3 parts
     * @param text target text. Not null.
     * @return 0,1,2 according to position
     */
    static private int getPosition(@NonNull RawText text) {
        int width = text.getRawImage().getWidth();
        return Math.round(text.getRect().centerX())*3/width;
    }

    /**
     * @param text source text. Not null.
     * @return True if text is in half up of the image (temporary method)
     */
    private static boolean isHalfUp(@NonNull RawText text) {
        int height = text.getRawImage().getHeight();
        return text.getRect().centerY() < height/2;
    }

    /**
     * Organize texts, if they are on the same level of a introduction\conclusion text, tag them accordingly
     * else consider them products or prices. (todo: simplify)
     * @param texts ordered list of RawTexts. Not null.
     * @param source source RawText. Not null.
     */
    private static void waveTagger(@NonNull List<RawText> texts, @NonNull RawText source) {
        int extendHeight = 20;
        RectF extendedRect = OcrUtils.extendRect(source.getRect(), extendHeight, -source.getRawImage().getWidth());
        for (RawText text : texts) {
            if (!text.getTags().contains(CENTER_TAG)) { //here we are excluding source
                if (extendedRect.contains(text.getRect())) {
                    if (source.getTags().contains(INTRODUCTION_TAG)) {
                        text.addTag(INTRODUCTION_TAG);
                        OcrUtils.log(2, "waveTagger", "Text: " + text.getDetection() + " is introduction");
                    } else {
                        text.addTag(CONCLUSION_TAG);
                        OcrUtils.log(2, "waveTagger", "Text: " + text.getDetection() + " is conclusion");
                    }
                }
            }
        }
    }

    /**
     * Tag rawTexts without a introduction\conclusion tag, as products\prices
     * @param texts list of rawTexts. Not null.
     */
    private static void missTagger(@NonNull List<RawText> texts) {
        for (RawText text : texts) {
            if (text.getTags().size() < 2 && text.getTags().get(0).equals(LEFT_TAG)) {
                text.addTag(PRODUCTS_TAG);
                OcrUtils.log(2, "waveTagger", "Text: "+text.getDetection() + " is possible product");
            } else if (text.getTags().size() < 2) {
                text.addTag(PRICES_TAG);
                OcrUtils.log(2, "waveTagger", "Text: "+text.getDetection() + " is possible price");
            }
        }

    }

    /**
     * Get index of rect at witch you want to stop tag 'block'
     * Method comments are an example of introduction tag
     * @param textList list of rawTexts. Not null.
     * @param tag tag for current block. Not null.
     * @return -1 if list is empty or there is no tag block (too low density), else index of last block of tag
     */
    private static int densitySnake(@NonNull List<RawText> textList, @NonNull String tag) {
        if (textList.size() == 0)
            return -1;
        double targetArea = 0;
        double totalArea = 0;
        SortedMap<Double, Integer> map = new TreeMap<>(); //Key is the density, Value is the position, the bigger the area for same density, the better it is
        for (int i = 0; i < textList.size(); ++i) {
            RawText text = textList.get(i);
            if (text.getTags().contains(tag)) {
                targetArea += getRectArea(text.getRect());
            }
            totalArea += getRectArea(text.getRect());
            OcrUtils.log(1, "densitySnake", "Text: " + text.getDetection() + "\ncurrent density is: "
                + targetArea/totalArea + "\nand tag: " + tag);
            map.put(targetArea/totalArea, i); // if we have same density but bigger area, let's choose biggest area
        }
        //here We have a list of ordered density and area, to choose the best we define a variable (needs to be tuned)
        // N = {(area of 1 rect)*[(n° of rect)-(n° of rect/15)]/(total area)}
        int limitText = Math.round(textList.size()/15);
        if (limitText == 0)
            limitText = 1;
        double limit = ((totalArea/textList.size())*(textList.size() - limitText)/totalArea);
        OcrUtils.log(1, "densitySnake", "Limit density is: " + limit);
        double targetDensity = -1;
        for (Double currentDensity : map.keySet()) {
            OcrUtils.log(1, "densitySnake", "Possible density is: " + currentDensity);
            if (currentDensity > limit) {
                targetDensity = currentDensity;
                break;
            }
        }
        //Now, we may have included some wrong texts (suppose we have all blocks introduction, followed by all products,
        // here we include also the first few products), so we better reduce area to the first introduction rect
        if (targetDensity == -1)
            return -1; //No introduction block found, or density too low
        int position = map.get(targetDensity);
        for (int i = position; i > 0; --i) {
            if (textList.get(i).getTags().contains(tag)) {
                position = i;
                break;
            }
        }
        OcrUtils.log(1, "densitySnake", "Final target text is: " + position
            + "\nWith text: " + textList.get(position).getDetection() + "\nand tag: " + tag);
        //Now position is the value of the last rect for introduction
        return position;
    }

    /**
     * Get area of a rect
     * @param rect A rect. Not null.
     * @return Area of the rect.
     */
    private static double getRectArea(@NonNull RectF rect) {
        return rect.width() * rect.height();
    }

    /**
     * Tag all texts in a interval with the same tag. Starting and ending indexes are included.
     * @param textList list of rawTexts. Not null.
     * @param tag new tag for rawTexts. Not null.
     * @param start index of first element. Int >= 0.
     * @param end index of last element. Int >= 0.
     */
    private static void intervalTagger(@NonNull List<RawText> textList, @NonNull String tag, @IntRange(from = 0) int start, @IntRange(from = 0) int end) {
        if (end < start)
            return;
        for (int i = start; i <= end; ++i) {
            RawText text = textList.get(i);
            removeOldTags(text);
            text.addTag(tag);
            text.setTagPosition(getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom));
            OcrUtils.log(1, "intervalTagger", "Text: " + text.getDetection() +
                    "\nis in position: " + getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom)
                    + "\nwith tag: " + tag);
        }
    }

    /**
     * Tag all texts in a interval with the same tag. Starting and ending indexes are included.
     * @param textList list of rawTexts. Not null.
     * @param tagLeft new tag for rawTexts on left. Not null.
     * @param tagCenter new tag for rawTexts on center. Not null.
     * @param tagRight new tag for rawTexts on right. Not null.
     * @param start index of first element. Int >= 0.
     * @param end index of last element. Int >= 0.
     */
    private static void intervalTagger(@NonNull List<RawText> textList, @NonNull String tagLeft, @NonNull String tagCenter,
                                       @NonNull String tagRight, @IntRange(from = 0) int start, @IntRange(from = 0) int end) {
        if (end < start)
            return;
        for (int i = start; i <= end; ++i) {
            RawText text = textList.get(i);
            removeOldTags(text);
            if (text.getTags().contains(LEFT_TAG)) {
                text.addTag(tagLeft);
                text.setTagPosition(getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom));
                OcrUtils.log(1, "intervalTagger", "Text: " + text.getDetection() +
                        "\nis in position: " + getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom)
                        + "\nwith tag: " + tagLeft);
            } else if (text.getTags().contains(RIGHT_TAG)) {
                text.addTag(tagRight);
                text.setTagPosition(getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom));
                OcrUtils.log(1, "intervalTagger", "Text: " + text.getDetection() +
                        "\nis in position: " + getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom)
                        + "\nwith tag: " + tagRight);
            } else {
                text.addTag(tagCenter);
                text.setTagPosition(getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom));
                OcrUtils.log(1, "intervalTagger", "Text: " + text.getDetection() +
                        "\nis in position: " + getTextBlockPosition(text, textList.get(start).getRect().top, textList.get(end).getRect().bottom)
                        + "\nwith tag: " + tagCenter);
            }
        }
    }

    /**
     * Remove all tags (excluded left-center-right) from a RawText
     * @param text source text. Not Null.
     */
    private static void removeOldTags(@NonNull RawText text) {
        text.removeTag(INTRODUCTION_TAG);
        text.removeTag(CONCLUSION_TAG);
        text.removeTag(PRODUCTS_TAG);
        text.removeTag(PRICES_TAG);
    }

    /**
     * Find position of a text inside its block with the formula: (text.centerY-start)/(end-start)
     * @param text source rawText. Not Null. Must be inside the block.
     * @param startPosition top of rect containing the whole block. >= 0.
     * @param endPosition bottom of rect containing the whole block. >= startPosition.
     * @return position as a 0 <= double <= 1.
     */
    private static double getTextBlockPosition(RawText text, float startPosition, float endPosition) {
        return ((double)(text.getRect().centerY() - startPosition))/(endPosition - startPosition);
    }
}
