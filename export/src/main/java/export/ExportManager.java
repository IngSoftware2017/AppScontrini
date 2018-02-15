package export;

import android.util.Log;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;

/**
 * Created by Federico Taschin on 08/01/2018.
 * This class manages the different export types by providing export methods that automatically call the export method of the Export object
 * for the desired type.
 * To add a custom Export, one must create a class that extends Export and then insert an istance of tht class in this manager by calling addExportType method
 */

public class ExportManager {

    private ArrayList<Export> knownTypes = new ArrayList<>();
    private DataManager dataManager;
    private String outputPath;

    /** Created by Federico Taschin
     *  Istantiate the ExportManager and initialize the known Export types
     * @param dataManager not null, an istance of the current DataManager containing the data to be file
     * @param outputPath not null, path in which to save the file file
     */
    public ExportManager(DataManager dataManager, String outputPath){
        this.dataManager = dataManager;
        this.outputPath = outputPath;
        //Creating known types
        CSVExport csvExport = new CSVExport(dataManager,outputPath);
        ExcelExport excelExport = new ExcelExport(dataManager, outputPath);
        XMLExport xmlExport = new XMLExport(dataManager, outputPath);
        //Adding known types
        addExportType(csvExport);
        addExportType(xmlExport);
        addExportType(excelExport);
    }

    /**
     * Exports the whole database in the location specified in the constructor
     * @author Federico Taschin
     * @param exportTag the tag related to the desired output format
     * @return a list with the ExportedFile objects. If nothing has been file, the list is empty
     * @throws ExportTypeNotSupportedException if the given tag is not known in the ExportManager
     */
    public List<ExportedFile> exportDatabase(String exportTag) throws ExportTypeNotSupportedException {
        Export export = getExportInstance(exportTag);
        Log.d("EXPORTDEBUG","EXPORT ISTANCE OF "+ export.TAG());
        return export.export();
    }

    /**
     * Exports the given mission in the location specified in the constructor
     * @author Federico Taschin
     * @param id the id of the mission to be file
     * @param exportTag the tag related to the desired output format
     * @return the ExportedFile
     * @throws ExportTypeNotSupportedException if the given tag is not known in the ExportManager
     */
    public ExportedFile exportMission(long id, String exportTag) throws ExportTypeNotSupportedException {
        Export export = getExportInstance(exportTag);
        return export.export(id);
    }

    /**
     * Adds a new export type to the manager. This method must be called when one wants to export with a custom Export object
     * If an Export object for the specified TAG already exists, it's silently overwritten
     * @author Federico Taschin
     * @param exportType the Export object that defines how to export in the given format
     */
    public void addExportType(Export exportType){
        if(alreadyExists(exportType.TAG())){
            removeExportType(exportType.TAG());
        }
        knownTypes.add(exportType);
    }

    /**
     * Removes an Export instance with the given tag from the ExportManager
     * @author Federico Taschin
     * @param tag the tag of the instance to be removed
     */
    public void removeExportType(String tag){
        try {
            knownTypes.remove(getExportInstance(tag));
        }catch (ExportTypeNotSupportedException e){
            //This exception should not be thrown since we assume that the tag exists
            e.printStackTrace();
        }
    }

    /**
     * Sets the output path of the ExportManager (for all the export types)
     * @author Federico Taschin
     * @param path a valid path
     */
    public void setOutputPath(String path){
        for (Export export: knownTypes) {
            export.changePath(path);
        }
    }

    /**
     * Gets all the known TAGs of the Export objects
     * @author Federico Taschin
     * @return ArrayList<String> not null with all the known TAGs
     */
    public ArrayList<String> exportTags(){
        ArrayList<String> exportTypes = new ArrayList<>();
        for(Export export : knownTypes){
            exportTypes.add(export.TAG());
        }
        return exportTypes;
    }

    public List<Export> exportTypes(){
        return exportTypes();
    }

    /**
     * Returns an Export object for the given TAG
     * @author Federico Taschin
     * @param TAG the TAG of the Export object needed
     * @return an instance (not null) of the right Export object for the given TAG
     * @throws ExportTypeNotSupportedException if the TAG (and therfore the Export type) is not known
     */
    private Export getExportInstance(String TAG) throws ExportTypeNotSupportedException {
        for(int i = 0; i<knownTypes.size(); i++){
            if(knownTypes.get(i).TAG().equals(TAG)){
                return knownTypes.get(i);
            }
        }
        throw new ExportTypeNotSupportedException(TAG);
    }

    /**
     * Checks if the given tag is already known for the ExportManager
     * @author Federico Taschin
     * @param tag not null
     * @return true if the tag is already known, false otherwise
     */
    private boolean alreadyExists(String tag){
        for(Export export : knownTypes){
            if(export.TAG().equals(tag)){
                return true;
            }
        }
        return false;
    }



}

