package com.example.nicoladalmaso.gruppo1;

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
 *
 */
public class EditPerson extends AppCompatActivity{
    public DataManager DB;
    int personID;
    Context context;
    PersonEntity thisPerson;
    String personName = "", personLastName = "", personAcademicTitle = "", email = "";
    TextView txtName;
    TextView txtLastName;
    TextView txtAcademicTitle;
    TextView txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this.getApplicationContext();
        DB = new DataManager(context);
        personID = Singleton.getInstance().getPersonID();
        thisPerson = DB.getPerson(personID);

        getSupportActionBar().setElevation(0);
        setTitle(context.getString(R.string.action_editPerson));
        setContentView(R.layout.activity_edit_person);
        setContentView(R.layout.activity_edit_person);

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
                thisPerson.setEmail(txtEmail.getText().toString());
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
        personName = thisPerson.getName();
        personLastName = thisPerson.getLastName();
        personAcademicTitle = thisPerson.getAcademicTitle();
        email = thisPerson.getEmail();
    }

    /**Dal Maso, adapted by Piccolo
     * show on screen the values of the person
     */
    private void setPersonValuesOnScreen(){
        txtName = findViewById(R.id.input_personEditName);
        txtLastName = findViewById(R.id.input_personEditLastName);
        txtAcademicTitle = findViewById(R.id.input_personEditAcademicTitle);
        txtEmail = findViewById(R.id.input_personEmail);
        txtName.setText(personName);
        txtLastName.setText(personLastName);
        txtAcademicTitle.setText(personAcademicTitle);
        txtEmail.setText(email);
    }
}
