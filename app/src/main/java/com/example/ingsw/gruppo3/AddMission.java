package com.example.ingsw.gruppo3;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

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
    EditText namePerson;
    ListView personsList;
    ArrayList<PersonEntity> personEntityArrayList;
    AddMissionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);

        personEntityArrayList = new ArrayList<PersonEntity>();
        personEntityArrayList.add(new PersonEntity());//fake person
        adapter = new AddMissionAdapter(this, R.layout.person_row_custom, personEntityArrayList);

        saveMissionButton = findViewById(R.id.saveButton);
        cancelMissionButton = findViewById(R.id.deleteButton);
        nameMissionText = findViewById(R.id.nameText);
        startDateMissionText = findViewById(R.id.starMissionText);
        namePerson = findViewById(R.id.addPersonEditText);
        endDateMissionText = findViewById(R.id.endMissionText);
        personsList = findViewById(R.id.personsList);
        personsList.setAdapter(adapter);
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

    }

    /**
     * Tests if the user wrote something
     * @return boolean
     */
    private boolean isCorrectCognomePerson(){
        if(namePerson.getText().equals("Cognome"))
            return false;
        return true;
    }
}
