package com.unipd.ingsw.gruppo3;

import android.content.Intent;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import database.DataManager;
import database.MissionEntity;

public class MainActivityGruppo1 extends AppCompatActivity {
    public List<MissionEntity> list;
    DataManager dataManager;
    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = new DataManager(this);
        list = dataManager.getAllMissions();
        setTitle("Le tue missioni");
        setContentView(R.layout.activity_main_gruppo1);
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
     * @param missionEntity
     */
    public void addToList(MissionEntity missionEntity){
        list.add(missionEntity);
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapterGruppo1 adapter = new MissionAdapterGruppo1(this, R.layout.mission_card, list);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * Legge tutte le missioni disponibili
     * @return ritorna array di missioni
     */

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
        TextView noMissions = (TextView)findViewById(R.id.noMissions);
        if(list.size()== 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            noMissions.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < list.size(); i++)
        {
                addToList(list.get(i));
        }
    }
}