package com.example.nicoladalmaso.gruppo1;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

public class AddNewPerson extends AppCompatActivity {

    Context context;
    public DataManager DB;
    TextView missionStart;
    TextView missionFinish;
    long personID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        setTitle(context.getString(R.string.newPerson));
        setContentView(R.layout.activity_add_new_person);
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(addPerson()){
                    Intent startMissionView = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionsTabbed.class);
                    startMissionView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startMissionView.putExtra(IntentCodes.INTENT_PERSON_ID,personID);
                    context.startActivity(startMissionView);
                    finish();
                }
            }
        });
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
            default:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    private boolean addPerson(){
        EditText editName =(EditText)findViewById(R.id.input_personName);
        EditText editLastName = (EditText)findViewById(R.id.input_personLastName);
        EditText editAcademicTitle = (EditText)findViewById(R.id.input_personAcademicTitle);
        // EditText editEmail = (EditText)findViewById(R.id.input_personEmail);
        String name = editName.getText().toString();
        String lastName = editLastName.getText().toString();
        String academicTitle = editAcademicTitle.getText().toString();
        // String email = editEmail.getText().toString();

        if ((name == null) || name.replaceAll(" ","").equals("")) {
            Toast.makeText(context, getResources().getString(R.string.toast_personNoName), Toast.LENGTH_SHORT).show();
            return false;
        }
        if((lastName==null) || lastName.replaceAll(" ","").equals("")) {
            Toast.makeText(context, getResources().getString(R.string.toast_personNoLastName), Toast.LENGTH_SHORT).show();
            return false;
        }

        PersonEntity person = new PersonEntity();
        person.setName(name);
        person.setLastName(lastName);
        person.setAcademicTitle(academicTitle);
        // person.setEmail(email);

        personID = DB.addPerson(person);

        return true;
    }
}
