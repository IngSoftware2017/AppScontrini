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

public abstract class Export {

    public final String TAG = "EXPORT_MANAGER";

    File folder;
    protected DataManager database;
    private String pathLocation;


    /**
     * @author Marco Olivieri
     *
     * Costructor
     * @param database, DataManager - the instance of AppScontrini db
     * @param pathLocation, String - path directory to save exportation files
     */
    public Export(DataManager database, String pathLocation) {
        this.database = database;
        this.pathLocation = pathLocation;

        //check new or exist path
        folder = new File(pathLocation);
        boolean check = false;
        if (!folder.exists())
            check = folder.mkdirs();
        Log.d(TAG, "Folder created: "+ String.valueOf(check));
    }

    /**
     * @author Marco Olivieri
     *
     * Export of all the db AppScontrini
     * @return boolean - if the exportation is ok
     */
    public abstract boolean export();

    /**
     * @author Marco Olivieri
     *
     * Export only the specific mission with the relative tickets
     * @param missionID
     * @return boolean - if the exportation is ok
     */
    public abstract boolean export(long missionID);

}
