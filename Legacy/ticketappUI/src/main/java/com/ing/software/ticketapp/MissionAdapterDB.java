package com.ing.software.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import database.DataManager;
import database.MissionEntity;

public class MissionAdapterDB extends ArrayAdapter<MissionEntity> {

    Context context;
    String path = "";
    int missionID = 0;
    List<MissionEntity> missions;

    public MissionAdapterDB(Context context, int textViewResourceId,
                          List<MissionEntity> objects) {
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
        TextView location = (TextView)convertView.findViewById(R.id.missionLocation);
        TextView total = (TextView)convertView.findViewById(R.id.missionTotal);
        //FloatingActionButton missionDelete = (FloatingActionButton)convertView.findViewById(R.id.dltMission);
        //missionDelete.setTag(position);
        MissionEntity c = getItem(position);
        title.setText(c.getName());
        location.setText(c.getLocation());
        convertView.setTag(c.getID());
        Log.d("MissionStartBadFormat", ""+c.getStartDate());
        //Lazzarin :blocco per convertire in formato più leggibile la data
        Date start=c.getStartDate();
        SimpleDateFormat tr=new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String startDate=tr.format(start);
        Date finish=c.getEndDate();
        String finishDate=tr.format(finish);
        Log.d("missionStart", startDate);
        Log.d("missionEnd",finishDate);
        DataManager dataManager = DataManager.getInstance(context);
        if (dataManager.getTotalAmountForMission(c.getID()) != null)
            total.setText(context.getString(R.string.total_with_numb, dataManager.getTotalAmountForMission(c.getID()).setScale(2, RoundingMode.HALF_UP)));


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
                missionID = Integer.parseInt(v.getTag().toString());
                Intent startTicketsView = new Intent(context, BillActivity.class);
                startTicketsView.putExtra("missionID", missionID);
                ((MissionsTabbed)context).startActivityForResult(startTicketsView, 1);
            }
        });

        return convertView;
    }
}
