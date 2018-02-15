package com.ing.software.ocr.OperativeObjects;

import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.ing.software.ocr.OcrObjects.OcrText;
import com.ing.software.ocr.OcrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ing.software.ocr.OcrVars.*;

/**
 * @author Michelon
 * Manages a receipt trying to find its elements (introduction, products, prices, conclusion).
 * Only methods that don't change mainImage (rects included) are static, as it should be.
 */

class OcrSchemer {

    private static final int X_DIVIDER = 13; //divider for rect to choose left-center-right tag
    private static final int X_DIVIDER_LEFT = 4; //where to end left tags, must be <= X_DIVIDER_RIGTH
    private static final int X_DIVIDER_RIGHT = 9; //where to end center tag, must be <= X_DIVIDER
    private static final int AREA_DIVIDER = 4; //used to calculate density. (total_area/AREA_DIVIDER) is the area under the current rect to analyze
    private static final int WAVE_TAGGER_HEIGHT_EXTEND = 20; //percentage of the height of source rect to extend to include other rects
    private static final int DENSITY_SNAKE_MISS_RECTS = 15; //for density limit we accept 1/MISS_RECTS wrong rects
    private static final String LEFT_TAG = "left"; //tag for text on left of the receipt
    private static final String CENTER_TAG = "center"; //tag for text on center of the receipt
    private static final String RIGHT_TAG = "right"; //tag for text on right of the screen
    static final String INTRODUCTION_TAG = "introduction"; //tag for text on top of the receipt
    static final String PRODUCTS_TAG = "products"; //tag for text on central-left part of the receipt
    static final String PRICES_TAG = "prices"; //tag for text on central-right part of the receipt
    static final String CONCLUSION_TAG = "conclusion"; //tag for text on bottom of the receipt

    private RawImage mainImage;

    OcrSchemer(RawImage mainImage) {
        this.mainImage = mainImage;
    }

    /**
     * Organize a list of Texts adding tags according to their position.
     */
    void prepareScheme() {
        List<OcrText> textList = mainImage.getAllTexts();
        for (OcrText rawText : textList) {
            switch (getPosition(rawText, mainImage)) {
                case 0: rawText.addTag(LEFT_TAG);
                    OcrUtils.log(6, "prepareScheme", "Text: "+rawText.text() + " is left");
                    break;
                case 1: rawText.addTag(CENTER_TAG);
                    OcrUtils.log(6, "prepareScheme", "Text: "+rawText.text() + " is center");
                    break;
                case 2: rawText.addTag(RIGHT_TAG);
                    OcrUtils.log(6, "prepareScheme", "Text: "+rawText.text() + " is right");
                    break;
                default: OcrUtils.log(3, "prepareScheme", "Could not find position for text: " + rawText.text());
            }
        }
        float targetHeight = getBestCentralArea(textList, mainImage);
        centralTagger(textList, targetHeight);
        missTagger(textList);
        Collections.sort(textList);
        int endingIntroduction = densitySnake(textList, INTRODUCTION_TAG);
        if (endingIntroduction != -1)
            intervalTagger(textList, INTRODUCTION_TAG, 0, endingIntroduction);
        //else
        //   endingIntroduction = 0;
        List<OcrText> reversedTextList = new ArrayList<>(textList);
        Collections.reverse(reversedTextList);
        int startingConclusion = densitySnake(reversedTextList, CONCLUSION_TAG);
        if (startingConclusion != -1) {
            startingConclusion = textList.size() - startingConclusion - 1;
            intervalTagger(textList, CONCLUSION_TAG, startingConclusion, textList.size() - 1);
        } else
            startingConclusion = textList.size(); // -1?
        intervalTagger(textList, PRODUCTS_TAG, PRODUCTS_TAG, PRICES_TAG, endingIntroduction + 1, startingConclusion -1);
    }

    /**
     * Find column where center of rawText is, dividing the maximized rect in a defined number of parts parts
     * @param text target text. Not null.
     * @return 0,1,2 according to position (left, center, right)
     */
    private static int getPosition(@NonNull OcrText text, RawImage mainImage) {
        //int width = text.getRawImage().getWidth();
        RectF maxRect = mainImage.getExtendedRect();
        float width = maxRect.width();
        float position = (text.box().centerX() - maxRect.left)*X_DIVIDER/width;
        if (0<=position && position<=X_DIVIDER_LEFT)
            return 0;
        else if (position<=X_DIVIDER_RIGHT)
            return 1;
        else
            return 2;
        //return Math.round(text.getBoundingBox().centerX())*3/width;
    }

    /**
     * @param text source text. Not null.
     * @param targetHeight target height
     * @return True if text is above target height
     */
    private static boolean isHalfUp(@NonNull OcrText text, float targetHeight) {
        //int height = text.getRawImage().getHeight();
        //return text.getBoundingBox().centerY() < height/2;
        return text.box().centerY() < targetHeight;
    }

    /**
     * Tries to find the area of the ticket where there is the highest number of left/right
     * texts, to decide the "central" point of the ticket
     * @param texts list of all texts
     * @return y coordinate of central point
     */
    private static float getBestCentralArea(List<OcrText> texts, RawImage mainImage) {
        Collections.sort(texts);
        if (texts.size() == 0)
            return -1;
        RectF maximRect = mainImage.getExtendedRect();
        float targetHeight = mainImage.getExtendedRect().height()/AREA_DIVIDER;
        SortedMap<Double, Integer> map = new TreeMap<>();
        for (int i = 0; i < texts.size(); ++i) {
            OcrText rawText = texts.get(i);
            RectF areaRect = new RectF(maximRect.left, rawText.box().top, maximRect.right, rawText.box().top + targetHeight);
            if (areaRect.bottom > maximRect.bottom)
                break;
            double leftRightArea = 0;
            double targetArea = 0;
            for (OcrText text : texts) {
                if (areaRect.contains(text.box())) {
                    if (text.getTags().contains(LEFT_TAG) || text.getTags().contains(RIGHT_TAG)) {
                        leftRightArea += getRectArea(text.box());
                    }
                    targetArea += getRectArea(text.box());
                }
            }
            if (targetArea == 0) //should never happen
                targetArea = 1;
            OcrUtils.log(5, "getBestCentralArea", "Partial density for: " + rawText.text() + " is: " + leftRightArea / targetArea);
            map.put(leftRightArea / targetArea, i);
        }
        OcrText chosenRect = texts.get(map.get(map.lastKey()));
        OcrUtils.log(4, "getBestCentralArea", "Best center is at: " + (chosenRect.box().centerY())
            + " of rect " + chosenRect.text() + " and density: " + map.lastKey());
        return chosenRect.box().centerY();
    }

    /**
     * Organize texts, if they are on the same level of a introduction\conclusion text, tag them accordingly
     * else consider them products or prices.
     * @param texts ordered list of Texts. Not null.
     * @param source source Text. Not null.
     */
    private void waveTagger(@NonNull List<OcrText> texts, @NonNull OcrText source) {
        RectF extendedRect = OcrUtils.extendRect(source.box(), WAVE_TAGGER_HEIGHT_EXTEND, -mainImage.getWidth());
        for (OcrText text : texts) {
            if (!text.getTags().contains(CENTER_TAG)) { //here we are excluding source
                if (extendedRect.contains(text.box())) {
                    if (source.getTags().contains(INTRODUCTION_TAG)) {
                        text.addTag(INTRODUCTION_TAG);
                        OcrUtils.log(6, "waveTagger", "Text: " + text.text() + " is introduction");
                    } else {
                        text.addTag(CONCLUSION_TAG);
                        OcrUtils.log(6, "waveTagger", "Text: " + text.text() + " is conclusion");
                    }
                }
            }
        }
    }

    /**
     * Tags central texts according to position
     * @param texts ordered list of Texts. Not null.
     * @param targetHeight center (y coordinate) of the ticket
     */
    private void centralTagger(@NonNull List<OcrText> texts, float targetHeight) {
        for (OcrText rawText : texts) {
            if (rawText.getTags().contains(CENTER_TAG)) { //here we are excluding source
                if (isHalfUp(rawText, targetHeight)) {
                    rawText.addTag(INTRODUCTION_TAG);
                    OcrUtils.log(6, "centralTagger", "Text: " + rawText.text() + " is center-introduction");
                } else {
                    rawText.addTag(CONCLUSION_TAG);
                    OcrUtils.log(6, "centralTagger", "Text: "+rawText.text() + " is center-conclusion");
                }
                waveTagger(texts, rawText);
            }
        }
    }

    /**
     * Tag rawTexts without a introduction\conclusion tag, as products\prices
     * @param texts list of Texts. Not null.
     */
    private void missTagger(@NonNull List<OcrText> texts) {
        for (OcrText text : texts) {
            if (!text.getTags().contains(INTRODUCTION_TAG) && !text.getTags().contains(CONCLUSION_TAG)) {
                if (text.getTags().contains(LEFT_TAG)) {
                    text.addTag(PRODUCTS_TAG);
                    OcrUtils.log(6, "missTagger", "Text: " + text.text() + " is possible product");
                } else {
                    text.addTag(PRICES_TAG);
                    OcrUtils.log(6, "missTagger", "Text: " + text.text() + " is possible price");
                }
            }
        }
    }

    /**
     * Get index of rect at which you want to stop tag 'block'
     * Method comments are an example of introduction tag
     * @param textList list of Texts. Not null.
     * @param tag tag for current block. Not null.
     * @return -1 if list is empty or there is no tag block (too low density), else index of last block of tag
     */
    private static int densitySnake(@NonNull List<OcrText> textList, @NonNull String tag) {
        if (textList.size() == 0)
            return -1;
        double targetArea = 0;
        double totalArea = 0;
        SortedMap<Double, Integer> map = new TreeMap<>(); //Key is the density, Value is the position, the bigger the area for same density, the better it is
        for (int i = 0; i < textList.size(); ++i) {
            OcrText text = textList.get(i);
            if (text.getTags().contains(tag)) {
                targetArea += getRectArea(text.box());
            }
            totalArea += getRectArea(text.box());
            OcrUtils.log(7, "densitySnake", "Text: " + text.text() + "\ncurrent density is: "
                + targetArea/totalArea + "\nand tag: " + tag);
            map.put(targetArea/totalArea, i); // if we have same density but bigger area, choose biggest area
        }
        //here We have a list of ordered density and area, to choose the best we define a variable
        // N = {(area of 1 rect)*[(n° of rect)-(n° of rect/DENSITY_SNAKE_MISS_RECTS)]/(total area)}
        int limitText = Math.round(textList.size()/DENSITY_SNAKE_MISS_RECTS);
        if (limitText == 0)
            limitText = 1;
        double limit = ((totalArea/textList.size())*(textList.size() - limitText)/totalArea);
        OcrUtils.log(5, "densitySnake", "Limit density is: " + limit);
        double targetDensity = -1;
        for (Double currentDensity : map.keySet()) {
            OcrUtils.log(5, "densitySnake", "Possible density is: " + currentDensity);
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
        OcrUtils.log(4, "densitySnake", "Final target text is: " + position
            + "\nWith text: " + textList.get(position).text() + "\nand tag: " + tag);
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
     * @param textList list of Texts. Not null.
     * @param tag new tag for Texts. Not null.
     * @param start index of first element. Int >= 0.
     * @param end index of last element. Int >= 0.
     */
    private void intervalTagger(@NonNull List<OcrText> textList, @NonNull String tag, @IntRange(from = 0) int start, @IntRange(from = 0) int end) {
        if (end < start)
            return;
        for (int i = start; i <= end; ++i) {
            OcrText text = textList.get(i);
            removeOldTags(text);
            text.addTag(tag);
            //text.setTagPosition(getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom));
            OcrUtils.log(6, "intervalTagger", "Text: " + text.text() +
                    "\nis in position: " /*+ getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom)*/
                    + "\nwith tag: " + tag);
        }
    }

    /**
     * Tag all texts in a interval with the same tag. Starting and ending indexes are included.
     * @param textList list of Texts. Not null.
     * @param tagLeft new tag for Texts on left. Not null.
     * @param tagCenter new tag for Texts on center. Not null.
     * @param tagRight new tag for Texts on right. Not null.
     * @param start index of first element. Int >= 0.
     * @param end index of last element. Int >= 0.
     */
    private void intervalTagger(@NonNull List<OcrText> textList, @NonNull String tagLeft, @NonNull String tagCenter,
                                       @NonNull String tagRight, @IntRange(from = 0) int start, @IntRange(from = 0) int end) {
        if (end < start)
            return;
        for (int i = start; i <= end; ++i) {
            OcrText text = textList.get(i);
            removeOldTags(text);
            if (text.getTags().contains(LEFT_TAG)) {
                text.addTag(tagLeft);
                //text.setTagPosition(getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom));
                OcrUtils.log(6, "intervalTagger", "Text: " + text.text() +
                        "\nis in position: " /*+ getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom)*/
                        + "\nwith tag: " + tagLeft);
            } else if (text.getTags().contains(RIGHT_TAG)) {
                text.addTag(tagRight);
                //text.setTagPosition(getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom));
                OcrUtils.log(6, "intervalTagger", "Text: " + text.box() +
                        "\nis in position: " /*+ getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom)*/
                        + "\nwith tag: " + tagRight);
            } else {
                text.addTag(tagCenter);
                //text.setTagPosition(getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom));
                OcrUtils.log(6, "intervalTagger", "Text: " + text.text() +
                        "\nis in position: " /*+ getTextBlockPosition(text, textList.get(start).box().top, textList.get(end).box().bottom)*/
                        + "\nwith tag: " + tagCenter);
            }
        }
    }

    /**
     * Remove all tags (excluded left-center-right) from a Text
     * @param text source text. Not Null.
     */
    private void removeOldTags(@NonNull OcrText text) {
        text.removeTag(INTRODUCTION_TAG);
        text.removeTag(CONCLUSION_TAG);
        text.removeTag(PRODUCTS_TAG);
        text.removeTag(PRICES_TAG);
    }
}
