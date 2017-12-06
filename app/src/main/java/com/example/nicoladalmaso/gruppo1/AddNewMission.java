package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import java.io.File;
import java.util.Date;

import database.DataManager;
import database.Mission;

public class AddNewMission extends AppCompatActivity {

    Context context;
    DataManager DB;
    Mission newMission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Nuova missione");
        setContentView(R.layout.activity_add_new_mission);
        context = this.getApplicationContext();
         DB = new DataManager(context);
    }

    /** Dal Maso
     * Opzioni per settaggio nuova toolbar dal /res/menu
     * @param menu
     * @return flag di successo
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addmission_menu, menu);
        return true;
    }

    /** Dal Maso
     * Edit by Lazzarin
     * Cattura degli eventi nella toolbar
     *
     * Modify: Improve database interaction
     * @author Matteo Mascotto
     *
     * @param item oggetto nella toolbar catturato
     * @return flag di successo
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_addMission:
                //read input text
                EditText editName =(EditText)findViewById(R.id.input_missionName);
                EditText editDescription = (EditText)findViewById(R.id.input_missionDescription);
                String name = editName.getText().toString();
                String description = editDescription.getText().toString();
                //create new directory with input text
                File newMissionPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + name);

                // If the insert of the Mission Info in the path it's ok, it will add it in the DB
                if (newMissionPath.mkdir()) {
                    Date start, end;
                    start = new Date(2017, 10, 10);
                    end = new Date(2017, 12, 12);

                    newMission = new Mission(name, description, start, end, "", 1);
                    DB.addMission(newMission);
                }

                //Start billActivity
                Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                startImageView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Variables.getInstance().setCurrentMissionDir(newMissionPath.getAbsolutePath());
                Log.d("GlobalDir", Variables.getInstance().getCurrentMissionDir());
                startImageView.putExtra("missionName", name);
                startImageView.putExtra("missionDescription",description);
                context.startActivity(startImageView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
