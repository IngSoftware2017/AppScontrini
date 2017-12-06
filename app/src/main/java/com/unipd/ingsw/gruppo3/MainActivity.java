package com.unipd.ingsw.gruppo3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.DataAnalyzer;
import com.ing.software.ocr.OnTicketReadyListener;

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
public class MainActivity extends AppCompatActivity implements View.OnClickListener, Serializable, OnTicketReadyListener {

    FloatingActionButton newMissionButton;
    ListView missionsList;
    TextView noticeEmptyText;
    static DataManager dataManager;
    static DataAnalyzer dataAnalyzer;
    List<MissionEntity> missions;
    MissionAdapter adapter;
    public static ArrayList<OnTicketReadyListener> listeners = new ArrayList<OnTicketReadyListener>();
    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(com.sw.ing.gestionescontrini.R.layout.activity_main);
        dataManager = new DataManager(this);
        dataAnalyzer = new DataAnalyzer();
        dataAnalyzer.initialize(this);

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

    public static void requestTicket(Bitmap photo, Ticket ticket, final OnTicketReadyListener ticketCb, Activity activity){
        dataAnalyzer.getTicket(photo,ticket,mainActivity, activity);
        //listeners.add(ticketCb);
        Log.d("AAAAAAA","REQUESTED ");
    }

    @Override
    public void onTicketReady(Ticket ticket) {
        dataManager.updateTicket(ticket.toEntity());

        for(OnTicketReadyListener listener :listeners){
          listener.onTicketReady(ticket);
        }
    }
}
