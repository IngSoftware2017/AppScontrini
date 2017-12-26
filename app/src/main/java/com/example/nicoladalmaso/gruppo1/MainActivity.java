package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import database.Constants;
import database.DAO;
import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;


public class MainActivity extends AppCompatActivity {
    public DataManager DB;
    public List<MissionEntity> listMission = new LinkedList<MissionEntity>();
    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB = new DataManager(this.getApplicationContext());
        setTitle(getString(R.string.titleMission));
        setContentView(R.layout.activity_main);
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewMission.class);
                startActivity(addMission);
            }
        });
        printAllMissionsDB();
    }

    /** PICCOLO
     * Adds in the database the new mission
     * @param mission the mission to be added
     */
    public void addToListDB(MissionEntity mission){
        listMission.add(mission);
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterDB adapter = new MissionAdapterDB(this, R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    /**Lazzarin
     * clear the view after I've eliminated a mission(before to call printAllMissions)
     */
    public void clearAllMissions()
    {
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterDB emptyAdapter = new MissionAdapterDB(this, R.layout.mission_card, listMission);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     */
    public void printAllMissionsDB(){
        List<MissionEntity> missions = DB.getAllMission();
        List<PersonEntity> persons = DB.getAllPerson();
        //Crea una persona fake per non creare problemi di FOREING KEY
        if(persons.size()==0){
            PersonEntity person = new PersonEntity();
            person.setName("Nicola");
            person.setAcademicTitle("Studente");
            person.setLastName("Dal Maso");
            DB.addPerson(person);
            persons = DB.getAllPerson();
        }

        Log.d("Lista Persone", ""+persons.get(0).getID());
        Log.d("Persons", ""+persons.size());
        Log.d("Missions", ""+missions.size());

        TextView noMissions = (TextView)findViewById(R.id.noMissions);
        if(missions.size() == 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            noMissions.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < missions.size(); i++)
        {
            addToListDB(missions.get(i));
        }
    }
}