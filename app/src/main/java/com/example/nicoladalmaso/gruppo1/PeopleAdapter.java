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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;
import database.MissionEntity;
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
        TextView title=(TextView)convertView.findViewById(R.id.personAcademicTitle);
        ImageView profilePic = (ImageView)convertView.findViewById(R.id.profile_image);
        PersonEntity person = getItem(position);
        profilePic.setImageResource(R.drawable.ic_user);
        name.setText(person.getName()+" "+person.getLastName());
        String academicTitle = person.getAcademicTitle();
        if ((academicTitle == null) || academicTitle.replaceAll(" ","").equals("")) {
            title.setText(context.getString(R.string.noAcademicTitle));
        }
        else {
            title.setText(person.getAcademicTitle());
        }

        convertView.setTag(person.getID());

        //profile pic set, up to cache memory
        Glide.with(context)
                .load("")
                .placeholder(R.drawable.ic_user)
                .into(profilePic);

        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent missionTab = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionsTabbed.class);
                Singleton.getInstance().setPersonID(Integer.parseInt(v.getTag().toString()));
                ((MainActivity)context).startActivityForResult(missionTab, 1);
            }
        });

        return convertView;
    }
}
