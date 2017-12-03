package com.sw.ing.gestionescontrini;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Created by Marco Olivieri on 03/12/2017
 */

public class AddMission extends AppCompatActivity {

    //Components
    Button saveMissionButton;
    Button cancelMissionButton;
    Button addPersonButton;
    EditText nameMissionText;
    EditText startDateMissionText;
    EditText endDateMissionText;
    ListView personsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);

        saveMissionButton = findViewById(R.id.saveButton);
        cancelMissionButton = findViewById(R.id.deleteButton);
        addPersonButton = findViewById(R.id.addPersonButton);
        nameMissionText = findViewById(R.id.nameText);
        startDateMissionText = findViewById(R.id.starMissionText);
        endDateMissionText = findViewById(R.id.endMissionText);
        personsList = findViewById(R.id.personsList);
    }

    /**
     * @author Marco Olivieri on 03/12/2017 (Team 3)
     * return boolean - if setting field is ok
     */
    private boolean checkCorrectField(){
        return false;
    }
}
