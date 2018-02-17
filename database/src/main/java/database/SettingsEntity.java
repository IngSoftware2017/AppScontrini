package database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;



/**
 * Represents the app settings
 * It saves the ocr options to scan the tickets
 * @author Marco Olivieri (Team 3)
 */

@Entity(tableName = Constants.SETTINGS_TABLE_NAME)
@TypeConverters(Converters.class) // automatic converters for database correct type


public class SettingsEntity {

    private int accuracyOCR;
    private boolean automaticCorrectionAmountOCR;
    private boolean searchUpDownOCR;

    public SettingsEntity(int accuracyOCR, boolean automaticCorrectionAmountOCR, boolean searchUpDownOCR) {
        this.accuracyOCR = accuracyOCR;
        this.automaticCorrectionAmountOCR = automaticCorrectionAmountOCR;
        this.searchUpDownOCR = searchUpDownOCR;
    }

    /** @author Marco Olivieri
     * Returns the accuracy of OCR
     * @return int - accuracy
     */
    public int getAccuracyOCR() {
        return accuracyOCR;
    }

    /** @author Marco Olivieri
     * Sets the accuracy of OCR
     * @param accuracyOCR not negative
     */
    public void setAccuracyOCR(int accuracyOCR) {
        this.accuracyOCR = accuracyOCR;
    }

    /** @author Marco Olivieri
     * Returns if there is the automatic correction of amount by OCR
     * @return boolean - automatic correction
     */
    public boolean isAutomaticCorrectionAmountOCR() {
        return automaticCorrectionAmountOCR;
    }

    /** @author Marco Olivieri
     * Sets if there is the automatic correction of amount by OCR
     * @param automaticCorrectionAmountOCR
     */
    public void setAutomaticCorrectionAmountOCR(boolean automaticCorrectionAmountOCR) {
        this.automaticCorrectionAmountOCR = automaticCorrectionAmountOCR;
    }

    /** @author Marco Olivieri
     * Returns if there is the search upside down by OCR
     * @return boolean - search upside down
     */
    public boolean isSearchUpDownOCR() {
        return searchUpDownOCR;
    }

    /** @author Marco Olivieri
     * Sets if there is the search upside down by OCR
     * @param searchUpDownOCR
     */
    public void setSearchUpDownOCR(boolean searchUpDownOCR) {
        this.searchUpDownOCR = searchUpDownOCR;
    }
}

