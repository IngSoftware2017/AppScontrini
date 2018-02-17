package database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;



/**
 * @author Marco Olivieri (Team 3)
 * Represents the app settings
 * It saves the ocr options to scan the tickets
 *
 * There may be multiple instances of settings
 */

@Entity(tableName = Constants.SETTINGS_TABLE_NAME)
@TypeConverters(Converters.class) // automatic converters for database correct type


public class SettingsEntity {

    @ColumnInfo(name = Constants.SETTINGS_PRIMARY_KEY)
    @PrimaryKey(autoGenerate = true)
    private long ID;
    private int accuracyOCR;
    private boolean automaticCorrectionAmountOCR;
    private boolean searchUpDownOCR;

    @Ignore
    /**
     * Non parametric constructor to use when you don't want set all fields
     */
    public SettingsEntity() {
    }

    /** @author Marco Olivieri
     * Parametric constructor
     *
     * @param accuracyOCR accuracy of the analysis of the OCR
     * @param automaticCorrectionAmountOCR enable automatic correction of the amount
     * @param searchUpDownOCR enable the search upside down
     */
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
     * @param accuracyOCR - not negative
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

