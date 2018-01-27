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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.daimajia.swipe.SwipeLayout;

import java.io.File;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

/**
 * Created by nicoladalmaso on 29/12/17.
 */

public class PeopleAdapter extends ArrayAdapter<PersonEntity> {

    Context context;
    String path = "";
    List<PersonEntity> persons;
    DataManager DB;

    public PeopleAdapter(Context context, int textViewResourceId,
                          List<PersonEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        persons = objects;
        DB = new DataManager(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.person_card, null);

        TextView name = (TextView)convertView.findViewById(R.id.personName);
        TextView title=(TextView)convertView.findViewById(R.id.personAcademicTitle);
        ImageView profilePic = (ImageView)convertView.findViewById(R.id.profile_image);
        ImageButton btnDelete = (ImageButton)convertView.findViewById(R.id.deletePerson);
        ImageButton btnUpdate = (ImageButton)convertView.findViewById(R.id.editPerson);
        LinearLayout toMissions = (LinearLayout) convertView.findViewById(R.id.personClick);
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

        //profile pic set, up to cache memory
        Glide.with(context)
                .load("")
                .placeholder(R.drawable.ic_user)
                .into(profilePic);

        toMissions.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                Intent missionTab = new Intent(context, com.example.nicoladalmaso.gruppo1.MissionsTabbed.class);
                Singleton.getInstance().setPersonID((int)person.getID());
                ((MainActivity)context).startActivityForResult(missionTab, 1);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                deletePerson((int)person.getID(), position);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                //Open Edit Person Activity
                Intent editPerson = new Intent(context, com.example.nicoladalmaso.gruppo1.EditPerson.class);
                editPerson.putExtra("personID", person.getID());
                Singleton.getInstance().setPersonID((int)person.getID());
                ((MainActivity)context).startActivityForResult(editPerson, 1);
            }
        });

        return convertView;
    }

    /**
     * Mantovan Federico (adapted by Dal Maso)
     * Delete the person and the missions\tickets associated with it
     */
    public void deletePerson(int personID, int position){
        AlertDialog.Builder toast = new AlertDialog.Builder(context);
        //Dialog
        toast.setMessage(context.getString(R.string.delete_person))
                .setTitle(context.getString(R.string.delete_title_person));
        //Positive button
        toast.setPositiveButton(context.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DB.deletePerson(personID);
                persons.remove(position);
                ((MainActivity)context).printAllPeople();
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
