package com.ing.software.ticketapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

public class MissionActivity extends AppCompatActivity {

    public DataManager DB;
    int personID;
    PersonEntity thisPerson;
    public List<MissionEntity> listMission = new LinkedList<MissionEntity>();
    Context context;
    final int MISSION_MOD = 1;
    final int PERSON_MOD = 2;

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB = new DataManager(this.getApplicationContext());
        context = getApplicationContext();

        Intent intent = getIntent();
        personID = intent.getExtras().getInt("personID");
        thisPerson = DB.getPerson(personID);

        setContentView(R.layout.activity_mission);
        //TODO set the title right using string.xml(like personName+" 's missions")
        setTitle(thisPerson.getName());

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), AddNewMission.class);
                addMission.putExtra("person", personID);
                Log.d("PersonID", ""+personID);
                startActivityForResult(addMission, 1);
            }
        });
        printAllMissions();
    }
    /** Dal Maso
     * Setting toolbar delete button and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.person_menu, menu);
        return true;
    }

    /** Dal Maso
     * Edit by Lazzarin
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case (R.id.action_deletePerson):
                deletePerson();
                break;

            case (R.id.action_editPerson):
                //Open Edit Person Activity
                Intent editPerson = new Intent(context, EditPerson.class);
                editPerson.putExtra("personID", personID);
                startActivityForResult(editPerson,PERSON_MOD);
                break;

            default:
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso
     * Catch intent results
     * @param requestCode action number
     * @param resultCode intent result code
     * @param data intent data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result", ""+requestCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (MISSION_MOD):
                    clearAllMissions();
                    printAllMissions();
                    break;

                case (PERSON_MOD):
                    thisPerson = DB.getPerson(personID);
                    setTitle(thisPerson.getName());
                    break;

                default:
                    clearAllMissions();
                    printAllMissions();
                    break;
            }
        }
    }

    /** PICCOLO
     * Adds in the database the new mission
     * @param mission the mission to be added
     */
    public void addToListDB(MissionEntity mission){
        listMission.add(mission);
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterDB adapter = new MissionAdapterDB(this, R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    /**Lazzarin
     * clear the view after I've eliminated a mission(before to call printAllMissions)
     */
    public void clearAllMissions()
    {
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterDB emptyAdapter = new MissionAdapterDB(this, R.layout.mission_card, listMission);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     */
    public void printAllMissions(){
        List<MissionEntity> missions = DB.getMissionsForPerson(personID);
        TextView noMissions = (TextView)findViewById(R.id.noMissions);
        for (int i = 0; i < missions.size(); i++)
        {
            addToListDB(missions.get(i));
        }
        if(missions.size() == 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            noMissions.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Mantovan Federico
     *
     * Delete the person and the missions\tickets associated with it
     */
    public void deletePerson(){
        AlertDialog.Builder toast = new AlertDialog.Builder(MissionActivity.this);
        //Dialog
        toast.setMessage(context.getString(R.string.delete_person))
                .setTitle(context.getString(R.string.delete_title_person));
        //Positive button
        toast.setPositiveButton(context.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
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
