package com.sw.ing.gestionescontrini;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.Mission;

/**
 * Modified by Marco Olivieri on 03/12/2017
 * Modified by Step on 03/12/2017.
 */
public class MainActivity extends AppCompatActivity {

    FloatingActionButton newMissionButton;
    ListView missionsList;
    TextView noticeEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataManager dbm = new DataManager(this);

        newMissionButton = findViewById(R.id.newMissionButton);
        missionsList = findViewById(R.id.missionsList);
        noticeEmptyText = findViewById(R.id.emptyNoticeTextView);
        Mission test = new Mission("name",null,null,"location",1);
        List<Mission> missions = new ArrayList<Mission>();
        missions.add(test);
        MissionAdapter adapter = new MissionAdapter(this,R.layout.mission_row_custom, missions);
        missionsList.setAdapter(adapter);
        checkInitialization();
    }

    /**
     * Initialize of activity
     * Check if there are some mission and show a message
     */
    private void checkInitialization(){
        if(missionsList.getAdapter().getCount()==0){
            noticeEmptyText.setEnabled(Boolean.TRUE);
            missionsList.setEnabled(Boolean.FALSE);
        }
    }
}
