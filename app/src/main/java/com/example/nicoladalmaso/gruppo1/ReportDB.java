package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        setContentView(R.layout.activity_report_db);
        setTitle("Report DB");

        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        txtNumPerson=(TextView)findViewById(R.id.msgNumPersone);
        txtActiveMission=(TextView)findViewById(R.id.msgNumActiveMission);
        txtCloseMission=(TextView)findViewById(R.id.msgNumCloseMission);
        txtNumTicket=(TextView)findViewById(R.id.msgNumTicket);

        inizializeValues();
    }

    /**
     * @author Marco Olivieri
     * Inizializes all fields with db numbers
     */

    private void inizializeValues(){
        List<PersonEntity> person = DB.getAllPerson();
        txtNumPerson.setText(txtNumPerson.getText()+" "+ String.valueOf(person.size()+1));

        List<MissionEntity> activeMission = DB.getActiveMission();
        txtActiveMission.setText(txtActiveMission.getText()+" "+ String.valueOf(activeMission.size()+1));

        List<MissionEntity> allMission = DB.getAllMission();
        int closeMission = allMission.size()-activeMission.size();
        txtCloseMission.setText(txtCloseMission.getText()+" "+ closeMission);

        List<TicketEntity> ticket = DB.getAllTickets();
        txtNumTicket.setText(txtNumTicket.getText()+" "+ String.valueOf(ticket.size()+1));
    }
}
