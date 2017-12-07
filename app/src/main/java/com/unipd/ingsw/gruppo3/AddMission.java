package com.unipd.ingsw.gruppo3;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

/**
 * Created by Marco Olivieri on 03/12/2017
 */

public class AddMission extends AppCompatActivity implements View.OnClickListener{
    private final String DEBUG_TAG = "ADM_DEBUG";
    //Components
    FloatingActionButton saveMissionButton;
    EditText nameMissionText;
    EditText startDateMissionText;
    EditText endDateMissionText;
    AddPersonEditText addPersonaEditText;
    ListView personsList;
    List<PersonEntity> personEntities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_add_mission);

        nameMissionText = findViewById(R.id.nameText);
        nameMissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameMissionText.setText("");
            }
        });
        startDateMissionText = findViewById(R.id.starMissionText);
        startDateMissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateMissionText.setText("");
            }
        });
        endDateMissionText = findViewById(R.id.endMissionText);
        endDateMissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDateMissionText.setText("");
            }
        });
        addPersonaEditText =findViewById(R.id.addPersonaEditText);
        addPersonaEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPersonaEditText.setText("");
            }
        });
        personsList = findViewById(R.id.personsList);

        saveMissionButton = findViewById(R.id.saveButton);
        saveMissionButton.setOnClickListener(this);

        personEntities = DataManager.getInstance(this).getAllPerson();
        PersonAdapter adapter = new PersonAdapter(this, R.layout.persona_row_item, personEntities);
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
        if(view.getId()==saveMissionButton.getId()){
            Log.d(DEBUG_TAG,"EDIT TEXT:"+addPersonaEditText.getText()+".");
            if(addPersonaEditText.getText().toString().equals("")){
                showErrorDialog("Inserire o selezionare una persona");
            }else{
                MissionEntity missionEntity = new MissionEntity();
                if(addPersonaEditText.getPersonEntity()==null){
                    PersonEntity personEntity = new PersonEntity();
                    DataManager.getInstance(this).addPerson(personEntity);
                    missionEntity.setPersonID(personEntity.getID());
                }else{
                    missionEntity.setPersonID(addPersonaEditText.getPersonEntity().getID());
                }
                missionEntity.setName(nameMissionText.getText().toString());
                DataManager.getInstance(this).addMission(missionEntity);
                Intent callBillActivity = new Intent(this, BillActivityGruppo1.class);
                callBillActivity.putExtra(IntentCodes.MISSION_OBJECT,missionEntity);
                startActivity(callBillActivity);
            }
        }
    }

    public void showErrorDialog(String errorMessage){
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d(DEBUG_TAG,"Toast displayed");
    }
}
