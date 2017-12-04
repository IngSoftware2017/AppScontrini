package com.sw.ing.gestionescontrini;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import database.Mission;

/**
 * Created by Step on 03/12/2017.
 */

public class MissionAdapter extends ArrayAdapter<Mission> {

    MissionViewerHolder holder;

    /**
     * Parametric constructor
     * @param context application context
     * @param textViewResourceId id of the textView
     * @param missions array of Mission class objects
     */
    public MissionAdapter(Context context, int textViewResourceId, List<Mission> missions){
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

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.mission_row_custom,null);
            holder = new MissionViewerHolder();
            holder.nameTextView = convertView.findViewById(R.id.textViewName);
            holder.locationTextView = convertView.findViewById(R.id.textViewLocation);
            convertView.setTag(holder);
        }
        else{
            holder = (MissionViewerHolder)convertView.getTag();
        }

        Mission mis = getItem(position);
        holder.nameTextView.setText(mis.getName());
        holder.locationTextView.setText(mis.getLocation());
        return convertView;
    }
    static class MissionViewerHolder{
        private TextView nameTextView;
        private TextView locationTextView;
    }
}
