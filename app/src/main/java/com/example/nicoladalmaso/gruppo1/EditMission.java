package com.example.nicoladalmaso.gruppo1;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import database.DataManager;
import database.MissionEntity;

import static android.provider.Settings.System.DATE_FORMAT;

/**
 * Created by Francesco on 03/01/2018.
 *
 * Modified: Implement the all methods
 * @author matteo.mascotto on 04/01/2018
 */
public class EditMission extends AppCompatActivity{

    public DataManager DB;
    Context context;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    AppUtilities appUtilities = new AppUtilities();

    final String START_DATEPICKER_TAG = "startDatePicker";
    final String FINISH_DATEPICKER_TAG = "finishDatePicker";
    final int MISSION_MOD = 1;

    int missionID;
    MissionEntity thisMission;
    String missionTitle = "", missionDateStart = "", missionDateFinish = "", missionLocation = "";
    TextView txtTitle;
    TextView txtDateStart;
    TextView txtDateFinish;
    TextView txtLocation;

    /**
     * Set the values at the opening of the EditMissin Activity
     * @author matteo.mascotto
     *
     * @param savedInstanceState It contains the most recent data of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);

        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        setTitle(getResources().getString(R.string.editMission));
        setContentView(R.layout.activity_edit_mission);

        initializeComponents();

        // Get data from parent view
        setMissionValues();
    }

    /**
     * Inizialization fo the object into the Activity
     * @author matteo.mascotto
     */
    private void initializeComponents(){
        txtDateStart = findViewById(R.id.input_missionStart);
        txtDateFinish = findViewById(R.id.input_missionFinish);
        LinearLayout bntMissionStart = findViewById(R.id.button_missionStart);
        LinearLayout bntMissionFinish = findViewById(R.id.button_missionFinish);

        bntMissionStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtDateStart);
                newFragment.show(getFragmentManager(), START_DATEPICKER_TAG);
            }
        });

        bntMissionFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtDateFinish);
                newFragment.show(getFragmentManager(), FINISH_DATEPICKER_TAG);
            }
        });
    }

    /**
     * Setting toolbar buttons and style from /res/menu
     * @author matteo.mascotto
     *
     * @param menu menu to set at the toolbar
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editmission_menu, menu);
        return true;
    }

    /**
     * Catch events on toolbar, Edit the Mission
     * @author matteo.mascotto
     *
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent();

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_editMission:
                // Extract data from EditTexts
                EditText editTitle = findViewById(R.id.input_missionName);
                EditText editLocation = findViewById(R.id.input_missionLocation);
                TextView txtDateStart = findViewById(R.id.input_missionStart);
                TextView txtDateFinish = findViewById(R.id.input_missionFinish);

                missionTitle = editTitle.getText().toString();
                missionLocation = editLocation.getText().toString();
                missionDateStart = txtDateStart.getText().toString();
                missionDateFinish = txtDateFinish.getText().toString();

                // Check if there's no Name or LastName
                if ((missionTitle == null) || missionTitle.replaceAll(" ","").equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.toast_missionNoName), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if((missionLocation == null) || missionLocation.replaceAll(" ","").equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.toast_missionNoLocation), Toast.LENGTH_SHORT).show();
                    return false;
                }

                thisMission.setName(missionTitle);
                thisMission.setLocation(missionLocation);
                try {
                    thisMission.setStartDate(dateFormat.parse(missionDateStart));
                    thisMission.setEndDate(dateFormat.parse(missionDateFinish));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // UPDATE MISSION if it's all OK, return false otherwise
                if (!DB.updateMission(thisMission)) return false;

                Intent startBillView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                startBillView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startBillView.putExtra("missionID", missionID);

                startActivityForResult(startBillView, MISSION_MOD);
                finish();
                break;

            default:
                finish();
                break;
        }
        return true;
    }

    /**
     * It assigns the values of the mission to the activity
     * @author matteo.mascotto
     */
    private void setMissionValues() {

        Bundle mission;

        Intent intent = getIntent();
        mission = intent.getExtras();
        missionID = mission.getInt("missionID", -1);

        Log.d("missionID", "Edit person " + missionID);
        thisMission = DB.getMission(missionID);

        missionTitle = thisMission.getName();
        missionLocation = thisMission.getLocation();
        missionDateStart = appUtilities.dateToStandardFormat(thisMission.getStartDate());
        missionDateFinish = appUtilities.dateToStandardFormat(thisMission.getEndDate());

        Log.d("DBMission",missionID + " " + missionTitle);

        //set those values to the edittext
        setPersonValuesOnScreen();
    }

    /**
     * Show on screen the values of the Mission
     * @author matteo.mascotto
     */
    private void setPersonValuesOnScreen() {

        txtTitle = findViewById(R.id.input_missionName);
        txtLocation = findViewById(R.id.input_missionLocation);
        txtDateStart = findViewById(R.id.input_missionStart);
        txtDateFinish = findViewById(R.id.input_missionFinish);

        txtTitle.setText(missionTitle);
        txtLocation.setText(missionLocation);
        txtDateStart.setText(missionDateStart);
        txtDateFinish.setText(missionDateFinish);
    }
}