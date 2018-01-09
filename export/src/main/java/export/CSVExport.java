package export;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * @author Marco Olivieri on 04/01/2018 (modified by Federico Taschin)
 *
 * This class defines the methods to export db datas in CSV file with semicolon delimiter
 */

public class CSVExport extends Export {

    //Delimiter used in CSV file
    private final String SEMICOLON_DELIMITER = ";";
    private final String NEW_LINE_SEPARATOR = "\n";

    //Header CSV file
    private final String TICKET_FILE_HEADER = "ID;AMOUNT;DATE;SHOP;TITLE;CATEGORY;MISSIONID;URI;CORNERS";
    private final String MISSION_FILE_HEADER = "ID;NAME;STARTDATE;ENDDATE;LOCATION;REPAID;PERSONID";
    private final String PERSON_FILE_HEADER = "ID;NAME;LASTNAME;ACADEMICTITLE;EMAIL;FOTO";

    //TablesEntity of db
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;

    public String TICKETS_FILE_NAME="tickets.csv",MISSIONS_FILE_NAME = "missions.csv", PERSONS_FILE_NAME ="persons.csv", SINGLE_MISSION_FILE_NAME ="mission";
    private final String csvExtension = ".csv";
    private final String TABLE_SEPARATOR = "__________________";

    File exportedTickets;
    File exportedMissions;
    File exportedPersons;


    /**
     * @author Marco Olivieri
     * Costructor
     * @param database, DataManager - the instance of AppScontrini db
     * @param pathLocation, String - path directory to save exportation files
     */
    public CSVExport(DataManager database, String pathLocation){
        super(database, pathLocation);

        exportedTickets = new File(getExportRootDirectory(), TICKETS_FILE_NAME);
        exportedMissions= new File(getExportRootDirectory(),MISSIONS_FILE_NAME);
        exportedPersons = new File(getExportRootDirectory(),PERSONS_FILE_NAME);

        tickets = database.getAllTickets();
        missions = database.getAllMission();
        persons = database.getAllPerson();
    }


    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Implementation of the extended abstract class Export
     * Create a CSV file export for each tables entities of database
     * @return List of file files. If an error occurs, the list is empty
     */
    public List<ExportedFile> export() {
        List<ExportedFile> exportedFiles = new ArrayList<>();
        try {
            FileWriter fileTickets = new FileWriter(exportedTickets);
            fileTickets = writeTickets(fileTickets);
            fileTickets.flush();
            fileTickets.close();

            FileWriter fileMissions = new FileWriter(exportedMissions);
            fileMissions = writeMissions(fileMissions);
            fileMissions.flush();
            fileMissions.close();

            FileWriter filePersons = new FileWriter(exportedPersons);
            filePersons = writePersons(filePersons);
            filePersons.flush();
            filePersons.close();

            exportedFiles.add(new ExportedFile(exportedTickets, ExportedFile.FILE_CONTENT.TICKETS));
            exportedFiles.add(new ExportedFile(exportedMissions,ExportedFile.FILE_CONTENT.MISSIONS));
            exportedFiles.add(new ExportedFile(exportedPersons,ExportedFile.FILE_CONTENT.PERSONS));
        } catch (IOException e) {
            Log.e(TAG(), e.getMessage());
        }
        return exportedFiles;
    }

    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Implementation of the extended abstract class Export
     * Create a CSV file export for the specific mission with relative tickets
     * @param missionId - the specific mission
     * @return the Exported file (null if an error occurs)
     */
    public ExportedFile export(long missionId){
        MissionEntity m = database.getMission(missionId);
        missions.clear();
        missions.add(m);
        tickets = database.getTicketsForMission(missionId);
        try {
            File out = new File(getExportRootDirectory(), SINGLE_MISSION_FILE_NAME+missions.get(0).getID()+csvExtension);
            FileWriter fileWriter = new FileWriter(out);
            fileWriter=writeMissions(fileWriter);
            fileWriter.append(TABLE_SEPARATOR);
            fileWriter = writeTickets(fileWriter);
            fileWriter.flush();
            fileWriter.close();
            return new ExportedFile(out, ExportedFile.FILE_CONTENT.SINGLE_MISSION);
        } catch (IOException e) {
            Log.e(TAG(), e.getMessage());
            return null;
        }
    }

    /** Federico Taschin
     *
     * @return the TAG of this type of export
     */
    @Override
    public String TAG() {
        return "csv";
    }


    /**
     * Federico Taschin
     * @param path the new path for the output file (or dir if the Export creates many files)
     */
    @Override
    public void onPathChanged(String path) {
        exportedTickets = new File(getExportRootDirectory(), TICKETS_FILE_NAME);
        exportedMissions = new File(getExportRootDirectory(),MISSIONS_FILE_NAME);
        exportedPersons = new File(getExportRootDirectory(),PERSONS_FILE_NAME);
    }


    /**
     * @author Marco Olivieri
     *
     * Creates a csv format file with all tickets of db
     * Each ticket is in one line.
     * The fields delimiter is a semicolon.
     * The category list of the ticket is wrote with all values separated from a slash
     * @param fileTickets, FileWriter - the file on which to write
     * @return FileWriter - complete file with all tickets
     */
    private FileWriter writeTickets(FileWriter fileTickets){
        try{
            fileTickets.append(TICKET_FILE_HEADER);
            fileTickets.append(NEW_LINE_SEPARATOR);
            for (TicketEntity t : tickets) {
                fileTickets.append(String.valueOf(t.getID()));
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(String.valueOf(t.getAmount()));
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(String.valueOf(t.getDate()));
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(t.getShop());
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(t.getTitle());
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(categoryToString(t.getCategory()));
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(String.valueOf(t.getMissionID()));
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(String.valueOf(t.getFileUri()));
                fileTickets.append(SEMICOLON_DELIMITER);
                fileTickets.append(cornersToString(t.getCorners()));
                fileTickets.append(NEW_LINE_SEPARATOR);
            }
        }
        catch (IOException e){
            Log.e(TAG(), e.getMessage());
        }
        return fileTickets;
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
                s+=list.get(i)+"/";
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
            return null;
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
     * Creates a csv format file with all missions of db
     * Each mission is in one line.
     * The fields delimiter is a semicolon.
     * @param fileMissions, FileWriter - the file on which to write
     * @return FileWriter - complete file with all missions
     */
    private FileWriter writeMissions(FileWriter fileMissions){
        try{
            fileMissions.append(MISSION_FILE_HEADER);
            fileMissions.append(NEW_LINE_SEPARATOR);
            for (MissionEntity m : missions) {
                fileMissions.append(String.valueOf(m.getID()));
                fileMissions.append(SEMICOLON_DELIMITER);
                fileMissions.append(m.getName());
                fileMissions.append(SEMICOLON_DELIMITER);
                fileMissions.append(String.valueOf(m.getStartDate()));
                fileMissions.append(SEMICOLON_DELIMITER);
                fileMissions.append(String.valueOf(m.getEndDate()));
                fileMissions.append(SEMICOLON_DELIMITER);
                fileMissions.append(m.getLocation());
                fileMissions.append(SEMICOLON_DELIMITER);
                fileMissions.append(String.valueOf(m.isRepay()));
                fileMissions.append(SEMICOLON_DELIMITER);
                fileMissions.append(String.valueOf(m.getPersonID()));
                fileMissions.append(NEW_LINE_SEPARATOR);
            }
        }
        catch (IOException e){
            Log.e(TAG(), e.getMessage());
        }
        return fileMissions;
    }


    /**
     * @author Marco Olivieri
     *
     * Creates a csv format file with all persons of db
     * Each person is in one line.
     * The fields delimiter is a semicolon.
     * @param filePersons, FileWriter - the file on which to write
     * @return FileWriter - complete file with all persons
     */
    private FileWriter writePersons(FileWriter filePersons){
        try{
            filePersons.append(PERSON_FILE_HEADER);
            filePersons.append(NEW_LINE_SEPARATOR);
            for (PersonEntity p : persons) {
                filePersons.append(String.valueOf(p.getID()));
                filePersons.append(SEMICOLON_DELIMITER);
                filePersons.append(p.getName());
                filePersons.append(SEMICOLON_DELIMITER);
                filePersons.append(p.getLastName());
                filePersons.append(SEMICOLON_DELIMITER);
                filePersons.append(p.getAcademicTitle());
                filePersons.append(SEMICOLON_DELIMITER);
                filePersons.append((p.getEmail()));
                filePersons.append(SEMICOLON_DELIMITER);
                filePersons.append(String.valueOf(p.getFoto()));
                filePersons.append(NEW_LINE_SEPARATOR);
            }
        }
        catch (IOException e){
            Log.e(TAG(), e.getMessage());
        }
        return filePersons;
    }
}
