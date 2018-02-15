package export;

import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
 * @author Marco Olivieri on 03/01/2018 (modified by Federico Taschin)
 *
 * This class defines the methods to export db datas in Excel file
 * References:
 * https://stackoverflow.com/questions/8006087/how-to-create-an-excel-file-in-android
 * https://poi.apache.org/download.html
 */

public class ExcelExport extends Export {

    //Header CSV file
    private final String TICKET_FILE_HEADER = "ID;AMOUNT;DATE;SHOP;TITLE;CATEGORY;MISSIONID;URI;CORNERS";
    private final String MISSION_FILE_HEADER = "ID;NAME;STARTDATE;ENDDATE;LOCATION;REPAID;PERSONID";
    private final String PERSON_FILE_HEADER = "ID;NAME;LASTNAME;ACADEMICTITLE;EMAIL;FOTO";

    //TablesEntity of db
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;

    private File file;
    String fileName;
    HSSFWorkbook workbook; //Excel document
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

    /**
     * @author Marco Olivieri
     *
     * Costructor
     * @param database, DataManager - the instance of AppScontrini db
     * @param pathLocation, String - path directory to save exportation files
     */
    public ExcelExport(DataManager database, String pathLocation){
        super(database, pathLocation);

        tickets = database.getAllTickets();
        missions = database.getAllMission();
        persons = database.getAllPerson();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        fileName = "export_" + sdf.format(timestamp)+".xlsx";
        file = new File(pathLocation, fileName);
    }


    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Implementation of the extended abstract class Export
     * Writes all tables entities in separeted sheet of the workbook.
     * Than it creates the complete document of the exportation db
     * @return List of file files. If an error occurs, the list is empty
     */
    public List<ExportedFile> export(){
        List<ExportedFile> exportedFiles = new ArrayList<>();
        workbook = new HSSFWorkbook();

        writeTickets();
        writeMissions();
        writePersons();
        try{
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.flush();
            fos.close();
            exportedFiles.add(new ExportedFile(file,ExportedFile.FILE_CONTENT.DATABASE));
            return exportedFiles;
        }
        catch (IOException e){
            Log.e(TAG(), e.getMessage());
            return exportedFiles;
        }
    }


    /**
     * @author Marco Olivieri (modified by Federico Taschin)
     *
     * Implementation of the extended abstract class Export
     * Writes the specific mission with relative tickets in separeted sheet of the workbook.
     * Than it creates the complete document of the exportation db
     * @param missionId - the specific mission
     * @return the ExportedFile, null if an error occurred
     */
    public ExportedFile export(long missionId){
        workbook = new HSSFWorkbook();
        MissionEntity m = database.getMission(missionId);
        missions.clear();
        missions.add(m);
        tickets = database.getTicketsForMission(missionId);

        writeTickets();
        writeMissions();

        try{
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.flush();
            fos.close();
            return new ExportedFile(file,ExportedFile.FILE_CONTENT.SINGLE_MISSION);
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
        return "xls";
    }

    /**
     * @author Federico Taschin
     * @param path the new path for the output
     */
    @Override
    void onPathChanged(String path) {
        file= new File(getExportRootDirectory(), fileName);
    }

    /**
     * @author Marco Olivieri
     *
     * Writes in workbook all the tickets in a separeted sheet
     * The category list of the ticket is wrote in one cell all values separated from a slash
     */
    private void writeTickets(){
        //creates a new sheet
        HSSFSheet firstSheet = workbook.createSheet("Tickets");

        //create header row
        HSSFRow rowHeader = firstSheet.createRow(0);
        String[] header = TICKET_FILE_HEADER.split(";");
        for(int i=0; i<header.length; i++) {
            HSSFCell icell = rowHeader.createCell(i);
            icell.setCellValue(new HSSFRichTextString(header[i]));
        }

        //write all one ticket row
        for (int i=0; i<tickets.size(); i++) {
            HSSFRow irow = firstSheet.createRow(i+1); //row 0 alredy created above

            TicketEntity t = tickets.get(i);
            HSSFCell cell0 = irow.createCell(0);
            cell0.setCellValue(new HSSFRichTextString(String.valueOf(t.getID())));

            HSSFCell cell1 = irow.createCell(1);
            cell1.setCellValue(new HSSFRichTextString(String.valueOf(t.getAmount())));

            HSSFCell cell2 = irow.createCell(2);
            cell2.setCellValue(new HSSFRichTextString(String.valueOf(t.getDate())));

            HSSFCell cell3 = irow.createCell(3);
            cell3.setCellValue(new HSSFRichTextString(t.getShop()));

            HSSFCell cell4 = irow.createCell(4);
            cell4.setCellValue(new HSSFRichTextString(t.getTitle()));

            HSSFCell cell5 = irow.createCell(5);
            cell5.setCellValue(new HSSFRichTextString(categoryToString(t.getCategory())));

            HSSFCell cell6 = irow.createCell(6);
            cell6.setCellValue(new HSSFRichTextString(String.valueOf(t.getMissionID())));

            HSSFCell cell7 = irow.createCell(7);
            cell7.setCellValue(new HSSFRichTextString(String.valueOf(t.getFileUri())));

            HSSFCell cell8 = irow.createCell(8);
            cell8.setCellValue(new HSSFRichTextString(cornersToString(t.getCorners())));
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
     * Writes in workbook all the missions in a separeted sheet
     */
    private void writeMissions(){
        //creates a new sheet
        HSSFSheet secondSheet = workbook.createSheet("Missions");

        //create header row
        HSSFRow rowHeader = secondSheet.createRow(0);
        String[] header = MISSION_FILE_HEADER.split(";");
        for(int i=0; i<header.length; i++) {
            HSSFCell icell = rowHeader.createCell(i);
            icell.setCellValue(new HSSFRichTextString(header[i]));
        }

        //write all one mission row
        for (int i=0; i<missions.size(); i++) {
            HSSFRow irow = secondSheet.createRow(i+1); //row 0 alredy created above

            MissionEntity m = missions.get(i);
            HSSFCell cell0 = irow.createCell(0);
            cell0.setCellValue(new HSSFRichTextString(String.valueOf(m.getID())));

            HSSFCell cell1 = irow.createCell(1);
            cell1.setCellValue(new HSSFRichTextString(m.getName()));

            HSSFCell cell2 = irow.createCell(2);
            cell2.setCellValue(new HSSFRichTextString(String.valueOf(m.getStartDate())));

            HSSFCell cell3 = irow.createCell(3);
            cell3.setCellValue(new HSSFRichTextString(String.valueOf(m.getEndDate())));

            HSSFCell cell4 = irow.createCell(4);
            cell4.setCellValue(new HSSFRichTextString(m.getLocation()));

            HSSFCell cell5 = irow.createCell(5);
            cell5.setCellValue(new HSSFRichTextString(String.valueOf(m.isClosed())));

            HSSFCell cell6 = irow.createCell(6);
            cell6.setCellValue(new HSSFRichTextString(String.valueOf(m.getPersonID())));

        }
    }


    /**
     * @author Marco Olivieri
     *
     * Writes in workbook all the persons in a separeted sheet
     */
    private void writePersons(){
        //creates a new sheet
        HSSFSheet thirdSheet = workbook.createSheet("Persons");

        //create header row
        HSSFRow rowHeader = thirdSheet.createRow(0);
        String[] header = PERSON_FILE_HEADER.split(";");
        for(int i=0; i<header.length; i++) {
            HSSFCell icell = rowHeader.createCell(i);
            icell.setCellValue(new HSSFRichTextString(header[i]));
        }

        //write all one person row
        for (int i=0; i<persons.size(); i++) {
            HSSFRow irow = thirdSheet.createRow(i+1); //row 0 alredy created above

            PersonEntity p = persons.get(i);
            HSSFCell cell0 = irow.createCell(0);
            cell0.setCellValue(new HSSFRichTextString(String.valueOf(p.getID())));

            HSSFCell cell1 = irow.createCell(1);
            cell1.setCellValue(new HSSFRichTextString(p.getName()));

            HSSFCell cell2 = irow.createCell(2);
            cell2.setCellValue(new HSSFRichTextString(p.getLastName()));

            HSSFCell cell3 = irow.createCell(3);
            cell3.setCellValue(new HSSFRichTextString(p.getAcademicTitle()));

            HSSFCell cell4 = irow.createCell(4);
            cell4.setCellValue(new HSSFRichTextString(p.getEmail()));

            HSSFCell cell5 = irow.createCell(5);
            cell5.setCellValue(new HSSFRichTextString(String.valueOf(p.getFoto())));

        }
    }

}
