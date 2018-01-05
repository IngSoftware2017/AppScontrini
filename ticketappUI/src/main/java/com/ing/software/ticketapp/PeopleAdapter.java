package com.ing.software.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import database.PersonEntity;

/**
 * Created by nicoladalmaso on 29/12/17.
 */

public class PeopleAdapter extends ArrayAdapter<PersonEntity> {

    Context context;
    String path = "";
    List<PersonEntity> persons;

    public PeopleAdapter(Context context, int textViewResourceId,
                          List<PersonEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        persons = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.person_card, null);
        TextView name = (TextView)convertView.findViewById(R.id.personName);

        PersonEntity person = getItem(position);
        name.setText(person.getName() + " " + person.getLastName());
        convertView.setTag(person.getID());

        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                /*Intent startMissionsView = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionActivity.class);
                startMissionsView.putExtra("personID", Integer.parseInt(v.getTag().toString()));
                ((MainActivity)context).startActivityForResult(startMissionsView, 1);*/
                Intent missionTab = new Intent(context, MissionsTabbed.class);
                missionTab.putExtra("personID", Integer.parseInt(v.getTag().toString()));
                ((MainActivity)context).startActivityForResult(missionTab, 1);
            }
        });

        return convertView;
    }
}
