package export;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * https://poi.apache.org/download.html
 * http://www.baeldung.com/java-microsoft-excel
 */

public class ExcelExport extends ExportManager {
    private List<TicketEntity> tickets;
    private List<MissionEntity> missions;
    private List<PersonEntity> persons;
    private File file;
    private String fileName;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd/HH.mm.ss");

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



    public boolean export(){
        return false;
    }
}
