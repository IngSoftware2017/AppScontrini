package export;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * @author Marco Olivieri on 05/01/2018 (modified by Federico Taschin)
 *
 * This class defines the methods to export db datas in XML file
 *
 * References:
 * https://stackoverflow.com/questions/5181294/how-to-create-xml-file-in-android
 * https://developer.android.com/reference/org/xmlpull/v1/XmlSerializer.html
 */

public class XMLExport extends Export {

    //TablesEntity of db
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;

    private File file;
    private String fileName;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    XmlSerializer serializer;

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
        fileName = "export_" + sdf.format(timestamp)+".xml";
        file = new File(getExportRootDirectory(), fileName);
    }


    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Implementation of the extended abstract class Export
     * Writes all tables entities in a XML file
     * Than it creates the file
     * @return List of file files. If an error occurs, the list is empty
     */
    public List<ExportedFile> export(){
        List<ExportedFile> exportedFiles = new ArrayList<>();
        try{
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));

            serializer.startTag(null,"Database");
            serializer.startTag(null,"Tables");

            writeTickets();
            writeMissions();
            writePersons();

            serializer.endTag(null,"Tables");
            serializer.endTag(null,"Database");


            serializer.endDocument();
            serializer.flush();
            fos.close();
            exportedFiles.add(new ExportedFile(file,ExportedFile.FILE_CONTENT.DATABASE));
            Log.d("EXPORTDEBUG",file.exists()?"FILE CREATED":"FILE NOT CREATED");
            Log.d("EXPORTDEBUG","PATH: "+file.getAbsolutePath());
        }
        catch (IOException e){
            Log.e(TAG(), e.getMessage());
        }
        return exportedFiles;
    }


    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Implementation of the extended abstract class Export
     * Writes the specific mission with relative tickets in a XML file
     * Than it creates the file
     * @param missionId - the specific mission
     * @return the Exported file (null if an error occurs)
     */
    public ExportedFile export(long missionId){

        MissionEntity m = database.getMission(missionId);
        missions.clear();
        missions.add(m);
        tickets = database.getTicketsForMission(missionId);

        try{
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));

            serializer.startTag(null,"Database");
            serializer.startTag(null,"Tables");

            writeTickets();
            writeMissions();

            serializer.endTag(null,"Tables");
            serializer.endTag(null,"Database");


            serializer.endDocument();
            serializer.flush();
            fos.close();
            return new ExportedFile(file, ExportedFile.FILE_CONTENT.SINGLE_MISSION);
        }
        catch (IOException e){
            Log.e(TAG(), e.getMessage());
            return null;
        }
    }

    /**
     * @author Federico Taschin
     * @return the TAG for this kind of export
     */
    @Override
    public String TAG() {
        return "xml";
    }

    /**
     * Re instantiates the output file (since the root was changed)
     * @author Federico Taschin
     * @param path the new root path (not used in this case)
     */
    @Override
    public void onPathChanged(String path) {
        file = new File(getExportRootDirectory(),fileName);
    }


    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Writes in xmlSerializer all the tickets
     * @throws IOException
     */
    private void writeTickets() throws IOException {
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
                serializer.startTag(null, "Corners");
                serializer.text(cornersToString(t.getCorners()));
                serializer.endTag(null, "Corners");
            }
            serializer.endTag(null,"Ticket");

        }catch (IOException e) {
            Log.e(TAG(), e.getMessage());
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
            return "";
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
     * Converts from a float[] to a String for db
     * @param corners, float[] of the rectangle coordinates
     * @return the corresponding String object, null if value is null
     */
    public String cornersToString(float[] corners) {
        if (corners == null)
            return "";
        else
        {
            String s="";
            for (int i=0; i<corners.length; i++)
                s+=corners[i]+";";
            return s;
        }
    }


    /**
     * @author Marco Olivieri
     *
     * Writes in xmlSerializer all the missions
     * @throws IOException
     */
    private void writeMissions() throws IOException {
        try {
            serializer.startTag(null,"Mission");
            for (int i=0; i<missions.size();i++) {
                MissionEntity m = missions.get(i);
                serializer.startTag(null, "Id");
                serializer.text(String.valueOf(m.getID()));
                serializer.endTag(null, "Id");
                serializer.startTag(null, "Name");
                serializer.text(m.getName());
                serializer.endTag(null, "Name");
                serializer.startTag(null, "StartDate");
                serializer.text(String.valueOf(m.getStartDate()));
                serializer.endTag(null, "StartDate");
                serializer.startTag(null, "EndDate");
                serializer.text(String.valueOf(m.getEndDate()));
                serializer.endTag(null, "EndDate");
                serializer.startTag(null, "Location");
                serializer.text(m.getLocation());
                serializer.endTag(null, "Location");
                serializer.startTag(null, "Closed");
                serializer.text(String.valueOf(m.isClosed()));
                serializer.endTag(null, "Closed");
                serializer.startTag(null, "PersonId");
                serializer.text(String.valueOf(m.getPersonID()));
                serializer.endTag(null, "PersonId");
            }
            serializer.endTag(null,"Mission");

        }catch (IOException e) {
            Log.e(TAG(), e.getMessage());
        }
    }


    /**
     * @author Marco Olivieri
     *
     * Writes in xmlSerializer all the persons
     * @throws IOException
     */
    private void writePersons() throws IOException {
        try {
            serializer.startTag(null,"Person");
            for (int i=0; i<persons.size();i++) {
                PersonEntity p = persons.get(i);
                serializer.startTag(null, "Id");
                serializer.text(String.valueOf(p.getID()));
                serializer.endTag(null, "Id");
                serializer.startTag(null, "Name");
                serializer.text(p.getName());
                serializer.endTag(null, "Name");
                serializer.startTag(null, "LastName");
                serializer.text(p.getLastName());
                serializer.endTag(null, "LastName");
                serializer.startTag(null, "AcademicTitle");
                serializer.text(p.getAcademicTitle());
                serializer.endTag(null, "AcademicTitle");
                serializer.startTag(null, "Email");
                serializer.text(p.getEmail());
                serializer.endTag(null, "Email");
                serializer.startTag(null, "Foto");
                serializer.text(String.valueOf(p.getFoto()));
                serializer.endTag(null, "Foto");
            }
            serializer.endTag(null,"Person");

        }catch (IOException e) {
            Log.e(TAG(), e.getMessage());
        }
    }

}
