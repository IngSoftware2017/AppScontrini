package com.example.nicoladalmaso.gruppo1;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import database.DataManager;
import database.Mission;

public class AddNewMission extends AppCompatActivity{

    Context context;
    public DataManager DB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB = new DataManager(this.getApplicationContext());
        setTitle("New Mission");
        setContentView(R.layout.activity_add_new_mission);
        context = this.getApplicationContext();
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
                EditText editName =(EditText)findViewById(R.id.input_missionName);
                EditText editDescription = (EditText)findViewById(R.id.input_missionDescription);

                String name = editName.getText().toString();
                String description = editDescription.getText().toString();
                Log.d("verify null",name);
                String voidChar=" ";
                if((name==null)||name.equals("")) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    name = timeStamp;
                }
                for(int i=0;i<50;i++)
                {
                    voidChar=voidChar+" ";
                    if(name.equals(voidChar))
                    { String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    name = timeStamp;}

                }
                if((description==null)||description.equals("")){
                    description = text.defaultDescription;
                }
                Mission miss = new Mission();
                int missionID = 0;
                miss.setPersonID(1);
                miss.setName(name);
                miss.setDescription(description);
                DB.addMission(miss);
                List<Mission> missions = DB.getAllMissions();
                for(int i = 0; i < missions.size(); i++){
                    if(missions.get(i).getID() > missionID)
                        missionID = missions.get(i).getID();
                }
                Log.d("New mission id", ""+missionID);
                //create new directory with input text
                //Start billActivity
                Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                startImageView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startImageView.putExtra("missionName", name);
                startImageView.putExtra("missionDescription",description);
                startImageView.putExtra("missionID", missionID);
                context.startActivity(startImageView);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
