package com.unipd.ingsw.gruppo3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import database.Constants;
import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

/**
 * Created by Marco Olivieri on 03/12/2017
 *
 * Modify: Implement the method checkCorrectField(), remove the auto setting of empty text to the Items
 * @author Matteo Mascotto on 07-12-2017
 *
 * Modify: Use Calendar (DatePicker) to set start and end of the Mission
 * @author Matteo Mascotto on 15-12-2017
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

        // Nome Missione
        nameMissionText = findViewById(R.id.nameText);
        nameMissionText.setOnClickListener(this);

        // Data Inizio Missione
        startDateMissionText = findViewById(R.id.starMissionText);
        startDateMissionText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            int mYear, mMonth, mDay;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar currentDate = Calendar.getInstance();

                    mYear = currentDate.get(Calendar.YEAR);
                    mMonth = currentDate.get(Calendar.MONTH);
                    mDay = currentDate.get(Calendar.DAY_OF_MONTH);

                    // Show the calendar, with the onDateSet return
                    DatePickerDialog mDatePicker = new DatePickerDialog(AddMission.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                            startDateMissionText.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                        }
                    },mYear, mMonth, mDay);
                    mDatePicker.setTitle("Data Inizio Missione");
                    mDatePicker.show();
                }
            }
        });


        // Data Fine Missione
        endDateMissionText = findViewById(R.id.endMissionText);
        endDateMissionText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            int mYear, mMonth, mDay;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar currentDate = Calendar.getInstance();

                    mYear = currentDate.get(Calendar.YEAR);
                    mMonth = currentDate.get(Calendar.MONTH);
                    mDay = currentDate.get(Calendar.DAY_OF_MONTH);

                    // Show the calendar, with the onDateSet return
                    DatePickerDialog mDatePicker = new DatePickerDialog(AddMission.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                            endDateMissionText.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                        }
                    },mYear, mMonth, mDay);
                    mDatePicker.setTitle("Data Fine Missione");
                    mDatePicker.show();
                }
            }
        });

        // Persona
        addPersonaEditText =findViewById(R.id.addPersonaEditText);
        /*
        addPersonaEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPersonaEditText.setText("");
            }
        });
        */
        personsList = findViewById(R.id.personsList);

        saveMissionButton = findViewById(R.id.saveButton);
        saveMissionButton.setOnClickListener(this);

        /* Matteo Mascotto - TO-DO: Improve different code for the autocomplete of the Person
            this code cause problem with the layout addMissionPage
        personEntities = DataManager.getInstance(this).getAllPerson();
        PersonAdapter adapter = new PersonAdapter(this, R.layout.persona_row_item, personEntities);
        personsList.setAdapter(adapter);
        */
    }

    /**
     * It control if the Mission data are corrects.
     * If it is not will return the object where we do the check, null otherwise
     * (not empty for text fields, correct date format for date fields)
     *
     * @author Marco Olivieri on 03/12/2017 (Team 3)
     * @author Matteo Mascotto on 07-12-2017
     *
     * @return the Object where there is an error, null otherwise
     */
    private Object checkCorrectField(){

        String dateEndMission;

        if (nameMissionText.getText().toString().equals("")) return nameMissionText;
        if (startDateMissionText.getText().toString().equals("")) return startDateMissionText;
        if (endDateMissionText.getText().toString().equals("")) return endDateMissionText;
        if (addPersonaEditText.getText().toString().equals("")) return addPersonaEditText;

        /* TODO: This control isn't correct, find a mode to check the correct date format
        dateInput = startDateMissionText.getText().toString();
        if (dateInput.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}") || dateInput.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            return startDateMissionText;
        }

        dateInput = endDateMissionText.getText().toString();
        if (dateInput.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}") || dateInput.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            return endDateMissionText;
        }

        dateEndMission = endDateMissionText.getText().toString();
        // Compare the startDate and endDate of the Mission to check if it contain a correct interval
        if (dateEndMission.compareTo(startDateMissionText.getText().toString())  0) {
            return endDateMissionText;
        }*/

        return null;
    }

    @Override
    public void onClick(View view) {
        // Action after activate the request to Save the Mission
        if (view.getId() == saveMissionButton.getId()) {
            Log.d(DEBUG_TAG, "EDIT TEXT:" + addPersonaEditText.getText() + ".");

            if (checkCorrectField() == nameMissionText) {
                showErrorDialog("Inserire un valore corretto di Missione");
            } else if (checkCorrectField() == startDateMissionText) {
                showErrorDialog("Inserire un valore corretto di data di inizio Missione");
            } else if (checkCorrectField() == endDateMissionText) {
                showErrorDialog("Inserire un valore corretto di data di fine Missione");
            } else if (checkCorrectField() == addPersonaEditText) {
                showErrorDialog("Inserire o selezionare una persona");
            } else {

                MissionEntity missionEntity = new MissionEntity();

                if (addPersonaEditText.getPersonEntity() != null) {
                    missionEntity.setPersonID(addPersonaEditText.getPersonEntity().getID());
                }

                missionEntity.setName(nameMissionText.getText().toString());
                DataManager.getInstance(this).addMission(missionEntity);
                Intent callBillActivity = new Intent(this, BillActivityGruppo1.class);
                callBillActivity.putExtra(IntentCodes.MISSION_OBJECT, missionEntity);
                startActivity(callBillActivity);
            }
    /*
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
            */
        }
    }

    /**
     * Lunch the error message by Toast
     *
     * @param errorMessage it contains the error message to write in the Toast
     */
    public void showErrorDialog(String errorMessage){
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d(DEBUG_TAG,"Toast displayed");
    }
}
