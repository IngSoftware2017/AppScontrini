package com.example.ingsw.gruppo3;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import database.DataManager;
import database.PersonEntity;

/** Created by Marco Olivieri
 *
 */
public class AddMission extends AppCompatActivity implements View.OnClickListener{

    //Components
    FloatingActionButton saveMissionButton;
    FloatingActionButton addPersonButton;
    EditText nameMissionText;
    EditText startDateMissionText;
    EditText endDateMissionText;
    EditText namePerson;
    EditText addPerson;
    ListView personsList;
    ArrayList<PersonEntity> personEntityArrayList;
    AddMissionAdapter adapter;
    DataManager dataManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);

        personEntityArrayList = new ArrayList<PersonEntity>();
        personEntityArrayList.add(new PersonEntity());//fake person
        adapter = new AddMissionAdapter(this, R.layout.person_row_custom, personEntityArrayList);

        addPersonButton = findViewById(R.id.addPersonButton);
        addPerson = findViewById(R.id.addPersonEditText);
        saveMissionButton = findViewById(R.id.saveButton);
        nameMissionText = findViewById(R.id.nameText);
        startDateMissionText = findViewById(R.id.starMissionText);
        namePerson = findViewById(R.id.addPersonEditText);
        endDateMissionText = findViewById(R.id.endMissionText);
        personsList = findViewById(R.id.personsList);
        personsList.setAdapter(adapter);
        dataManager = (DataManager) getIntent().getSerializableExtra(IntentCodes.DATAMANAGER_INTENT_CODE);
    }



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.addPersonButton){
            PersonEntity person = new PersonEntity();
            if(isCorrectCognomePerson()) {
                person.setLastName(addPerson.getText().toString());
                if (dataManager.addPerson(person) > 0) {
                    personEntityArrayList.add(person);
                }
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Attenzione!");
                alertDialog.setMessage("Inserire un cognome valido");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        }else if(view.getId() == R.id.saveButton){
            if(checkCorrectField()){
                //campi ok
            }
            else{
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Attenzione!");
                alertDialog.setMessage("Campi obbligatori");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

        }

    }
    /**
     * Tests if the user wrote something
     * @return boolean
     */
    private boolean isCorrectCognomePerson(){
        if(namePerson.getText().equals("Cognome")|| namePerson.getText().equals(""))
            return false;
        return true;
    }

    /**
     * @author Marco Olivieri on 03/12/2017 (Team 3)
     * return boolean - if setting field is ok
     */
    private boolean checkCorrectField(){
        if(nameMissionText.getText().equals("Nome")||startDateMissionText.getText().equals("Data Inizio")||endDateMissionText.getText().equals("Data Fine")||
                nameMissionText.getText().equals("")||startDateMissionText.getText().equals("")||endDateMissionText.getText().equals(""))
            return false;
        return true;
    }
}

