package com.example.ingsw.gruppo3;


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
    FloatingActionButton cancelMissionButton;
    FloatingActionButton addPersonButton;
    EditText nameMissionText;
    EditText startDateMissionText;
    EditText endDateMissionText;
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

        addPerson = findViewById(R.id.addPersonEditText);
        saveMissionButton = findViewById(R.id.saveButton);
        cancelMissionButton = findViewById(R.id.deleteButton);
        nameMissionText = findViewById(R.id.nameText);
        startDateMissionText = findViewById(R.id.starMissionText);
        endDateMissionText = findViewById(R.id.endMissionText);
        personsList = findViewById(R.id.personsList);
        personsList.setAdapter(adapter);
        dataManager = (DataManager) getIntent().getSerializableExtra(IntentCodes.DATAMANAGER_INTENT_CODE);
    }

    /**
     * @author Marco Olivieri on 03/12/2017 (Team 3)
     * return boolean - if setting field is ok
     */
    private boolean checkCorrectField(){
        return false;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.addPersonButton){
            PersonEntity person = new PersonEntity();
            person.setLastName(addPerson.getText().toString());
            if(dataManager.addPerson(person)>0){
                personEntityArrayList.add(person);
            }
        }else if(view.getId() == R.id.saveButton){

        }

    }
}
