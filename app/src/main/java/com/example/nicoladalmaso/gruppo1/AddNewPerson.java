package com.example.nicoladalmaso.gruppo1;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
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
    int personID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        setTitle(context.getString(R.string.newPerson));
        setContentView(R.layout.activity_add_new_person);
    }
    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addmission_menu, menu);
        return true;
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
            case R.id.action_addMission:
                //read input text
                EditText editName =(EditText)findViewById(R.id.input_personName);
                EditText editLastName = (EditText)findViewById(R.id.input_personLastName);
                EditText editAcademicTitle = (EditText)findViewById(R.id.input_personAcademicTitle);
                String name = editName.getText().toString();
                String lastName = editLastName.getText().toString();
                String academicTitle = editAcademicTitle.getText().toString();

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


                long personID = DB.addPerson(person);
                Log.d("New mission id", ""+personID);
                //create new directory with input text
                //Start billActivity
                Bundle bundle = new Bundle();

                Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionActivity.class);
                startImageView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startImageView.putExtra("personID", (int) personID);
                startImageView.putExtra("personName", name);
                context.startActivity(startImageView);
                finish();
                break;

            default:
                finish();
                break;
        }
        return true;
    }
}
