package com.ing.software.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import database.DataManager;
import database.PersonEntity;

/**
 * Created by Francesco on 02/01/2018.
 */

public class EditPerson extends AppCompatActivity{
    public DataManager DB;
    int personID;
    Context context;
    PersonEntity thisPerson;
    String personName = "", personLastName = "", personAcademicTitle = "";
    TextView txtName;
    TextView txtLastName;
    TextView txtAcademicTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        //TODO: use string.xml
        setTitle("Modifica persona");
        setContentView(R.layout.activity_edit_person);

        context = this.getApplicationContext();
        DB = new DataManager(context);

        //Get data from parent view
        setPersonValues();

        setPersonValuesOnScreen();
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.confirm_menu, menu);
        return true;
    }
    /** Dal Maso, adapted by Piccolo
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_confirm:
                //TODO set the right intent
                thisPerson.setName(txtName.getText().toString());
                thisPerson.setLastName(txtLastName.getText().toString());
                thisPerson.setAcademicTitle(txtAcademicTitle.getText().toString());
                DB.updatePerson(thisPerson);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso, adapted by Piccolo
     * set the values of the person
     */
    private void setPersonValues(){
        Intent intent = getIntent();
        personID = intent.getExtras().getInt("personID");
        Log.d("personID", "Edit person "+personID);
        thisPerson = DB.getPerson(personID);
        //TODO: fix the crash
        personName = thisPerson.getName();
        personLastName = thisPerson.getLastName();
        personAcademicTitle = thisPerson.getAcademicTitle();
        Log.d("DBPerson",personID+" "+personName+" "+personLastName);
        //set those values to the edittext
    }

    /**Dal Maso, adapted by Piccolo
     * show on screen the values of the person
     */
    private void setPersonValuesOnScreen(){
        txtName = (TextView)findViewById(R.id.input_personEditName);
        txtLastName = (TextView)findViewById(R.id.input_personEditLastName);
        txtAcademicTitle = (TextView)findViewById(R.id.input_personEditAcademicTitle);
        txtName.setText(personName);
        txtLastName.setText(personLastName);
        txtAcademicTitle.setText(personAcademicTitle);
    }
}
