package com.unipd.ingsw.gruppo3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import database.MissionEntity;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class MissionAdapterGruppo1 extends ArrayAdapter<MissionEntity> {

    Context context;

    public MissionAdapterGruppo1(Context context, int textViewResourceId,
                                 List<MissionEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
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
        final MissionEntity missionEntity= getItem(position);
        title.setText(missionEntity.getName());
        description.setText("");
        convertView.setTag(position);

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
                int index = Integer.parseInt(v.getTag().toString());
                MissionEntity mission = getItem(index);

                Intent startMissionView = new Intent(context, BillActivityGruppo1.class);
                startMissionView.putExtra(IntentCodes.MISSION_OBJECT,mission);
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
                                  ((MainActivityGruppo1)context).clearAllMissions();
                                  ((MainActivityGruppo1)context).printAllMissions();
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
