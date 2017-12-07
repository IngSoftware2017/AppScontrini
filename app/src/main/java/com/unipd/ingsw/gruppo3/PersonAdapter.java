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
import database.PersonEntity;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class PersonAdapter extends ArrayAdapter<PersonEntity> {

    Context context;
    String path = "";
    int pos = 0;

    public PersonAdapter(Context context, int textViewResourceId,
                                 List<PersonEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.persona_row_item, null);
        CardView card = (CardView) convertView.findViewById(R.id.personaCardView);
        TextView personNameView = (TextView)convertView.findViewById(R.id.personNameView);
        //FloatingActionButton missionDelete = (FloatingActionButton)convertView.findViewById(R.id.dltMission);
        //missionDelete.setTag(position);
        PersonEntity personEntity= getItem(position);
        personNameView.setText(personEntity.getLastName());
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
                pos = Integer.parseInt(v.getTag().toString());
                path = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                Log.d("Directory Mission",path);
                File directory = new File(path);
                File[] files = directory.listFiles();

                Intent startMissionView = new Intent(context, BillActivityGruppo1.class);
                Variables.getInstance().setCurrentMissionDir(files[pos].getPath());
                Log.d("GlobalDir", Variables.getInstance().getCurrentMissionDir());
                startMissionView.putExtra("missionName", files[pos].getName());
                startMissionView.putExtra("missionId", pos);
                context.startActivity(startMissionView);
            }//onClick
        });

        return convertView;
    }
}
