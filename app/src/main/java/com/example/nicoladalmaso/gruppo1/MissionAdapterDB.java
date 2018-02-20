package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import database.DataManager;
import database.MissionEntity;

/**
 * Created by nicoladalmaso on 30/11/17.
 * 
 * Modified: Add pop-up menu for the card view
 * @author Matteo Mascotto on 13-01-2018
 */

public class MissionAdapterDB extends ArrayAdapter<MissionEntity> {

    DataManager DB;
    MissionsTabbed activity;
    String path = "";
    int missionID = 0;
    List<MissionEntity> missions;

    public MissionAdapterDB(MissionsTabbed activity, int textViewResourceId,
                            List<MissionEntity> objects) {
        super(activity, textViewResourceId, objects);
        this.activity = activity;
        missions = objects;
        DB = new DataManager(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.mission_card, null);
        CardView card = (CardView)convertView.findViewById(R.id.missionCard);
        TextView title = (TextView)convertView.findViewById(R.id.missionTitle);
        TextView location = (TextView)convertView.findViewById(R.id.missionLocation);
        ImageButton menuCard = (ImageButton) convertView.findViewById(R.id.missionMenu);

        MissionEntity c = getItem(position);
        title.setText(c.getName());
        location.setText(c.getLocation());
        convertView.setTag(c.getID());
        Log.d("MissionStartBadFormat", ""+c.getStartDate());
        //Lazzarin :blocco per convertire in formato più leggibile la data
        Date start=c.getStartDate();
        SimpleDateFormat tr=new SimpleDateFormat("dd/MM/yyyy");
        String startDate=tr.format(start);
        Date finish=c.getStartDate();
        String finishDate=tr.format(finish);
        Log.d("missionStart", startDate);
        Log.d("missionEnd",finishDate);


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
                Intent startTicketsView = new Intent(activity.getApplicationContext(), BillActivity.class);
                startTicketsView.putExtra(IntentCodes.INTENT_MISSION_ID, missionID);
                (activity).startActivityForResult(startTicketsView, 1);
            }
        });

        /**
         * Listener for the pupUp menu of the mission's cardView
         * @author Matteo Mascotto
         */
        menuCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(activity,menuCard, c.getID());
            }
        });

        return convertView;
    }

    /**
     * It show the popUp menu for the mission
     * @author Matteo Mascotto
     *
     * @param view Viewer of the Mission's Card
     * @param missionID position inside the popUp menu list
     */
    private void showPopupMenu(Activity context, View view, long missionID) {

        PopupMenu popup = new PopupMenu(view.getContext(),view);
        MenuInflater inflater = popup.getMenuInflater();

        inflater.inflate(R.menu.popup_mission_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopUpMissionMenuListener(MissionAdapterDB.this,view, missionID));

        // Dynamic menu construction based on the open or not of a mission
        MissionEntity missionEntity = DB.getMission(missionID);
        if (missionEntity.isClosed()) {
            popup.getMenu().findItem(R.id.close_mission).setEnabled(false);
            popup.getMenu().findItem(R.id.export_mission).setEnabled(true);
        } else {
            popup.getMenu().findItem(R.id.close_mission).setEnabled(true);
            popup.getMenu().findItem(R.id.export_mission).setEnabled(false);
        }

        popup.show();
    }

    public void closeMission(MissionEntity missionEntity){
        missions.remove(missionEntity);
        notifyDataSetChanged();
        activity.reload();

    }

    public void deleteMission(MissionEntity missionEntity){
        missions.remove(missionEntity);
        notifyDataSetChanged();
        activity.reload();
    }

    public void setMissionRepaid(long missionID){
        for(MissionEntity missionEntity : missions){
            if(missionEntity.getID()==missionID) {
                missionEntity.setClosed(true);
                notifyDataSetChanged();
            }
        }
    }
}
