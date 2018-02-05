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
import android.widget.Toast;

import java.io.File;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

/**
 * Created by nicoladalmaso on 29/12/17.
 *
 * Modified: remove Academic Title view
 * @author matteo.mascotto on 12-01-2018
 */

public class PeopleAdapter extends ArrayAdapter<PersonEntity> {

    Context context;
    String path = "";
    List<PersonEntity> persons;

    public PeopleAdapter(Context context, int textViewResourceId, List<PersonEntity> objects) {
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
        //TextView title=(TextView)convertView.findViewById(R.id.personAcademicTitle);
        PersonEntity person = getItem(position);
        name.setText(person.getName()+" "+person.getLastName());
        String academicTitle = person.getAcademicTitle();
        TextView countText = (TextView)convertView.findViewById(R.id.activeCountText);
        DataManager db = new DataManager(context);
        int count=db.getActiveMissionsNumberForPerson(person.getID());
        if(count==0)
            countText.setVisibility(View.INVISIBLE);
        else{
            countText.setVisibility(View.VISIBLE);
            countText.setText(count+"");
        }

        /*
        if ((academicTitle == null) || academicTitle.replaceAll(" ","").equals("")) {
            title.setText(activity.getString(R.string.noAcademicTitle));
        }
        else {
            title.setText(person.getAcademicTitle());
        }
        */

        convertView.setTag(person.getID());

        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                /*Intent startMissionsView = new Intent(activity, com.example.nicoladalmaso.gruppo1.MissionActivity.class);
                startMissionsView.putExtra("personID", Integer.parseInt(v.getTag().toString()));
                ((MainActivity)activity).startActivityForResult(startMissionsView, 1);*/
                Intent missionTab = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionsTabbed.class);
                missionTab.putExtra(IntentCodes.INTENT_PERSON_ID, Integer.parseInt(v.getTag().toString()));
                ((MainActivity)context).startActivityForResult(missionTab, 1);
            }
        });

        return convertView;
    }
}
