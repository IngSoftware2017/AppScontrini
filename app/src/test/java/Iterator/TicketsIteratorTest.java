package Iterator;

import android.graphics.Bitmap;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Test class for the data structure TicketsIterator
 *
 * Created by matteo.mascotto on 18/11/2017.
 */
public class TicketsIteratorTest {

    Bitmap img;
    TicketsIterator<Bitmap> TicketsList = new TicketsIterator();

    /*
    How can i insert a new bitmap object in the  TicketsIterator?
     */

    @Test
    public void add() throws Exception {
        // assertTrue(TicketsList.add(img) == TicketsList.get(img));
    }

    @Test
    public void get() throws Exception {
    }

    @Test
    public void iterate_throught_TicketsIterator_in_right_order() {

        int i = 0, count = 0;
        int minHeight = 100, maxHeight = 500;
        int minWeight = 100, maxWeight = 500;
        Random numR = new Random();

        while (i <= 10) {
            img = Bitmap.createBitmap(numR.nextInt(maxHeight - minHeight + 1) + minHeight, numR.nextInt(maxWeight - minWeight + 1) + minWeight, Bitmap.Config.ARGB_4444);
            TicketsList.add(img);
            i++;
        }

        Iterator<Bitmap> It_Bmp = TicketsList.iterator();

        while (It_Bmp.hasNext()) {
            Bitmap element = It_Bmp.next();
            count++;

            // assertThat(element, equals(img));
        }

        // assertThat(count, equals(10));
    }
}