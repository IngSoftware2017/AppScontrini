package com.sw.ing.gestionescontrini.iterator;

import android.support.test.InstrumentationRegistry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import database.Ticket;

/**
 * Created by Federico Taschin on 02/12/2017.
 */

public class XMLParser {
    private static final String XML_NAME = "DATASET v2.xml";
    private static final String TICKET_TAG = "Ticket";
    private static final String ID_TAG = "ID";
    private static final String DATE_TAG = "Date";
    private static final String POSITION_DATE_TAG = "PositionDate";
    private static final String AMOUNT_TAG = "Amount";
    private static final String POSITION_AMOUNT_TAG = "PositionAmount";
    private static final String SHOP_TAG = "Shop";
    private static final String FEATURES_TAG = "Features";

    private int current;
    private ArrayList<TicketInfo> tickets;

    public XMLParser(){
        tickets = new ArrayList<TicketInfo>();
    }


    /**
     *
     * @throws IOException if an error in the opening of the xml file occurs
     * @throws ParserConfigurationException
     * @throws SAXException if an error in the parsing of the xml occurs
     */
    public void parseXML() throws IOException, ParserConfigurationException, SAXException {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    /* Parse the xml-data from our URL. */
            InputStream inputStream = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets().open(XML_NAME);
    /*Get Document Builder*/
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document dom = builder.parse(inputStream);

            Element rootElement = dom.getDocumentElement();
            NodeList list = rootElement.getElementsByTagName(TICKET_TAG);
            for(int i = 0; i<list.getLength(); i++){
                try {
                    tickets.add(readTicket(list.item(i)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
    }


    /**
     * @param node the node to be parsed
     * @return TicketInfo with the information about the xml Ticket element
     * @throws ParseException if a field of the <Ticket> element is in the wrong format
     */
    private TicketInfo readTicket(Node node) throws ParseException {
        TicketInfo ticket = new TicketInfo();
        Element element = (Element) node;
        //Values to be parsed
        String idValue = element.getElementsByTagName(ID_TAG).item(0).getTextContent();
        String dateValue = element.getElementsByTagName(DATE_TAG).item(0).getTextContent();
        String positionDateValue = element.getElementsByTagName(POSITION_DATE_TAG).item(0).getTextContent();
        String amountValue = element.getElementsByTagName(AMOUNT_TAG).item(0).getTextContent();
        String positionAmountValue = element.getElementsByTagName(POSITION_AMOUNT_TAG).item(0).getTextContent();
        //Already valid values
        String shop = element.getElementsByTagName(SHOP_TAG).item(0).getTextContent();
        String features = element.getElementsByTagName(FEATURES_TAG).item(0).getTextContent();
        //Parsing values
        int id = Integer.parseInt(idValue);
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-mm-yyyy");
        Date date = dateformat.parse(dateValue);
        int[] positionDate = readCoordinates(positionDateValue);
        BigDecimal amount = new BigDecimal(Double.parseDouble(amountValue));
        int[] positionAmount = readCoordinates(positionAmountValue);
        //Setting values
        ticket.setID(id);
        ticket.setDate(date);
        ticket.setPositionDate(positionDate);
        ticket.setAmount(amount);
        ticket.setPositionAmount(positionAmount);
        ticket.setShop(shop);
        ticket.setFeatures(features);
        return ticket;
    }

    /**
     * @param coords String in the format (xcoord, ycoord)
     * @return array of x and y values
     */
    private int[] readCoordinates(String coords){
        coords = coords.substring(1,coords.length());
        String x = coords.substring(0,coords.indexOf(","));
        String y = coords.substring(coords.indexOf(",")+1,coords.length());
        x = x.replace(" ","");
        y = y.replace(" ","");
        return new int[]{Integer.parseInt(x), Integer.parseInt(y)};
    }

    /**
     * @return an ArrayList<TicketInfo> containing all the TicketInfo of the xml
     */
    public ArrayList<TicketInfo> getTicketInfos (){
        return  tickets;
    }

    /**
     * @param id the id of the ticket
     * @return TicketInfo of the desired ticket, null if id is not valid
     */
    public TicketInfo getTicketInfo (int id){
        for(TicketInfo ticket : tickets){
            try {
                if (ticket.getID() == id)
                    return ticket;
            }catch(NullPointerException e){
                return null;
            }
        }
        return null;
    }

}
