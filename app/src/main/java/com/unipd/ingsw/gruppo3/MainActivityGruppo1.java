package com.unipd.ingsw.gruppo3;

import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.DataAnalyzer;
import com.ing.software.ocr.OnTicketReadyListener;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;

public class MainActivityGruppo1 extends AppCompatActivity implements OnTicketReadyListener{
    private static final String DEBUG_TAG="MAG1_DEBUG";
    public List<MissionEntity> list = new LinkedList<MissionEntity>();
    DataManager dataManager;
    DataAnalyzer analyzer;
    public static MainActivityGruppo1 mainActivity;
    ArrayList<OnTicketReadyListener> listeners = new ArrayList<OnTicketReadyListener>();
    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setTitle("Le tue missioni");
        setContentView(R.layout.activity_main_gruppo1);
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), AddNewMissionGruppo1.class);
                startActivity(addMission);
            }
        });
        dataManager = DataManager.getInstance(this);
        analyzer = new DataAnalyzer();
        analyzer.initialize(this);
        printAllMissions();
    }

    /** Dal Maso
     * Aggiunge alla lista la nuova missione
     * @param missionEntity MissionEntity to be inserted
     */
    public void addToListView(MissionEntity missionEntity){
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
        list = dataManager.getAllMission();
        TextView noMissions = (TextView)findViewById(R.id.noMissions);
        if(list.size()== 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            Log.d(DEBUG_TAG,"Mission number: "+list.size());
            noMissions.setVisibility(View.INVISIBLE);
        }
        for(MissionEntity missionEntity : list){
            addToListView(missionEntity);
        }
    }

    public void subscribe(OnTicketReadyListener onTicketReadyListener){
        listeners.add(onTicketReadyListener);
    }

    public void requestTicket(Ticket ticket){
        analyzer.getTicket(ticket, this);
    }

    @Override
    public void onTicketReady(Ticket ticket) {
        for(OnTicketReadyListener listener : listeners){
            if(listener.isRequested(ticket))
                listener.onTicketReady(ticket);
        }
    }

    @Override
    public boolean isRequested(Ticket ticket) {
        return true;
    }
}