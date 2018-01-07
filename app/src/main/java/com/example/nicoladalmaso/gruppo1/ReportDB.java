package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * @author Marco Olivieri on 06/01/2018
 */

public class ReportDB extends AppCompatActivity {

    public DataManager DB;
    Context context;
    TextView txtNumPerson;
    TextView txtActiveMission;
    TextView txtCloseMission;
    TextView txtNumTicket;

    //@author Marco Olivieri
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_report_db);
        setTitle("Report DB");

        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        txtNumPerson=(TextView)findViewById(R.id.msgNumPersone);
        txtActiveMission=(TextView)findViewById(R.id.msgNumActiveMission);
        txtCloseMission=(TextView)findViewById(R.id.msgNumCloseMission);
        txtNumTicket=(TextView)findViewById(R.id.msgNumTicket);

        initializeValues();
    }

    /**
     * @author Elardo Stefano
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //toolbar is empty, handle only the return arrow which close the activity
        finish();
        return true;
    }

    /**
     * @author Marco Olivieri
     * Inizializes all fields with db numbers
     */

    private void initializeValues(){
        List<PersonEntity> person = DB.getAllPerson();
        txtNumPerson.setText(txtNumPerson.getText()+" "+ String.valueOf(person.size()));

        List<MissionEntity> activeMission = DB.getMissionRepaid(false);
        txtActiveMission.setText(txtActiveMission.getText()+" "+ String.valueOf(activeMission.size()));

        //List<MissionEntity> allMission = DB.getAllMission();
        //MissionEntity m = allMission.get(0);
        //boolean repaid = m.isRepay();
        //int closeM = allMission.size()-activeMission.size();
        List<MissionEntity> closeMission = DB.getMissionRepaid(true);
        txtCloseMission.setText(txtCloseMission.getText()+" "+ String.valueOf(closeMission.size()));

        List<TicketEntity> ticket = DB.getAllTickets();
        txtNumTicket.setText(txtNumTicket.getText()+" "+ String.valueOf(ticket.size()));
    }
}
