package com.ing.software.ticketapp;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import database.DataManager;
import database.MissionEntity;

public class AddNewMission extends AppCompatActivity{

    Context context;
    public DataManager DB;
    TextView missionStart;
    TextView missionFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        setTitle(context.getString(R.string.newMission));
        setContentView(R.layout.activity_add_new_mission);
        initializeComponents();
    }

    private void initializeComponents(){
        missionStart = (TextView)findViewById(R.id.input_missionStart);
        missionFinish = (TextView)findViewById(R.id.input_missionFinish);
        LinearLayout bntMissionStart = (LinearLayout)findViewById(R.id.button_missionStart);
        LinearLayout bntMissionFinish = (LinearLayout)findViewById(R.id.button_missionFinish);
        bntMissionStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(missionStart);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        bntMissionFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(missionFinish);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addmission_menu, menu);
        return true;
    }

    /** Dal Maso
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_addMission:
                //read input text
                EditText editName =(EditText)findViewById(R.id.input_missionName);
                EditText editDescription = (EditText)findViewById(R.id.input_missionDescription);

                String name = editName.getText().toString();
                String description = editDescription.getText().toString();
                Log.d("verify null",name);
                String checkName= name.replaceAll(" ","");

                if((name==null)||checkName.equals("")) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                    name = timeStamp;
                }
                if((description==null)||description.equals("")){
                    description = context.getString(R.string.defaultDescription);
                }

                MissionEntity miss = new MissionEntity();
                miss.setName(name);
                miss.setPersonID(1);

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    miss.setStartMission(format.parse((String)missionStart.getText()));
                    miss.setEndMission(format.parse((String)missionFinish.getText()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                long missionID = DB.addMission(miss);
                Log.d("New mission id", ""+missionID);
                //create new directory with input text
                //Start billActivity
                Bundle bundle = new Bundle();

                Intent startImageView = new Intent(context, BillActivity.class);
                startImageView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startImageView.putExtra("missionID", (int) missionID);
                startImageView.putExtra("missionName", miss.getName());
                context.startActivity(startImageView);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
