package com.ing.software.ocr;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import static com.ing.software.common.Reflect.*;
import static com.ing.software.ocr.OcrVars.DATE_DMY;
import static org.junit.Assert.*;

/**
 * @author Riccardo Zaglia
 */
public class RegexTest {

    private String match(Pattern regex, String target) {
        Matcher matcher = regex.matcher(target);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    @Test
    public void dateDmyTest1() {
        assertEquals(null, match(DATE_DMY, "abc"));
    }


    @Test
    public void potentialPriceTest1() {
        assertEquals(null, match(DATE_DMY, "abc"));
    }

    @Test
    public void priceNoThousandMarkTest1() {
        assertEquals(null, match(DATE_DMY, "abc"));
    }

    @Test
    public void priceUpsideDownTest1() {
        assertEquals(null, match(DATE_DMY, "abc"));
    }
}