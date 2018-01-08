package com.example.nicoladalmaso.gruppo1;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

public class MissionsClosed extends Fragment {

    public DataManager DB;
    int personID;
    PersonEntity thisPerson;
    public List<MissionEntity> listMission = new LinkedList<MissionEntity>();
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_missions_closed, container, false);

        DB = new DataManager(getContext());

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
     *
     * Modify by Marco Olivieri: get repaid mission in alphabetical order by sql query
     */
    public void printAllMissions(){
        //List<MissionEntity> missions = DB.getMissionsForPerson(personID);
        //int count = 0;
        List<MissionEntity> missions = DB.getMissionRepaidForPerson(true, personID);
        TextView noMissions = (TextView)rootView.findViewById(R.id.noMissionsClosed);
        for (int i = 0; i < missions.size(); i++)
        {
            //if(missions.get(i).isRepay()) {
                //count++;
                addToListDB(missions.get(i));
            //}
        }
        //if(count == 0){
        if(missions.size() == 0){
            noMissions.setVisibility(View.VISIBLE);
        }
        else{
            noMissions.setVisibility(View.INVISIBLE);
        }

    }
}
