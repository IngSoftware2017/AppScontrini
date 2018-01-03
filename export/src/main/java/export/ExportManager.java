package export;

import java.io.File;

import database.Database;

/**
 * @author Marco Olivieri on 03/01/2018
 *
 * This class must be implemented by a specific class that override export method
 * This class is used in polimorfism to export db
 */

public abstract class ExportManager {

    Database database;
    String pathDirectory;


    public ExportManager(Database database, String pathDirectory) {
        this.database = database;
        this.pathDirectory = pathDirectory;
        //TODO : check new or exist path
    }

    abstract File export();
}
