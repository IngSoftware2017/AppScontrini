package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;


/**
 * Created by Stefano Elardo on 20/02/2018.
 * Using Matteo Mascotto code
 */

public class PopUpPersonMenuListener implements PopupMenu.OnMenuItemClickListener {
    private long personID;
    DataManager DB;
    PersonEntity person;
    PeopleAdapter adapterPerson;
    Activity activity;


    /**
     * Constructor of the PopUpMissionMenuListener class
     * @param adapterPerson instance of the adapter used for persons
     * @param view view passed
     * @param personID it contain the ID of the person where the user tapped
     */
    public PopUpPersonMenuListener(PeopleAdapter adapterPerson, View view, long personID) {
        this.personID = personID;
        this.adapterPerson = adapterPerson;
        activity = adapterPerson.activity;
        DB = new DataManager(adapterPerson.activity);
        person = DB.getPerson(personID);
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
            case R.id.modify_person:
                if(activity ==null){
                    Log.d("DEBUG","CONTEXT NULL");
                }
                Intent editPerson = new Intent(activity, EditPerson.class);
                editPerson.putExtra(IntentCodes.INTENT_PERSON_ID, personID);
                activity.startActivityForResult(editPerson,IntentCodes.MODIFY_PERSON);
                break;


            // Delete the person
            case R.id.delete_person:
                deletePerson();
                break;
        }

        return true;
    }

    /**
     * @author Stefano Elardo using Mantovan code
     * Delete the person and the missions\tickets associated with it
     */
    public void deletePerson(){
        AlertDialog.Builder toast = new AlertDialog.Builder(activity);
        //Dialog
        toast.setMessage(activity.getString(R.string.delete_person))
                .setTitle(activity.getString(R.string.delete_title_person));
        //Positive button
        toast.setPositiveButton(activity.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<MissionEntity> listMission = DB.getMissionsForPerson(personID);
                for(int i = 0; i < listMission.size(); i++){
                    List<TicketEntity> listTicket = DB.getTicketsForMission((int) listMission.get(i).getID());
                    for(int j = 0; j < listTicket.size(); j++){
                        DB.deleteTicket((int) listTicket.get(j).getID());
                    }
                }
                DB.deleteMission(personID);
                DB.deletePerson(personID);
                adapterPerson.deletePerson(person);
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
