package export;

/**Created by Federico Taschin
 * Exception that is thrown if one tries to export in an unknown format (that is an Export object with an Unknown tag)
 */
public class ExportTypeNotSupportedException extends Exception{
    private static String errorMessage1 = "No export type found for tag ";
    private static String getErrorMessage2 = "\nMake sure to use addExportType(Export exportType) before trying to export with a custom class";

    public ExportTypeNotSupportedException(String tag) {
        super(errorMessage1 + tag + getErrorMessage2);
    }
}
