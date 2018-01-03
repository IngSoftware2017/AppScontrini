package export;

import java.io.File;

import database.Database;

/**
 * @author Marco Olivieri on 03/01/2018
 *
 * This class defines the methods to export db datas in Excel file
 */

public class DBtoExcel extends ExportManager {

    public DBtoExcel(Database database, String pathDirectory){
        super(database, pathDirectory);
    }

    public File export(){
        return null;
    }
}
