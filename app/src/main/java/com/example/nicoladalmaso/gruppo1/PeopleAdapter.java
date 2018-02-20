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
import android.widget.PopupMenu;
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
 * @author Matteo Mascotto on 12-01-2018
 *
 * Modified: added pop-up menu using Matteo Mascotto code slightly changed
 * @author Stefano Elardo on 20-02-2018
 */

public class PeopleAdapter extends ArrayAdapter<PersonEntity> {

    Context context;
    String path = "";
    List<PersonEntity> persons;
    MainActivity activity;
    DataManager DB;

    public PeopleAdapter(Context context, int textViewResourceId, List<PersonEntity> objects, MainActivity activity) {
        super(context, textViewResourceId, objects);
        this.context = context;
        persons = objects;
        DB = new DataManager(context);
        this.activity=activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.person_card, null);
        TextView name = (TextView)convertView.findViewById(R.id.personName);
        ImageButton menuCard = (ImageButton) convertView.findViewById(R.id.personMenu);
        //TextView title=(TextView)convertView.findViewById(R.id.personAcademicTitle);
        PersonEntity person = getItem(position);
        name.setText(person.getLastName()+" "+person.getName());
        String academicTitle = person.getAcademicTitle();
        TextView countText = (TextView)convertView.findViewById(R.id.activeCountText);
        DataManager db = new DataManager(context);
        int count=db.getActiveMissionsNumberForPerson(person.getID());
        if(count==0)
            countText.setVisibility(View.INVISIBLE);
        else{
            countText.setVisibility(View.VISIBLE);
            countText.setText(count+"");
            if(count>9) {
                countText.setTextSize(countText.getTextSize() / 4);
            }
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
        /**
         * Listener for the pupUp menu of the mission's cardView
         * @author Matteo Mascotto
         */
        menuCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(activity,menuCard, person.getID());
            }
        });

        return convertView;
    }
    /**
     * It show the popUp menu for the mission
     * @author Matteo Mascotto
     *
     * @param view Viewer of the Mission's Card
     * @param personID position inside the popUp menu list
     */
    private void showPopupMenu(Activity context, View view, long personID) {

        PopupMenu popup = new PopupMenu(view.getContext(),view);
        MenuInflater inflater = popup.getMenuInflater();

        inflater.inflate(R.menu.popup_person_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopUpPersonMenuListener(PeopleAdapter.this,view, personID));

        popup.show();
    }

    public void deletePerson(PersonEntity personEntity){
        persons.remove(personEntity);
        notifyDataSetChanged();
        activity.reload();
    }
}
