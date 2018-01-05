package export;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import database.DataManager;
import database.Database;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * @author Marco Olivieri on 05/01/2018
 *
 * This class defines the methods to export db datas in XML file
 *
 * References:
 * https://stackoverflow.com/questions/5181294/how-to-create-xml-file-in-android
 * https://developer.android.com/reference/org/xmlpull/v1/XmlSerializer.html
 */

public class XMLExport extends ExportManager {

    private final String TAG = "EXCEL_EXPORT";

    /*Header CSV file
    private final String TICKET_FILE_HEADER = "ID;AMOUNT;DATE;SHOP;TITLE;CATEGORY;MISSIONID;URI";
    private final String MISSION_FILE_HEADER = "ID;NAME;STARTDATE;ENDDATE;LOCATION;REPAID;PERSONID";
    private final String PERSON_FILE_HEADER = "ID;NAME;LASTNAME;ACADEMICTITLE";
    */


    //TablesEntity of db
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;

    private File file;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

    /**
     * @author Marco Olivieri
     *
     * Costructor
     * @param database, DataManager - the instance of AppScontrini db
     * @param pathLocation, String - path directory to save exportation files
     */
    public XMLExport(DataManager database, String pathLocation){
        super(database, pathLocation);

        tickets = database.getAllTickets();
        missions = database.getAllMission();
        persons = database.getAllPerson();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String fileName = "export_" + sdf.format(timestamp)+".xml";
        file = new File(pathLocation, fileName);
    }



    public boolean export(){

        try{
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));

            serializer.startTag(null,"Database");
            serializer.startTag(null,"Tables");

            writeTickets(serializer);
            writeMissions(serializer);
            writePersons(serializer);

            serializer.endTag(null,"Tables");
            serializer.endTag(null,"Database");


            serializer.endDocument();
            serializer.flush();
            fos.close();
            return true;
        }
        catch (IOException e){
            Log.e(TAG, e.getMessage());
            return false;
        }
    }


    /**
     * @author Marco Olivieri
     *
     * Writes in xmlSerializer all the tickets
     * @param serializer, XmlSerializer - where the tickets are wrote
     * @throws IOException
     */
    private void writeTickets(XmlSerializer serializer) throws IOException {
        try {
            serializer.startTag(null,"Ticket");
            for (int i=0; i<tickets.size();i++) {
                TicketEntity t = tickets.get(i);
                serializer.startTag(null, "Id");
                serializer.text(String.valueOf(t.getID()));
                serializer.endTag(null, "Id");
                serializer.startTag(null, "Amount");
                serializer.text(String.valueOf(t.getAmount()));
                serializer.endTag(null, "Amount");
                serializer.startTag(null, "Date");
                serializer.text(String.valueOf(t.getDate()));
                serializer.endTag(null, "Date");
                serializer.startTag(null, "Shop");
                serializer.text(t.getShop());
                serializer.endTag(null, "Shop");
                serializer.startTag(null, "Title");
                serializer.text(t.getTitle());
                serializer.endTag(null, "Title");
                serializer.startTag(null, "Category");
                serializer.text(categoryToString(t.getCategory()));
                serializer.endTag(null, "Category");
                serializer.startTag(null, "MissionId");
                serializer.text(String.valueOf(t.getMissionID()));
                serializer.endTag(null, "MissionId");
                serializer.startTag(null, "Uri");
                serializer.text(String.valueOf(t.getFileUri()));
                serializer.endTag(null, "Uri");
            }
            serializer.endTag(null,"Ticket");

        }catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     * @author Marco Olivieri
     *
     * Converts a list of string in one string with all values separeted from a slash
     * @param list, List<String>
     * @return String - string with all values
     */
    private String categoryToString(List<String> list) {
        if (list == null)
            return null;
        else
        {
            String s="";
            for (int i=0; i<list.size(); i++)
                s+=list.get(i)+",";
            return s;
        }
    }


    /**
     * @author Marco Olivieri
     *
     * Writes in xmlSerializer all the tickets
     * @param serializer, XmlSerializer - where the missions are wrote
     * @throws IOException
     */
    private void writeMissions(XmlSerializer serializer) throws IOException {
        try {
            serializer.startTag(null,"Mission");
            for (int i=0; i<missions.size();i++) {
                MissionEntity m = missions.get(i);

            }
            serializer.endTag(null,"Mission");

        }catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void writePersons(XmlSerializer serializer) throws IOException {}

}
