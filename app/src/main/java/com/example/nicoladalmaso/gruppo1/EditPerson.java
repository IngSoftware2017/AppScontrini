package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import database.DataManager;
import database.PersonEntity;

/**
 * Created by Francesco on 02/01/2018.
 *
 * Modified: Improve the Person Modify
 * @author matteo.mascotto on 03/01/2018
 */

public class EditPerson extends AppCompatActivity{

    public DataManager DB;
    Context context;

    int personID;
    PersonEntity thisPerson;
    String personName = "", personLastName = "", personAcademicTitle = "";
    TextView txtName;
    TextView txtLastName;
    TextView txtAcademicTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);

        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        setTitle(getResources().getString(R.string.editPerson));
        setContentView(R.layout.activity_edit_person);

        //Get data from parent view
        setPersonValues();
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editmission_menu, menu);
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

        Intent intent = new Intent();

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_editMission:
                // Extract data from EditTexts
                EditText editName = findViewById(R.id.input_personName);
                EditText editLastName = findViewById(R.id.input_personLastName);
                EditText editAcademicTitle = findViewById(R.id.input_personAcademicTitle);
                personName = editName.getText().toString();
                personLastName = editLastName.getText().toString();
                personAcademicTitle = editAcademicTitle.getText().toString();

                // Check if there's no Name or LastName
                if ((personName == null) || personName.replaceAll(" ","").equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.toast_personNoName), Toast.LENGTH_SHORT).show();
                    return false;
                }
                if((personLastName==null) || personLastName.replaceAll(" ","").equals("")) {
                    Toast.makeText(context, getResources().getString(R.string.toast_personNoLastName), Toast.LENGTH_SHORT).show();
                    return false;
                }

                thisPerson.setName(txtName.getText().toString());
                thisPerson.setLastName(txtLastName.getText().toString());
                thisPerson.setAcademicTitle(txtAcademicTitle.getText().toString());

                // UPDATE PERSON if it's all OK, return false otherwise
                if (!DB.updatePerson(thisPerson)) return false;

                Intent startMissionView = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionActivity.class);
                startMissionView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startMissionView.putExtra("personID", personID);
                startMissionView.putExtra("personName", personName);

                context.startActivity(startMissionView);
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso, adapted by Piccolo
     * set the values of the person
     */
    private void setPersonValues(){

        Bundle person;

        Intent intent = getIntent();
        person = intent.getExtras();
        personID = person.getInt("personID", -1);

        Log.d("personID", "Edit person " + personID);
        thisPerson = DB.getPerson(personID);
        personName=thisPerson.getName();
        personLastName=thisPerson.getLastName();
        personAcademicTitle=thisPerson.getAcademicTitle();

        Log.d("DBPerson",personID+" "+personName+" "+personLastName);

        //set those values to the edittext
        setPersonValuesOnScreen();
    }

    /**Dal Maso, adapted by Piccolo
     * show on screen the values of the person
     */
    private void setPersonValuesOnScreen(){
        txtName=(TextView)findViewById(R.id.input_personName);
        txtLastName=(TextView)findViewById(R.id.input_personLastName);
        txtAcademicTitle=(TextView)findViewById(R.id.input_personAcademicTitle) ;
        txtName.setText(personName);
        txtLastName.setText(personLastName);
        txtAcademicTitle.setText(personAcademicTitle);
    }
}
