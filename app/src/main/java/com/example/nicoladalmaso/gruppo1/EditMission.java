package com.example.nicoladalmaso.gruppo1;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import database.DataManager;
import database.MissionEntity;

/**
 * Created by Francesco on 03/01/2018.
 */

public class EditMission extends AppCompatActivity {
    public DataManager DB;
    long missionID;
    Context context;
    private String missionName;
    private String missionStart;
    private String missionEnd;
    private String missionLocation;
    boolean missionIsClosed;
    MissionEntity thisMission;
    TextView txtMissionName;
    TextView txtMissionStart;
    TextView txtMissionEnd;
    TextView txtMissionLocation;
    CheckBox chkIsClosed;
    //TODO: poter cambiare persona?
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setTitle("Modifica la missione");
        setContentView(R.layout.activity_edit_mission);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        //Get data from parent view
        txtMissionStart = (TextView)findViewById(R.id.input_missionStart);
        txtMissionEnd = (TextView)findViewById(R.id.input_missionFinish);
        LinearLayout bntMissionStart = (LinearLayout)findViewById(R.id.button_missionStart);
        LinearLayout bntMissionFinish = (LinearLayout)findViewById(R.id.button_missionFinish);
        bntMissionStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtMissionStart);
                newFragment.show(getFragmentManager(), "startDatePicker");
            }
        });

        bntMissionFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtMissionEnd);
                newFragment.show(getFragmentManager(), "finishDatePicker");
            }
        });
        setMissionValues();

    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.confirm_menu, menu);
        return true;
    }
    /** Dal Maso, adapted by Piccolo
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_confirm:
                thisMission.setName(txtMissionName.getText().toString());
                thisMission.setLocation(txtMissionLocation.getText().toString());
                //Date missionStart =new Date(txtMissionStart.getText().toString());
                //Date missionEnd =new Date(txtMissionEnd.getText().toString());
                Log.d("DBmissionDEBUG","prima della data");
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    String start=(String) missionStart.toString();
                    String finish=(String) missionEnd.toString();
                    if(!AppUtilities.checkDate(start,finish)) {
                        Log.d("formato data inserita", "errato");
                        Toast.makeText(context, getResources().getString(R.string.toast_errorDate), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else
                        Log.d("formato data inserita","corretto");
                    thisMission.setStartMission(format.parse(start));
                    thisMission.setEndMission(format.parse(finish));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //thisMission.setStartMission(missionStart);
                //thisMission.setEndMission(missionEnd);
                thisMission.setRepay(chkIsClosed.isChecked());
                DB.updateMission(thisMission);
                Log.d("DBmissionUPDATE",missionID+" "+missionName+" "+missionLocation+" "+missionStart+" "+missionEnd+" "+missionIsClosed);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso, adapted by Piccolo
     * set the values of the mission
     */
    private void setMissionValues(){
        Intent intent = getIntent();
        missionID = (int) intent.getExtras().getLong("missionID");
        Log.d("missionID", "Edit mission "+missionID);
        thisMission = DB.getMission(missionID);
        //TODO: capire perchè non salva
        missionName=thisMission.getName();
        missionLocation=thisMission.getLocation();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        missionStart=formatter.format(thisMission.getStartMission());
        missionEnd=formatter.format(thisMission.getEndMission());
        missionIsClosed= thisMission.isRepay();
        Log.d("DBmission",missionID+" "+missionName+" "+missionLocation+" "+missionStart+" "+missionEnd);
        //set those values to the edittext
        setMissionValuesOnScreen();
    }

    /**Dal Maso, adapted by Piccolo
     * show on screen the values of the mission
     */
    private void setMissionValuesOnScreen(){
        txtMissionName=(TextView)findViewById(R.id.input_missionName);
        txtMissionLocation=(TextView)findViewById(R.id.input_missionLocation);
        txtMissionStart=(TextView)findViewById(R.id.input_missionStart);
        txtMissionEnd=(TextView)findViewById(R.id.input_missionFinish);
        chkIsClosed=(CheckBox)findViewById(R.id.check_isRepaid);
        txtMissionName.setText(missionName);
        txtMissionLocation.setText(missionLocation);
        txtMissionStart.setText(missionStart.toString());
        txtMissionEnd.setText(missionEnd.toString());
        chkIsClosed.setChecked(missionIsClosed);

    }
}