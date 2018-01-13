package com.example.nicoladalmaso.gruppo1;

import android.view.MenuItem;
import android.widget.PopupMenu;

/**
 * Class for the PopUp menu of the Mission's CardView
 *
 * Created by matteo.mascotto on 13/01/2018.
 */

public class PopUpMissionMenuListener implements PopupMenu.OnMenuItemClickListener {

    private int position;

    /**
     * Constructor of the PopUpMissionMenuListener class
     *
     * @param positon it contain the position where the user tapped into the menu voices list
     */
    public PopUpMissionMenuListener(int positon) {
        this.position = positon;
    }

    /**
     * Event for manage the chose of the menu voice
     *
     * @param menuItem item where the user pressed
     * @return boolean value for check the correct operations
     */
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // Open modify activity
            case R.id.modify_mission:
                // TODO Open the activity EditMission

            // Export
            case R.id.export_mission:
                // TODO Choose if use a submenu or a pop-up for the format choise

            // Close the mission
            case R.id.close_mission:
                // TODO Make the mission as Close

            // Delete the mission
            case R.id.delete_mission:
                // TODO Delete the mission and its tickets

            default:

        }
        return false;
    }
}
