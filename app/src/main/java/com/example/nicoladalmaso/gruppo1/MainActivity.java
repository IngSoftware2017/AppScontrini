package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
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
import database.Mission;
import database.Person;


public class MainActivity extends AppCompatActivity {
    public List<Missione> list = new LinkedList<Missione>();
    public DataManager DB;
    public List<Mission> listMission = new LinkedList<Mission>();
    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB = new DataManager(this.getApplicationContext());
        setTitle("Missioni");
        Variables.getInstance().setCurrentMissionDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());
        setContentView(R.layout.activity_main);
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        /*/PICCOLO Aggiungo delle missioni di prova
        Date data1= new Date();
        data1.setTime(2041920531);
        Person p1=new Person("aaaa","AAAAAAA","Dottore");
        Person p2=new Person("bbbb","BBBBBBB","Laureando");
        DB.addPerson(p1);
        DB.addPerson(p2);
        DB.addMission(new Mission(new Date(2000,03,02),new Date(2001,03,15),"New York",p1.getID()));
        DB.addMission(new Mission(new Date(2010,07,02),new Date(2011,01,15),"Berlino",p2.getID()));
        DB.addMission(new Mission(new Date(2005,03,02),new Date(2005,03,25),"Londra",p1.getID()));
        List<Mission> missionList = db.getAllMissions();
        for(int i=0; i<=missionList.size()-1; i++) {
            Log.d("mission", "mission " + i + ": " + missionList.get(i).getStartMission() + "," + missionList.get(i).getEndMission() + ","
                    + missionList.get(i).getLocation() + "," + missionList.get(i).getPersonID());
        }*/
        /*File f = new File(path);
        File[] files = f.listFiles();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                Log.d("Dir", inFile.toString());
            }
        }*/
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewMission.class);
                startActivity(addMission);
            }
        });
        //printAllMissions();
        printAllMissionsDB();
    }

    /** PICCOLO
     * Adds in the database the new mission
     * @param toAdd mission to be added
     */
    public void addToListDB(Mission toAdd){
        listMission.add(toAdd);
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterDB adapter = new MissionAdapterDB(this, R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * Aggiunge alla lista la nuova missione
     * @param title Tilolo missione
     * @param desc descrizione missione
     */
    public void addToList(String title, String desc){
        list.add(new Missione(title, desc));
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapter adapter = new MissionAdapter(this, R.layout.mission_card, list);
        listView.setAdapter(adapter);
    }

    /** Dal Maso (NOT USED)
     * Legge tutte le missioni disponibili
     * @return ritorna array di missioni
     */
    private File[] readAllMissions(){
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        return files;
    }

    /**Lazzarin
     * clear the view after I've eliminated a mission(before to call printAllMissions)
     *
     */
    public void clearAllMissions(){
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapter emptyAdapter = new MissionAdapter(this, R.layout.mission_card, list);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);}

    /** Dal Maso (NOT USED)
     *  Stampa tutte le immagini
     */
    public void printAllMissions(){
        File[] files = readAllMissions();
        TextView noMissions = (TextView)findViewById(R.id.noMissions);
        if(files.length == 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            noMissions.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < files.length; i++)
        {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("HH:mm'\n'dd/MM/yyyy");
            if(files[i].isDirectory())
                addToList(files[i].getName(), simpleDateFormat.format(files[i].lastModified()));
        }
    }

    //Dal Maso
    public void printAllMissionsDB(){
        List<Mission> missions = DB.getAllMissions();
        List<Person> persons = DB.getAllPersons();
        //Crea una persona fake per non creare problemi di FOREING KEY
        if(persons.size()==0){
            Person person = new Person();
            person.setName("Nicola");
            person.setAcademicTitle("Studente");
            person.setLastName("Dal Maso");
            DB.addPerson(person);
            persons = DB.getAllPersons();
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