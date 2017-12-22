package com.unipd.ingsw.gruppo3;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
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
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import database.Constants;
import database.DataManager;
import database.Database;
import database.MissionEntity;
import database.PersonEntity;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Marco Olivieri on 03/12/2017
 *
 * Modify: Implement the method checkCorrectField(), remove the auto setting of empty text to the Items
 * @author Matteo Mascotto on 07-12-2017
 *
 * Modify: Use Calendar (DatePicker) to set start and end of the Mission
 * @author Matteo Mascotto on 15-12-2017
 *
 * Modify: Implement of the person management
 * @author Matteo Mascotto on 16-12-2017
 */

public class AddMission extends AppCompatActivity implements View.OnClickListener{

    private final String DEBUG_TAG = "ADM_DEBUG";

    //Components
    FloatingActionButton saveMissionButton;
    FloatingActionButton newPersonButton;

    EditText nameMissionText;
    EditText startDateMissionText;
    EditText endDateMissionText;
    //AddPersonEditText addPersonaEditText;
    Spinner personsSpinner;
    //ListView personsList;
    List<PersonEntity> personEntities;
    TextView noticeEmptyText;

    int personID;

    //Initialization of an user Person who will own every mission without associated Person
    PersonEntity user = new PersonEntity("user","user","");
    MissionEntity missionEntity = new MissionEntity();


    /**
     * It sets the listener to the all AddMission objects and manage the calendar show for make
     * faster the date insert
     * @author: Matteo Mascotto
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_add_mission);

        DataManager.getInstance(this).addPerson(user);
        missionEntity.setPersonID(user.getID());
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
                            // The month coding begin from 0 to 11, so to have the right month number is necessary + 1
                            selectedMonth = selectedMonth + 1;
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
                            // The month coding begin from 0 to 11, so to have the right month number is necessary + 1
                            selectedMonth = selectedMonth + 1;
                            endDateMissionText.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                        }
                    },mYear, mMonth, mDay);
                    mDatePicker.setTitle("Data Fine Missione");
                    mDatePicker.show();
                }
            }
        });

        // Persona
        //addPersonaEditText = findViewById(R.id.addPersonaEditText);

        // Lista Persone salvate
        //personsList = findViewById(R.id.personsList);

        // Message for No Mission
        noticeEmptyText = findViewById(R.id.emptyNoticeTextView);

        personEntities = DataManager.getInstance(this).getAllPerson();
        //PersonAdapter adapter = new PersonAdapter(this, R.layout.persona_row_item, personEntities);
        //personsList.setAdapter(adapter);
        //checkInitialization();

        // Botton SAVE
        saveMissionButton = findViewById(R.id.saveButton);
        saveMissionButton.setOnClickListener(this);
    }

    /**
     * Initialize of activity
     * Check if there are some persons and show a message
     * @author Matteo Mascotto
     */
/*    private void checkInitialization(){
        if(personsList.getAdapter().getCount()==0){
            //noticeEmptyText.setEnabled(Boolean.TRUE);
            personsList.setEnabled(Boolean.FALSE);
        }
    }
*/
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
        // if (addPersonaEditText.getText().toString().equals("")) return addPersonaEditText;

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

    /**
     * It manage the all click that are done, inside there is a management of the different object clicked
     * @author Matteo Mascotto
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        //Action after activate the request to add a Person
        if (view.getId()== newPersonButton.getId()){
            Intent callAddPersonActivity = new Intent(this,AddPerson.class);
            startActivity(callAddPersonActivity);
        }

        // Action after activate the request to Save the Mission
        if (view.getId() == saveMissionButton.getId()) {
            //Log.d(DEBUG_TAG, "EDIT TEXT:" + addPersonaEditText.getText() + ".");

            if (checkCorrectField() == nameMissionText) {
                Toast.makeText(this, R.string.missionErrorMessage, Toast.LENGTH_LONG).show();
            } else if (checkCorrectField() == startDateMissionText) {
                Toast.makeText(this, R.string.startErrorMessage, Toast.LENGTH_LONG).show();
            } else if (checkCorrectField() == endDateMissionText) {
                Toast.makeText(this, R.string.endErrorMessage, Toast.LENGTH_LONG).show();
            //} else if (checkCorrectField() == addPersonaEditText) {
               // Toast.makeText(this, R.string.personErrorMessage, Toast.LENGTH_LONG).show();
            } else {

                //missionEntity = new MissionEntity();
/*
                // If the person it's already present it assign it to the Mission, otherwise it create a new one
                // lastName and academicTitle are not yet implemented
                if (addPersonaEditText.getPersonEntity() == null) {
                    String newPerson = addPersonaEditText.getText().toString();
                    PersonEntity personEntity = new PersonEntity(newPerson, "", "");
                    DataManager.getInstance(this).addPerson(personEntity);
                }


                if (addPersonaEditText.getPersonEntity() != null) {
                    missionEntity.setPersonID(addPersonaEditText.getPersonEntity().getID());
                }

*/
                //missionEntity.setPersonID(addPersonaEditText.getPersonEntity().getID());
                missionEntity.setName(nameMissionText.getText().toString());
                DataManager.getInstance(this).addMission(missionEntity);

                Intent callBillActivity = new Intent(this, BillActivityGruppo1.class);
                callBillActivity.putExtra(IntentCodes.MISSION_OBJECT, missionEntity);
                startActivity(callBillActivity);
            }
/*
                if(addPersonaEditText.getText().toString().equals("")){
                    showErrorDialog("Inserire o selezionare una persona");
                }
                /*else{
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
     * @author Marco Olivieri
     *
     * @param errorMessage it contains the error message to write in the Toast
     */
    public void showErrorDialog(String errorMessage){
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d(DEBUG_TAG,"Toast displayed");
    }
}
