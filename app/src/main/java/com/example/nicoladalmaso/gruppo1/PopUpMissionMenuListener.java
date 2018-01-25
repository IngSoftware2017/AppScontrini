package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
 * Created by matteo.mascotto on 13/01/2018.
 */

public class PopUpMissionMenuListener extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private int missionID;
    DataManager DB;
    MissionEntity mission;
    final Context context;

    File defaultOutputPath;
    ExportManager manager;

    /**
     * Constructor of the PopUpMissionMenuListener class
     *
     * @param missionID it contain the ID of the mission where the user tapped
     */
    public PopUpMissionMenuListener(View view, int missionID) {
        this.missionID = missionID;
        this.context = view.getContext();

        DB = new DataManager(context);
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
                Intent editMission = new Intent(this.context, EditMission.class);
                editMission.putExtra(IntentCodes.INTENT_MISSION_ID, missionID);
                //startActivity(editMission);
                // TODO Catch the error on startActivity: it make me CRAZY!
                break;

            // Export
            case R.id.export_mission:
                break;

            // Management of the different export type
            case R.id.export_csv:
            case R.id.export_xls:
            case R.id.export_xml:

                defaultOutputPath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                manager = new ExportManager(DB, defaultOutputPath.getPath());

                try {
                    ExportedFile exported = manager.exportMission(missionID, context.getResources().getString(R.string.popup_export_csv));
                    EmailBuilder.createEmail().attachFile(exported.file).sendEmail(context);
                } catch (ExportTypeNotSupportedException e) {
                    e.printStackTrace();
                }
                break;

            // Close the mission
            case R.id.close_mission:
                mission.setRepay(true);
                DB.updateMission(mission);
                finish();
                // TODO find an alternative method to reload the activity. finish() it doesn't do it
                break;

            // Delete the mission
            case R.id.delete_mission:
                deleteMission();
                finish();
                // TODO find an alternative method to reload the activity. finish() it doesn't do it
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
        AlertDialog.Builder toast = new AlertDialog.Builder(context);

        //Dialog
        toast.setMessage(context.getString(R.string.deleteMissionToast))
                .setTitle(context.getString(R.string.deleteTitle));

        //Positive button
        toast.setPositiveButton(context.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<TicketEntity> list = DB.getTicketsForMission(missionID);
                for(int i = 0; i < list.size(); i++){
                    DB.deleteTicket((int) list.get(i).getID());
                }
                DB.deleteMission(missionID);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        //Negative button
        toast.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
