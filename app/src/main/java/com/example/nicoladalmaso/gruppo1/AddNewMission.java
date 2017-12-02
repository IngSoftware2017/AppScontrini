package com.example.nicoladalmaso.gruppo1;

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

public class AddNewMission extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Nuova missione");
        setContentView(R.layout.activity_add_new_mission);
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
     * Cattura degli eventi nella toolbar
     * @param item oggetto nella toolbar catturato
     * @return flag di successo
     * TODO: gestire l'aggiunta di una missione (Creo una cartella con il nome inserito nell'editText all'interno di getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_addMission:
                //read input text
                String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                EditText editName =findViewById(R.id.input_missionName);
                EditText editDescription = findViewById(R.id.input_missionDescription);
                String name=editName.toString();
                String description=editDescription.toString();
                //create new directory with input text
                File newMissionPath=new File(path+"/"+name);
                newMissionPath.mkdir();

                Log.d("AddMission", "OK");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
