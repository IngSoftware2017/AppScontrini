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
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import database.DataManager;
import database.MissionEntity;

/**
 * Created by nicoladalmaso on 30/11/17.
 */

public class MissionAdapterDB extends ArrayAdapter<MissionEntity> {

    Context context;
    String path = "";
    int missionID = 0;
    List<MissionEntity> missions;
    DataManager DB;

    public MissionAdapterDB(Context context, int textViewResourceId,
                          List<MissionEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        missions = objects;
        DB = new DataManager(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.mission_card, null);
        CardView card = (CardView)convertView.findViewById(R.id.missionCard);
        TextView title = (TextView)convertView.findViewById(R.id.missionTitle);
        TextView location = (TextView)convertView.findViewById(R.id.missionLocation);
        TextView total = (TextView)convertView.findViewById(R.id.missionTotal);

        MissionEntity c = getItem(position);
        title.setText(c.getName());
        location.setText(c.getLocation());
        total.setText(DB.getTotalAmountForMission(c.getID()).setScale(2, RoundingMode.HALF_EVEN).toString()+" €");
        convertView.setTag(c.getID());
        Log.d("MissionStartBadFormat", ""+c.getStartDate());
        //Lazzarin :blocco per convertire in formato più leggibile la data
        Date start=c.getStartDate();
        SimpleDateFormat tr=new SimpleDateFormat("dd/MM/yyyy");
        String startDate=tr.format(start);
        Date finish=c.getEndDate();
        String finishDate=tr.format(finish);
        Log.d("missionStart", startDate);
        Log.d("missionEnd",finishDate);


        //Dal Maso
        //Sets a default background color for the mission's card
        if(c.isRepay()) {
            card.setBackgroundColor(Color.parseColor("#7c7c7c"));
        }
        else{
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
        }

        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                missionID = Integer.parseInt(v.getTag().toString());
                Intent startTicketsView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                Singleton.getInstance().setMissionID(missionID);
                ((MissionsTabbed)context).startActivityForResult(startTicketsView, 1);
            }
        });

        return convertView;
    }
}
