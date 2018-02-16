package com.ing.software.ocr;

import org.junit.Test;

import java.util.regex.*;

import static com.ing.software.ocr.DataAnalyzer.*;
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

    /*
    @Test
    public void dateDmyTest1() {
        assertEquals(null, match(DATE_DMY, "abc"));
    }
    */

    @Test
    public void potentialPriceTest1() {
        assertEquals(null, match(POTENTIAL_PRICE, "abc"));
    }

    @Test
    public void priceWithSpacesTest1() {
        assertEquals(null, match(PRICE_WITH_SPACES, "abc"));
    }

    @Test
    public void priceUpsideDownTest1() {
        assertEquals(null, match(PRICE_UPSIDEDOWN, "abc"));
    }

    @Test
    public void mfkdlfmdsl() {
        int month = 3;

//        assertEquals(true, Arrays.asList(1, 2, 3, 4, 5).contains(month));
//        Date date = new GregorianCalendar(2000, 1, 29).getTime();
        assertEquals(3, (int)Integer.valueOf("03"));
    }

}