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
import android.view.animation.Animation;
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
    public List<MissionEntity> listMission = new LinkedList<>();
    View rootView;
    MissionAdapterDB adapter;
    ListView listView;
    TextView noMissions;
    MissionsTabbed tabInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_missions_closed, container, false);

        DB = new DataManager(getContext());

        listView = (ListView)rootView.findViewById(R.id.listMission);
        noMissions = (TextView)rootView.findViewById(R.id.noMissionsClosed);
        personID = getArguments().getInt("personID", 0);

        printAllMissions();
        return rootView;
    }

    public void setParentInstance(MissionsTabbed missionsTabbed){
        this.tabInstance = missionsTabbed;
    }
    /** PICCOLO
     * Adds in the database the new mission
     */
    public void addToListDB(){
        adapter = new MissionAdapterDB(tabInstance, R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     *
     * Modify by Marco Olivieri: get repaid mission in alphabetical order by sql query
     */
    public void printAllMissions(){
        listMission.clear();
        List<MissionEntity> missions = DB.getMissionClosedForPerson(true, personID);
        TextView noMissions = (TextView)rootView.findViewById(R.id.noMissionsClosed);
        for (int i = 0; i < missions.size(); i++)
        {
            //if(missions.get(i).isRepay()) {
                //count++;
                listMission.add(missions.get(i));
            //}
        }
        addToListDB();

        if(missions.size() == 0)
            noMissions.setVisibility(View.VISIBLE);
        else
            noMissions.setVisibility(View.INVISIBLE);

    }

    /** Dal Maso
     * Delete one listview cell
     * @param v view to animate after deleting
     * @param index item position
     */
    public void deleteCell(final View v, final int index) {
        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                listMission.remove(index);
                adapter.notifyDataSetChanged();
                if(listMission.size() == 0){
                    noMissions.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationStart(Animation animation) {}
        };
        AppUtilities.collapse(v, al);
    }
}