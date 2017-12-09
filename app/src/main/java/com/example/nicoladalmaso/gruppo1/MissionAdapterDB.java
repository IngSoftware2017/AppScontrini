package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import database.Mission;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class MissionAdapterDB extends ArrayAdapter<Mission> {

    Context context;
    String path = "";
    int pos = 0;
    List<Mission> missions;

    public MissionAdapterDB(Context context, int textViewResourceId,
                          List<Mission> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        missions = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.mission_card, null);
        CardView card = (CardView) convertView.findViewById(R.id.missionCard);
        TextView title = (TextView)convertView.findViewById(R.id.missionTitle);
        TextView description = (TextView)convertView.findViewById(R.id.missionDescription);
        //FloatingActionButton missionDelete = (FloatingActionButton)convertView.findViewById(R.id.dltMission);
        //missionDelete.setTag(position);
        Mission c = getItem(position);
        title.setText(c.getName());
        description.setText(c.getDescription());
        convertView.setTag(c.getID());
        Log.d("MID", ""+c.getID());

        //Dal Maso
        //Sets a default background color for the mission's card
        switch (position%4){
            case 0:
                card.setBackgroundColor(Color.parseColor("#1F566D"));
                break;
            case 1:
                card.setBackgroundColor(Color.parseColor("#007787"));
                break;
            case 2:
                card.setBackgroundColor(Color.parseColor("#950068"));
                break;
            case 3:
                card.setBackgroundColor(Color.parseColor("#BC004F"));
                break;
        }

        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                pos = Integer.parseInt(v.getTag().toString());
                //path = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                //File directory = new File(path);
                //File[] files = directory.listFiles();
                Intent startMissionView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                //Variables.getInstance().setCurrentMissionDir(files[pos].getPath());
                String name = "";
                for(int i = 0; i < missions.size(); i++){
                    if(missions.get(i).getID() == pos){
                        name = missions.get(i).getName();
                    }
                }
                Log.d("MissionName", name);
                startMissionView.putExtra("missionName", name);
                startMissionView.putExtra("missionID", pos);
                context.startActivity(startMissionView);
            }//onClick
        });

        //lazzarin
        /*
            missionDelete.setOnClickListener(new View.OnClickListener(){
                  public void onClick(View v){
                      pos=  Integer.parseInt(v.getTag().toString());
                      //check if position is right
                      Log.d("tagMission", ""+pos);
                      //path = Variables.getInstance().getCurrentMissionDir();
                      path = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();

                      //check if directory is right
                      Log.d("Dir", path);

                      AlertDialog.Builder toast = new AlertDialog.Builder(context);
                      toast.setMessage("Sei sicuro di voler eliminare la missione?Tutti gli scontrini verranno eliminati")
                              .setTitle("Cancellazione");

                      toast.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
                              File directory = new File(path);
                              File[] files = directory.listFiles();
                              if(files[pos].delete()){
                                  ((MainActivity)context).clearAllMissions();
                                  ((MainActivity)context).printAllMissions();
                              }
                          }
                      });
                      toast.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
                              //Nothing to do
                          }
                      });
                      AlertDialog alert = toast.show();
                      Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                      nbutton.setTextColor(Color.parseColor("#2196F3"));

                  }
            });
        */
        return convertView;
    }
}
