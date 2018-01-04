package export;

import android.util.Log;

import java.io.File;

import database.DataManager;

/**
 * @author Marco Olivieri on 03/01/2018
 *
 * This class must be implemented by a specific class that override export method
 * This class is used in polimorfism to export db
 */

public abstract class ExportManager {

    private final String TAG = "EXPORT_MANAGER";

    File folder;
    DataManager database;
    String pathLocation;


    public ExportManager(DataManager database, String pathLocation) {
        this.database = database;
        this.pathLocation = pathLocation;

        //check new or exist path
        folder = new File(pathLocation);
        boolean check = false;
        if (!folder.exists())
            check = folder.mkdir();
        Log.d(TAG, "Folder created: "+ String.valueOf(check));
    }

    public abstract boolean export();
}
