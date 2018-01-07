package com.ing.software.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import database.DataManager;
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
        inflater.inflate(R.menu.confirm_menu, menu);
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
        Intent intent = new Intent();
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_confirm:
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

                Intent startMissionView = new Intent(context, MissionsTabbed.class);
                startMissionView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startMissionView.putExtra("personID", (int) personID);
                startMissionView.putExtra("personName", name);
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
}
