package export;

import java.io.File;

import database.DataManager;

/**
 * @author Marco Olivieri on 03/01/2018
 *
 * This class must be implemented by a specific class that override export method
 * This class is used in polimorfism to export db
 */

public abstract class ExportManager {

    DataManager database;
    String pathLocation;


    public ExportManager(DataManager database, String pathLocation) {
        this.database = database;
        this.pathLocation = pathLocation;
        //TODO : check new or exist path
    }

    abstract boolean export();
}
