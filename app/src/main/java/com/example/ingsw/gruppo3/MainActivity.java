package com.example.ingsw.gruppo3;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

/**
 * Modified by Marco Olivieri on 03/12/2017
 * Modified by Step on 03/12/2017.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, Serializable{

    FloatingActionButton newMissionButton;
    ListView missionsList;
    TextView noticeEmptyText;
    static DataManager dataManager;
    List<MissionEntity> missions;
    MissionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(com.sw.ing.gestionescontrini.R.layout.activity_main);
        dataManager = new DataManager(this);

        newMissionButton = findViewById(R.id.newMissionButton);
        missionsList = findViewById(R.id.missionsList);
        noticeEmptyText = findViewById(R.id.emptyNoticeTextView);
        missions = new ArrayList<MissionEntity>();
        //MissionEntity test = new MissionEntity("name",null,null,"location",1);
        //missions.add(test);
        adapter = new MissionAdapter(this, R.layout.mission_row_custom, missions);
        missionsList.setAdapter(adapter);
        newMissionButton.setOnClickListener(this);
        checkInitialization();
        loadMissions();
    }

    /**
     * Initialize of activity
     * Check if there are some mission and show a message
     */
    private void checkInitialization(){
        if(missionsList.getAdapter().getCount()==0){
            noticeEmptyText.setVisibility(View.VISIBLE);
        }
        else{
            noticeEmptyText.setVisibility(View.INVISIBLE);
        }
    }

    public void callAddmission(){
        Intent callAddMission = new Intent(this, AddMission.class);
        //callAddMission.putExtra(IntentCodes.MAINACTIVITY,this);
        startActivity(callAddMission);
    }


    @Override
    public void onClick(View view) {
        if(view instanceof FloatingActionButton){
            callAddmission();
        }
    }

    public void loadMissions(){
        missions.addAll(dataManager.getAllMissions());
    }

    public int addPerson(PersonEntity personEntity){
        return dataManager.addPerson(personEntity);
    }

}
