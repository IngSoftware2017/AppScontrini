package com.example.ingsw.gruppo3;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import database.DataManager;
import database.PersonEntity;

/** Created by Marco Olivieri
 *
 */
public class AddMission extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener{

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
    DatePickerDialog.OnDateSetListener dateListener;
    DatePickerDialog startDatePicker, endDatePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);

        personEntityArrayList = new ArrayList<PersonEntity>();
        personEntityArrayList.add(new PersonEntity());//fake person
        adapter = new AddMissionAdapter(this, R.layout.person_row_custom, personEntityArrayList);

        addPersonButton = findViewById(R.id.addPersonButton);
        addPersonButton.setOnClickListener(this);
        addPerson = findViewById(R.id.addPersonEditText);
        saveMissionButton = findViewById(R.id.saveButton);
        nameMissionText = findViewById(R.id.nameMissionEditText);
        startDateMissionText = findViewById(R.id.startDateEditText);
        namePerson = findViewById(R.id.addPersonEditText);
        endDateMissionText = findViewById(R.id.endMissionEditText);
        personsList = findViewById(R.id.personsList);
        personsList.setAdapter(adapter);
        startDateMissionText.setOnClickListener(this);
        endDateMissionText.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.addPersonButton){
            PersonEntity person = new PersonEntity();
            if(isCorrectCognomePerson()) {
                person.setLastName(addPerson.getText().toString());
                if (MainActivity.dataManager.addPerson(person) > 0) {
                    personEntityArrayList.add(person);
                    adapter.notifyDataSetChanged();
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

        }else if(view.getId() == R.id.startDateEditText){
             startDatePicker = new DatePickerDialog(this, this, 2017, 12, 6);
            startDatePicker.show();
        }else if(view.getId() == R.id.endMissionEditText){
             endDatePicker = new DatePickerDialog(this, this, 2017, 12, 6);
             endDatePicker.show();
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

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        if(datePicker.equals(startDatePicker)){
            startDateMissionText.setText(i+"-"+i1+"-"+i2);
        }else if(datePicker.equals(endDatePicker)){
            endDateMissionText.setText(i+"-"+i1+"-"+i2);
        }
    }
}

