package database;

import java.util.Date;

/**
 * Represents one mission and its associated information
 * @author Marco Olivieri on 26/11/2017 (Team 3)
 */

public class Mission {

    private int ID;
    private Date startMission;
    private Date endMission;
    private String locality;
    private boolean isRepay;

    /**
     * Non parametric constructor
     */
    public Mission() {
    }

    /**
     * Parametric constructor
     *
     * @param ID Unique ID of the Mission
     * @param startMission Date of the beginning of the mission
     * @param endMission Date of the end of the mission
     * @param locality Name of locality where the mission took place
     */
    public Mission(int ID, Date startMission, Date endMission, String locality) {
        this.ID = ID;
        this.startMission = startMission;
        this.endMission = endMission;
        this.locality = locality;
        isRepay = false;
    }
}

