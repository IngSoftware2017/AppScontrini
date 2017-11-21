package Iterator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    Bitmap img_Bmp;
    TicketsIterator<Bitmap> TicketsList = new TicketsIterator();

    /*
     * Test for the add e get methods, check if the element just insert it's the same of the one just extract.
     */
    @Test
    public void Add_n_Get() throws Exception {
        int i = 0;
        int minHeight = 100, maxHeight = 500;
        int minWeight = 100, maxWeight = 500;
        Random numR = new Random();
        int numIMG = 50;
        Bitmap[] IMG_Generated;
        IMG_Generated = new Bitmap[100];
        String path_IMG = "C:\\Users\\matteo.mascotto\\Pictures\\Tickets\\";

        // Generate numIMG of imagine to insert into the Iterator and it save each one of it into an
        // Bitmap array usefull for testing of the get method
        while (i <= numIMG) {
            // Generation of the fake Ticket Bitmap
            try {
                /*
                 * Two different ways to auto-generation fake imagine for testing. Necessary Mock Test!!
                 */
                // 1
                img_Bmp = Bitmap.createBitmap(numR.nextInt(maxHeight - minHeight + 1) + minHeight, numR.nextInt(maxWeight - minWeight + 1) + minWeight, Bitmap.Config.ARGB_4444);
                // 2
                img_Bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

                /*
                 * Extraction of Bitmap from real file and bitmap generation
                 */
                img_Bmp = BitmapFactory.decodeFile(path_IMG + "00");

                IMG_Generated[i] = img_Bmp;

            } catch (Exception e) {
                // Print the cause of the error just generated
                e.getCause().printStackTrace();
            }

            // Addition of the imagine just created
            TicketsList.add(img_Bmp);

            i++;
        }

        // Test if the imagine inserted it is correct
        while (i <= numIMG) {
            assertTrue(IMG_Generated[i] == TicketsList.get(IMG_Generated[i]));
            i++;
        }
    }

    @Test
    public void remove() throws Exception {
    }

    @Test
    public void iterate_throught_TicketsIterator_in_right_order() {

        int i = 0, count = 0;
        int minHeight = 100, maxHeight = 500;
        int minWeight = 100, maxWeight = 500;
        Random numR = new Random();

        while (i <= 10) {
            img_Bmp = Bitmap.createBitmap(numR.nextInt(maxHeight - minHeight + 1) + minHeight, numR.nextInt(maxWeight - minWeight + 1) + minWeight, Bitmap.Config.ARGB_4444);
            TicketsList.add(img_Bmp);
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