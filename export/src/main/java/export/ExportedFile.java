package export;

import java.io.File;

/**
 * Created by Federico Taschin on 09/01/2018.
 * This is a container class which holds the file file and the description of its content
 */

public class ExportedFile {

    public enum FILE_CONTENT{
        PERSONS, MISSIONS, TICKETS, SINGLE_MISSION, DATABASE;
    }

    public File file;
    public FILE_CONTENT content;

    public ExportedFile(File file, FILE_CONTENT content) {
        this.file = file;
        this.content = content;
    }

    public ExportedFile(){}
}
