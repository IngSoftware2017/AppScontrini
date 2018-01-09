package com.ing.software.ticketapp;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

public class MissionsOpen extends Fragment {

    public DataManager DB;
    int personID;
    PersonEntity thisPerson;
    public List<MissionEntity> listMission = new LinkedList<MissionEntity>();
    View rootView;
    MissionAdapterDB adapter;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_missions_open, container, false);

        DB = new DataManager(getContext());

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), AddNewMission.class);
                addMission.putExtra("person", personID);
                Log.d("PersonID", ""+personID);
                startActivityForResult(addMission, 1);
            }
        });

        listView = (ListView)rootView.findViewById(R.id.listMission);
        personID = getArguments().getInt("personID", 0);
        Log.d("TAB1", ""+personID);

        printAllMissions();
        return rootView;
    }

    /** PICCOLO
     * Adds in the database the new mission
     * @param mission the mission to be added
     */
    public void addToListDB(MissionEntity mission){
        listMission.add(mission);
        ListView listView = (ListView)rootView.findViewById(R.id.listMission);
        MissionAdapterDB adapter = new MissionAdapterDB(getContext(), R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    public void addToListDB(){
        adapter = new MissionAdapterDB(getContext(), R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    /**Lazzarin
     * clear the view after I've eliminated a mission(before to call printAllMissions)
     */
    public void clearAllMissions()
    {
        ListView listView = (ListView)rootView.findViewById(R.id.listMission);
        MissionAdapterDB emptyAdapter = new MissionAdapterDB(getContext(), R.layout.mission_card, listMission);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     */
    public void printAllMissions(){
        listMission.clear();
        List<MissionEntity> missions = DB.getMissionsForPerson(personID);
        int count = 0;
        TextView noMissions = (TextView)rootView.findViewById(R.id.noMissions);
        for (int i = 0; i < missions.size(); i++)
        {
            if(!missions.get(i).isRepay()){
                listMission.add(missions.get(i));
                //addToListDB(missions.get(i));
                count++;
            }
        }
        addToListDB();
        if(count == 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            noMissions.setVisibility(View.INVISIBLE);
        }
    }
}
