package com.unipd.ingsw.gruppo3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
 * @author Stefano Elardo on 15-12-2017 Implement DataPicker for startMission EditText and endMission EditText
 */

public class AddMission extends AppCompatActivity implements View.OnClickListener{

    private final String DEBUG_TAG = "ADM_DEBUG";

    //Components
    FloatingActionButton saveMissionButton;

    EditText nameMissionText;
    EditText startDateMissionText;
    EditText endDateMissionText;
    AddPersonEditText addPersonaEditText;
    Context context;

    private Calendar calendar;
    private int yearStart, yearEnd, monthStart, monthEnd, dayStart, dayEnd;

    ListView personsList;
    List<PersonEntity> personEntities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_add_mission);

        nameMissionText = findViewById(R.id.nameText);
        /*
        nameMissionText.setOnClickListener(this);
        nameMissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameMissionText.setText("");
                nameMissionText.setEnabled(true);
            }
        });
        */

        //Calendar initialization
        context=AddMission.this;
        calendar = Calendar.getInstance();
        yearStart = calendar.get(Calendar.YEAR);
        monthStart = calendar.get(Calendar.MONTH);
        dayStart = calendar.get(Calendar.DAY_OF_MONTH);
        yearEnd = yearStart;
        monthEnd = monthStart;
        dayEnd = dayStart;



        startDateMissionText = findViewById(R.id.starMissionText);

        startDateMissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(context, startDateListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        endDateMissionText = findViewById(R.id.endMissionText);

        endDateMissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(context, endDateListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


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
     * It control if the Mission data are corrects
     * (not empty for text fields, correct date format for date fields)
     *
     * @author Marco Olivieri on 03/12/2017 (Team 3)
     * @author Matteo Mascotto on 07-12-2017
     *
     * @return the Object where there is an error, null otherwise
     */
    private Object checkCorrectField(){

        String dateInputStart, dateInputEnd;

        if (nameMissionText.getText().toString().equals("")) return nameMissionText;
        if (startDateMissionText.getText().toString().equals("")) return startDateMissionText;
        if (endDateMissionText.getText().toString().equals("")) return endDateMissionText;
        if (addPersonaEditText.getText().toString().equals("")) return addPersonaEditText;

        //Not needed checks with Date Picker
        /*
        dateInputStart = startDateMissionText.getText().toString();
        if (dateInput.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}") || dateInput.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            return startDateMissionText;
        }

        dateInputEnd = endDateMissionText.getText().toString();
        if (dateInput.matches("[0-9]{2}-[0-9]{2}-[0-9]{4}") || dateInput.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            return endDateMissionText;
        }

        // Compare the beginDate and endDate of the Mission to check if it contain a correct interval
        if (dateInput.compareTo(startDateMissionText.getText().toString()) > 0) {
            return endDateMissionText;
        }
        */
        return null;
    }


    /**
     * @author Stefano Elardo
     * Method that check if end date is temporally after start date
     * @return the result of the confrontation between dates
     */
    private boolean checkCorrectInterval(){
        String dateInputStart = startDateMissionText.getText().toString();
        String dateInputEnd = endDateMissionText.getText().toString();
        return (dateInputStart.compareTo(dateInputEnd)<0);
    }


    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    yearStart = calendar.get(Calendar.YEAR);
                    monthStart = calendar.get(Calendar.MONTH)+1;
                    dayStart = calendar.get(Calendar.DAY_OF_MONTH);
                    //updateLabel();
                    showDate(0,yearStart,monthStart,dayStart);
                }
    };

    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            yearEnd = calendar.get(Calendar.YEAR);
            monthEnd = calendar.get(Calendar.MONTH)+1;
            dayEnd = calendar.get(Calendar.DAY_OF_MONTH);
            showDate(1,yearEnd,monthEnd,dayEnd);
        }
    };
    /**
     * More precise way to set the EditText, needs improvement, will be implemented soon
     */
    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALY);

        startDateMissionText.setText(sdf.format(calendar.getTime()));
    }

    /**
     * Old version of updateLabel, manually set the EditText building a string
     * @param choice flag to understand who call the method TODO find a better alternative
     * @param year year to insert
     * @param month month to insert
     * @param day day to insert
     */
    private void showDate(int choice,int year, int month, int day) {
        String str=""+day+"/"+month+"/"+year;
        if(choice==0)
            startDateMissionText.setText(str);
        else
            endDateMissionText.setText(str);
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
            }else if (!checkCorrectInterval()){
                showErrorDialog("Invalid date interval, missions should end after or the same day of begins");
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
