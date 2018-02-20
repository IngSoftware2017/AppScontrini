package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;


import java.io.File;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;
import export.ExportManager;
import export.ExportTypeNotSupportedException;
import export.ExportedFile;

/**
 * Class for the PopUp menu of the Mission's CardView
 *
 * Created by Matteo Mascotto on 13/01/2018.
 */

public class PopUpMissionMenuListener implements PopupMenu.OnMenuItemClickListener {

    private long missionID;
    DataManager DB;
    MissionEntity mission;
    MissionAdapterDB adapterDB;
    Activity activity;

    File defaultOutputPath;
    ExportManager manager;

    /**
     * Constructor of the PopUpMissionMenuListener class
     *
     * @param missionID it contain the ID of the mission where the user tapped
     */
    public PopUpMissionMenuListener(MissionAdapterDB adapterDB, View view, long missionID) {
        this.missionID = missionID;
        this.adapterDB = adapterDB;
        activity = adapterDB.activity;
        DB = new DataManager(adapterDB.activity);
        mission = DB.getMission(missionID);
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
                if(activity ==null){
                    Log.d("DEBUG","CONTEXT NULL");
                }
                Intent editMission = new Intent(activity, EditMission.class);
                editMission.putExtra(IntentCodes.INTENT_MISSION_ID, missionID);
                activity.startActivityForResult(editMission,IntentCodes.MODIFY_MISSION);
                break;

            // Export
            case R.id.export_mission:
                break;

            // Management of the different export type
            case R.id.export_csv:
            case R.id.export_xls:
            case R.id.export_xml:

                defaultOutputPath = activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                manager = new ExportManager(DB, defaultOutputPath.getPath());

                try {
                    ExportedFile exported = manager.exportMission(missionID, activity.getResources().getString(R.string.popup_export_csv));
                    EmailBuilder.createEmail().attachFile(exported.file).sendEmail(activity);
                } catch (ExportTypeNotSupportedException e) {
                    e.printStackTrace();
                }
                break;

            // Close the mission
            case R.id.close_mission:
                mission.setClosed(true);
                DB.updateMission(mission);
                adapterDB.closeMission(mission);
                break;

            // Delete the mission
            case R.id.delete_mission:
                deleteMission();
                break;
        }

        return true;
    }

    /**
     * Delete the mission from the bill viewer (inside the mission)
     *
     * @author: Matteo Mascotto (Using Dal Maso and Lazzarin code)
     */
    public void deleteMission(){

        //Lazzarin
        AlertDialog.Builder toast = new AlertDialog.Builder(activity);

        //Dialog
        toast.setMessage(activity.getString(R.string.deleteMissionToast))
                .setTitle(activity.getString(R.string.deleteTitle));

        //Positive button
        toast.setPositiveButton(activity.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<TicketEntity> list = DB.getTicketsForMission(missionID);
                for(int i = 0; i < list.size(); i++){
                    DB.deleteTicket((int) list.get(i).getID());
                }
                boolean deleted = DB.deleteMission(missionID);
                adapterDB.deleteMission(mission);
            }
        });

        //Negative button
        toast.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing to do
            }
        });

        //Show toast
        AlertDialog alert = toast.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setTextColor(Color.parseColor("#2196F3"));
    }


}
