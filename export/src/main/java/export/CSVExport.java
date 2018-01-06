package export;

import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * @author Marco Olivieri on 04/01/2018
 *
 * This class defines the methods to export db datas in CSV file with semicolon delimiter
 */

public class CSVExport extends ExportManager {

    private final String TAG = "CSV_EXPORT";

    //Delimiter used in CSV file
    private final String SEMICOLON_DELIMITER = ";";
    private final String NEW_LINE_SEPARATOR = "\n";

    //Header CSV file
    private final String TICKET_FILE_HEADER = "ID;AMOUNT;DATE;SHOP;TITLE;CATEGORY;MISSIONID;URI";
    private final String MISSION_FILE_HEADER = "ID;NAME;STARTDATE;ENDDATE;LOCATION;REPAID;PERSONID";
    private final String PERSON_FILE_HEADER = "ID;NAME;LASTNAME;ACADEMICTITLE;EMAIL;FOTO";

    //TablesEntity of db
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;

    /**
     * @author Marco Olivieri
     *
     * Costructor
     * @param database, DataManager - the instance of AppScontrini db
     * @param pathLocation, String - path directory to save exportation files
     */
    public CSVExport(DataManager database, String pathLocation){
        super(database, pathLocation);

        tickets = database.getAllTickets();
        missions = database.getAllMission();
        persons = database.getAllPerson();
    }


    /**
     * @author Marco Olivieri
     *
     * Implementation of the extended abstract class ExportManager
     * Create a CSV file export for each tables entities of database
     * @return boolean - if the exportation is ok
     */
    public boolean export() {

        try {

            FileWriter fileTickets = new FileWriter(folder + "/tickets.csv");
            fileTickets = writeTickets(fileTickets);
            fileTickets.flush();
            fileTickets.close();

            FileWriter fileMissions = new FileWriter(folder + "/missions.csv");
            fileMissions = writeMissions(fileMissions);
            fileMissions.flush();
            fileMissions.close();

            FileWriter filePersons = new FileWriter(folder + "/persons.csv");
            filePersons = writePersons(filePersons);
            filePersons.flush();
            filePersons.close();

            return true;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
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
                fileTickets.append(NEW_LINE_SEPARATOR);
            }
        }
        catch (IOException e){
            Log.e(TAG, e.getMessage());
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
            Log.e(TAG, e.getMessage());
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
            Log.e(TAG, e.getMessage());
        }
        return filePersons;
    }
}
