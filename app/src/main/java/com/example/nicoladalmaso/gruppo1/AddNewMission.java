package com.example.nicoladalmaso.gruppo1;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AddNewMission extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Aggiungi una missione");
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
                //Qui si gestisce il click alla V della toolbar

                Log.d("AddMission", "OK");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
