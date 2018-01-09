package export;

import android.util.Log;

import java.io.File;
import java.util.List;

import database.DataManager;

/**
 * @author Marco Olivieri on 03/01/2018 (modified by Federico Taschin)
 *
 * This class must be implemented by a specific class that override export method
 * This class is used in polimorfism to export db
 */

public abstract class Export {

    private File folder;
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
        Log.d(TAG(), "Folder created: "+ String.valueOf(check));
    }

    /**
     * @author Marco Olivieri
     *
     * Export of all the db AppScontrini
     * @return List of created files
     */
    public abstract List<ExportedFile> export();

    /**
     * @author Marco Olivieri
     *
     * Export only the specific mission with the relative tickets
     * @param missionID
     * @return the created file
     */
    public abstract ExportedFile export(long missionID);

    /**
     * @author Federico Taschin
     * @return the TAG for this kind of export
     */
    public abstract String TAG();

    /**
     * @author Federico Taschin
     * @return the root directory for the file files
     */
    public File getExportRootDirectory(){
        return folder;
    }

    /**
     * Changes the root path of the output with the path given.
     * It calls the abstract method onPathChanged(String path) so that the implementation of export
     * can know when the root path is changed and can modify its private files (if it has)
     * @author Federico Taschin
     * @param path the new path of the output root.
     */
    public void changePath(String path){
        folder = new File(path);
        onPathChanged(path);
    }

    /**
     * Called when the root path for the output is changed. In this method one should execute the operations
     * needed when the root path changes (ex private file and subdirectories handling)
     * @author Federico Taschin
     * @param path the new root path
     */
    abstract void onPathChanged(String path);

    /**
     * @author Federico TAschin
     * @return the TAG for this kind of export
     */
    @Override
    public String toString(){
        return TAG();
    }

}
