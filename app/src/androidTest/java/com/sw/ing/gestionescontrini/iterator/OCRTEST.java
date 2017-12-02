package com.sw.ing.gestionescontrini.iterator;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import Iterator.TicketsIterator;

/**
 * Created by Federico Taschin on 20/11/2017.
 */

@RunWith(AndroidJUnit4.class)
public class OCRTEST {

    static final String XML_NAME = "DATASET v2.xml";

    @Test
    public void loadPhotos() throws IOException {
        final String folder = "photos";
        AssetManager mgr = InstrumentationRegistry.getInstrumentation()
                .getContext().getResources().getAssets();
        final int imgTot = mgr.list(folder).length;
        for (int i = 0; i < imgTot; i++) {
            InputStream is = mgr.open(folder + "/" + String.valueOf(i) + ".jpg");
            Bitmap bm = BitmapFactory.decodeStream(is);
        }

        Assert.assertTrue(imgTot>0);
    }

    @Test
    public void loadXml(){
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
    /* Parse the xml-data from our URL. */
            InputStream inputStream = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets().open(XML_NAME);
    /*Get Document Builder*/
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document dom = builder.parse(inputStream);

            Element rootElement = dom.getDocumentElement();
            NodeList list = rootElement.getElementsByTagName("Ticket");
            Log.d("DEBUGOCR","Number of tickets: "+list.getLength());
            Log.d("DEBUGOCR", "Elements in one ticket: "+list.item(0).getChildNodes().getLength());
            Element element = (Element) list.item(0);
            Log.d("DEBUGOCR","ID: "+element.getElementsByTagName("ID").item(0).getTextContent());

        } catch (Exception e) {
            Log.d("ECCEZIONE","ECCCEZIONE: "+e.getMessage());
        }
    }

}
