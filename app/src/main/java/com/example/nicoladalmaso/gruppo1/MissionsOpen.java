package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.LinkedList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

public class MissionsOpen extends Fragment {

    public DataManager DB;
    int personID;
    PersonEntity thisPerson;
    public List<MissionEntity> listMission = new LinkedList<MissionEntity>();
    View rootView;
    MissionsTabbed tabInstance;
    MissionAdapterDB adapter;
    ListView listView;
    View myView;
    TextView noMissions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_missions_open, container, false);

        DB = new DataManager(getContext());
        myView = rootView.findViewById(R.id.circularAnimation);
        noMissions = (TextView)rootView.findViewById(R.id.noMissions);
        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cx = myView.getWidth();
                int cy = myView.getHeight();
                AppUtilities.circularReveal(myView, cx, cy);
                Intent addMission = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewMission.class);
                addMission.putExtra("person", personID);
                Log.d("PersonID", ""+personID);
                startActivityForResult(addMission, 1);
            }
        });

        if(DB.getAllMission().size() == 0) startGuide();

        listView = (ListView)rootView.findViewById(R.id.listMission);
        personID = getArguments().getInt("personID", 0);

        printAllMissions();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        myView.setVisibility(View.INVISIBLE);
    }

    /** PICCOLO
     * Adds in the database the new mission
     */
    public void addToListDB(){
        adapter = new MissionAdapterDB(getContext(), R.layout.mission_card, listMission);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     *
     * Modify by Marco Olivieri: get active mission in alphabetical order by sql query
     */
    public void printAllMissions(){
        listMission.clear();
        List<MissionEntity> missions = DB.getMissionClosedForPerson(false, personID);
        TextView noMissions = (TextView)rootView.findViewById(R.id.noMissions);
        for (int i = 0; i < missions.size(); i++)
            listMission.add(missions.get(i));

        addToListDB();

        if(missions.size() == 0)
            noMissions.setVisibility(View.VISIBLE);
        else
            noMissions.setVisibility(View.INVISIBLE);
    }

    /** Dal Maso
     * Se non sono presenti missioni mostra come aggiungerne una
     */
    private void startGuide(){
        TapTargetView.showFor(getActivity(),
                TapTarget.forView(rootView.findViewById(R.id.fab_addMission),
                        getResources().getString(R.string.missionAddTitle),
                        getResources().getString(R.string.missionAddDesc))
                        .targetCircleColor(R.color.white)
                        .titleTextSize(21)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(13)
                        .descriptionTextColor(R.color.white)
                        .textColor(R.color.white)
                        .icon(getResources().getDrawable(R.mipmap.ic_add_white_24dp)),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView v) {
                        super.onTargetClick(v);      // This call is optional
                        int cx = myView.getWidth();
                        int cy = myView.getHeight();
                        AppUtilities.circularReveal(myView, cx, cy);
                        Intent addMission = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewMission.class);
                        addMission.putExtra("person", personID);
                        Log.d("PersonID", ""+personID);
                        startActivityForResult(addMission, 1);
                    }
                }
        );
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
