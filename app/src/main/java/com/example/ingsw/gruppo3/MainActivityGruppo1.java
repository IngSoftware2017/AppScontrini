package com.example.ingsw.gruppo3;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.ing.software.ocr.DataAnalyzer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import database.DataManager;

public class MainActivityGruppo1 extends AppCompatActivity {
    public List<MissioneGruppo1> list = new LinkedList<MissioneGruppo1>();
    DataManager dataManager;
    DataAnalyzer anaz;
    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Le tue missioni");
        setContentView(R.layout.activity_main_gruppo1);
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
       /* File newDir1 = new File(path + "/MissioneGruppo1 1");
        File newDir2 = new File(path + "/MissioneGruppo1 2");
        File newDir3 = new File(path + "/MissioneGruppo1 3");
        File newDir4 = new File(path + "/MissioneGruppo1 4");
        newDir1.mkdir();
        newDir2.mkdir();
        newDir3.mkdir();
        newDir4.mkdir();*/
        File f = new File(path);
        File[] files = f.listFiles();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                Log.d("Dir", inFile.toString());
            }
        }
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), AddNewMissionGruppo1.class);
                startActivity(addMission);
            }
        });
        printAllMissions();
    }

    /** Dal Maso
     * Aggiunge alla lista la nuova missione
     * @param title Tilolo missione
     * @param desc descrizione missione
     */
    public void addToList(String title, String desc){
        list.add(new MissioneGruppo1(title, desc));
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterGruppo1 adapter = new MissionAdapterGruppo1(this, R.layout.mission_card, list);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
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
        MissionAdapterGruppo1 emptyAdapter = new MissionAdapterGruppo1(this, R.layout.mission_card, list);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);}

    /** Dal Maso
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
}