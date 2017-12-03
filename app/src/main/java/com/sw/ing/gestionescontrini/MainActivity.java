package com.sw.ing.gestionescontrini;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import database.DataManager;

public class MainActivity extends AppCompatActivity {

    Button newMissionButton;
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

        checkInizialization();
    }

    /**
     * Inizialize of activity
     * Check if there are some mission and show a message
     */
    private void checkInizialization(){
        if(missionsList.getAdapter().getCount()==0){
            noticeEmptyText.setEnabled(Boolean.TRUE);
            missionsList.setEnabled(Boolean.FALSE);
        }
    }
}
