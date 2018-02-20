package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
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
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;

/**
 * Created by nicoladalmaso on 30/11/17.
 * 
 * Modified: Add pop-up menu for the card view
 * @author Matteo Mascotto on 13-01-2018
 */
public class MissionAdapterDB extends ArrayAdapter<MissionEntity> {

    Context context;
    DataManager DB;
    MissionsTabbed activity;
    String path = "";
    int missionID = 0;
    List<MissionEntity> missions;

    public MissionAdapterDB(Context context, int textViewResourceId,
                            List<MissionEntity> objects) {
        super(context, textViewResourceId, objects);
        this.activity = activity;
        missions = objects;
        DB = new DataManager(getContext());
    }

    /** Dal Maso
     * It manages the Adapter
     * @param position item position
     * @param convertView my custom view
     * @param parent parent view
     * @return view setted
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(R.layout.mission_card, null);

        final View view = convertView;

        SwipeLayout card = (SwipeLayout) convertView.findViewById(R.id.swipeMission);
        //CardView card = (CardView)convertView.findViewById(R.id.missionCard);
        TextView title = (TextView)convertView.findViewById(R.id.missionTitle);
        TextView location = (TextView)convertView.findViewById(R.id.missionLocation);
        ImageButton menuCard = (ImageButton) convertView.findViewById(R.id.missionMenu);
        TextView total = (TextView)convertView.findViewById(R.id.missionTotal);
        ImageButton btnDelete = (ImageButton)convertView.findViewById(R.id.deleteMission);
        ImageButton btnUpdate = (ImageButton)convertView.findViewById(R.id.editMission);
        RelativeLayout toTickets = (RelativeLayout) convertView.findViewById(R.id.missionClick);

        MissionEntity mission = getItem(position);
        title.setText(mission.getName());
        location.setText(mission.getLocation());
        total.setText(DB.getTotalAmountForMission(mission.getID()).setScale(2, RoundingMode.HALF_EVEN).toString() + " " + Singleton.getInstance().getCurrency());
        convertView.setTag(mission.getID());

        //Lazzarin :blocco per convertire in formato più leggibile la data
        SimpleDateFormat tr = new SimpleDateFormat("dd/MM/yyyy");
        Date start = mission.getStartDate();
        String startDate = tr.format(start);
        Date finish = mission.getEndDate();
        String finishDate = tr.format(finish);

        if(mission.isClosed()) {
            int textColor = context.getResources().getColor(R.color.white);
            card.setBackgroundColor(Color.parseColor("#7c7c7c"));
            total.setTextColor(textColor);
            location.setTextColor(textColor);
            title.setTextColor(textColor);
        }

        toTickets.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent startTicketsView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillActivity.class);
                Singleton.getInstance().setMissionID((int)mission.getID());
                ((MissionsTabbed)context).startActivityForResult(startTicketsView, 1);
            }
        });

        btnDelete.setOnClickListener(v -> deleteMission(view, mission, position));

        btnUpdate.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                //Open Edit Person Activity
                Intent editMission = new Intent(context, com.example.nicoladalmaso.gruppo1.EditMission.class);
                Singleton.getInstance().setMissionID((int)mission.getID());
                ((MissionsTabbed)context).startActivityForResult(editMission, 1);
            }
        });

        /**
         * Listener for the pupUp menu of the mission's cardView
         * @author Matteo Mascotto
         */
        menuCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(activity,menuCard, mission.getID());
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

    /**
     * Nicola Dal Maso
     * Delete the person and the missions\tickets associated with it
     */
    public void deleteMission(View v, MissionEntity mission, int position){
        AlertDialog.Builder toast = new AlertDialog.Builder(context);
        //Dialog
        toast.setMessage(context.getString(R.string.deleteMissionToast))
                .setTitle(context.getString(R.string.deleteTitle));
        //Positive button
        toast.setPositiveButton(context.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<TicketEntity> list = DB.getTicketsForMission(mission.getID());
                for(int i = 0; i < list.size(); i++){
                    DB.deleteTicket((int) list.get(i).getID());
                }
                DB.deleteMission(mission.getID());
                if(!mission.isClosed())
                    ((MissionsTabbed)context).updateThisAdapter(0, v, position);
                else
                    ((MissionsTabbed)context).updateThisAdapter(1, v, position);
            }
        });
        //Negative button
        toast.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing to do
            }
        });
        //Show toast
        AlertDialog alert = toast.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setTextColor(Color.parseColor("#2196F3"));
    }
}
