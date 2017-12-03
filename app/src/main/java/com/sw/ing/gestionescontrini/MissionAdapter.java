package com.sw.ing.gestionescontrini;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import database.Mission;

/**
 * Created by Step on 03/12/2017.
 */

public class MissionAdapter extends ArrayAdapter<Mission> {

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
        convertView = inflater.inflate(R.layout.mission_row_custom,null);
        TextView name = convertView.findViewById(R.id.textViewName);
        TextView location = convertView.findViewById(R.id.textViewLocation);
        Mission mis = getItem(position);
        name.setText(mis.getName());
        location.setText(mis.getLocation());
        return convertView;
    }
}
