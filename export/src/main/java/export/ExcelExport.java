package export;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * @author Marco Olivieri on 03/01/2018
 *
 * This class defines the methods to export db datas in Excel file
 * References:
 * http://www.cuelogic.com/blog/creatingreading-an-excel-file-in-android/
 * https://poi.apache.org/download.html
 * http://www.baeldung.com/java-microsoft-excel
 *
 */

public class ExcelExport extends ExportManager {

    private final String TAG = "EXCEL_EXPORT";

    //Header CSV file
    private final String TICKET_FILE_HEADER = "ID;AMOUNT;DATE;SHOP;TITLE;CATEGORY;MISSIONID;URI";
    private final String MISSION_FILE_HEADER = "ID;NAME;STARTDATE;ENDDATE;LOCATION;REPAID;PERSONID";
    private final String PERSON_FILE_HEADER = "ID;NAME;LASTNAME;ACADEMICTITLE";

    //TablesEntity of db
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;

    private File file;
    private String fileName;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd/HH.mm.ss");

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
        fileName = "dbAppScontrini_" + sdf.format(timestamp);
        file = new File(pathLocation, fileName);

        //outFile = new FileInputStream(new File(pathDirectory));
        //Workbook workbook = new XSSFWorkbook(outFile);

    }


    /**
     * @author Marco Olivieri
     *
     * Implementation of the extended abstract class ExportManager
     * @return boolean - if the exportation is ok
     */
    public boolean export(){
        Workbook wb = new HSSFWorkbook();
        return false;
    }
}
