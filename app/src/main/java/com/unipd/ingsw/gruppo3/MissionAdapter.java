package com.unipd.ingsw.gruppo3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import database.MissionEntity;

/**
 * Created by Step on 03/12/2017.
 * Old MissionAdapter, now using MissionAdapterGruppo1 until we decide if we want to change
 * the card view created by Nicola Dal Maso with something else
 */

public class MissionAdapter extends ArrayAdapter<MissionEntity> {

    /**
     * Parametric constructor
     * @param context application context
     * @param textViewResourceId id of the textView
     * @param missions list of Mission class objects
     */
    public MissionAdapter(Context context, int textViewResourceId, List<MissionEntity> missions){
        super(context,textViewResourceId,missions);
    }

    /**
     * Method that convert the usual view with one modified to show Mission objects
     * @param position list position
     * @param convertView view to convert
     * @param parent group of view
     * @return the converted view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        MissionViewerHolder holder;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.mission_row_custom, parent,false);
            holder = new MissionViewerHolder();
            holder.nameTextView = convertView.findViewById(R.id.textViewName);
            holder.locationTextView = convertView.findViewById(R.id.textViewLocation);
            convertView.setTag(holder);
        }
        else{
            holder = (MissionViewerHolder)convertView.getTag();
        }

        MissionEntity mis = getItem(position);
        holder.nameTextView.setText(mis.getName());
        holder.locationTextView.setText(mis.getLocation());
        return convertView;
    }

    private static class MissionViewerHolder{
        private TextView nameTextView;
        private TextView locationTextView;
    }
}
