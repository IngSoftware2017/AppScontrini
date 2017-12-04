package com.sw.ing.gestionescontrini.iterator.iterator.test;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import com.sw.ing.gestionescontrini.iterator.Bundle;
import com.sw.ing.gestionescontrini.iterator.TicketInfo;
import com.sw.ing.gestionescontrini.iterator.TicketsIterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Federico Taschin on 03/12/2017.
 */

@RunWith(AndroidJUnit4.class)
public class SampleIteratorTestUse {

    public final String XML_NAME = "DATASET v2.xml";
    /* Created by Federico Taschin
     * This class shows how to use the Iterator.
     */
    @Test
    public void useIterator() {
        try {
            Iterator<Bundle> iterator = new TicketsIterator.IteratorBuilder().setXML(XML_NAME).build(); //Build the iterator

            // use the iterator
            while(iterator.hasNext()){
                Bundle bundle = iterator.next();
                TicketInfo info = bundle.getTicketInfo();
                Bitmap bitmap = bundle.getBitmap();
            }
        //exceptions generated if an error in the reading of the xml occurs
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
