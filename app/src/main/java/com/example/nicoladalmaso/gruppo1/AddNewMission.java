package com.example.nicoladalmaso.gruppo1;

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
import android.widget.Toast;

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
import database.PersonEntity;

public class AddNewMission extends AppCompatActivity{

    Context context;
    public DataManager DB;
    TextView missionStart;
    TextView missionFinish;
    int personID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setTitle(getString(R.string.newMission));
        setContentView(R.layout.activity_add_new_mission);

        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        Intent intent = getIntent();
        personID = intent.getExtras().getInt("person");
        Log.d("PersonIDAddMission", ""+personID);

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
                newFragment.show(getFragmentManager(), "startDatePicker");
            }
        });

        bntMissionFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment().newInstance(missionFinish);
                newFragment.show(getFragmentManager(), "finishDatePicker");
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
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.action_addMission:
                //read input text
                EditText editName =(EditText)findViewById(R.id.input_missionName);
                EditText editLocation = (EditText)findViewById(R.id.input_missionLocation);
                String name = editName.getText().toString();
                String location = editLocation.getText().toString();

                if ((name == null) || name.replaceAll(" ","").equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.toast_missionNoName), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if((location==null) || location.replaceAll(" ","").equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.toast_missionNoLocation), Toast.LENGTH_SHORT).show();
                    return false;
                }

                MissionEntity miss = new MissionEntity();
                miss.setName(name);
                miss.setPersonID(personID);
                miss.setLocation(location);
                miss.setRepay(false);

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {

                    String start=(String) missionStart.getText();

                    String finish=(String) missionFinish.getText();
                    if(!AppUtilities.checkDate(start,finish)) {
                        Log.d("formato data inserita", "errato");
                        Toast.makeText(context, getResources().getString(R.string.toast_errorDate), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else
                        Log.d("formato data inserita","corretto");



                    miss.setStartMission(format.parse(start));
                    miss.setEndMission(format.parse(finish));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                long missionID = DB.addMission(miss);
                Log.d("New mission id", ""+missionID);
                //create new directory with input text
                //Start billActivity
                Bundle bundle = new Bundle();

                Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                startImageView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startImageView.putExtra("missionID", (int) missionID);
                startImageView.putExtra("missionName", miss.getName());
                context.startActivity(startImageView);
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }




}
