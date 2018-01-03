package export;

import database.DataManager;
import database.Database;

/**
 * @author Marco Olivieri on 03/01/2018
 *
 * This abstract class defines methods to export datas from database
 */

public abstract class ExportOperation {
     String pathDirectory;
     String formatFile;
     Database database;

     /**
      * @author Marco Olivieri
      * Chooses the work Directory to save the exportation
      * @param path, String - the path of directory
      * @return boolean - if it's all ok
      */
     boolean chooseDirectory(String path){
          pathDirectory = path;
          //check in exist
          return false;
     }

     void chooseFormatFile(FormatFile value){
          formatFile = value.toString();
     }


     void chooseDatabase(Database d){
          database = d;
     }
}
