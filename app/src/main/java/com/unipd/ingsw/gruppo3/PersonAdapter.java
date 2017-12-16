package com.unipd.ingsw.gruppo3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;

/**
 * Created by nicoladalmaso on 30/11/17.
 *
 * Modify: Correct the persons list view showing the Name + LastName and not only LastName
 *         (not yet improved), improve Javadoc, implement the assignation of the Person at the
 *         Mission by the PersonsList
 * @author Matteo Mascotto on 16/12/2017
 */

public class PersonAdapter extends ArrayAdapter<PersonEntity> {

    Context context;
    DataManager dataManager;
    // int pos = 0;

    /**
     * Constructor of the class
     *
     * @param context it contain the context of the class
     * @param textViewResourceId
     * @param objects
     */
    public PersonAdapter(Context context, int textViewResourceId, List<PersonEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }

    /**
     * It sets the card view of the Person List
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.persona_row_item, null);
        CardView card = convertView.findViewById(R.id.personaCardView);
        TextView personNameView = convertView.findViewById(R.id.personNameView);
        PersonEntity personEntity= getItem(position);
        personNameView.setText(personEntity.getName() + " " + personEntity.getLastName());
        convertView.setTag(position);

        // Sets a default background color for the Person's card
        card.setBackgroundColor(Color.parseColor("#DBD8D8"));

        convertView.setOnClickListener(new View.OnClickListener(){
            /**
             * Assign the selected Person to the Mission
             * @author: Matteo Mascotto
             *
             * @param v
             */
            public void onClick (View v){
                /*
                pos = Integer.parseInt(v.getTag().toString());
                path = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                Log.d("Directory Mission",path);
                File directory = new File(path);
                File[] files = directory.listFiles();

                Intent startMissionView = new Intent(context, BillActivityGruppo1.class);
                Variables.getInstance().setCurrentMissionDir(files[pos].getPath());
                Log.d("GlobalDir", Variables.getInstance().getCurrentMissionDir());
                startMissionView.putExtra("missionName", files[pos].getName());
                startMissionView.putExtra("missionId", pos);
                context.startActivity(startMissionView);*/


                // Extraction of the persons selected and than assign it to the Mission
                // TODO Trovare il modo di estrarre l'ID della persona su cui si ha cliccato
                //      all'interno della lista per poi poterla asseganre alla Missione

            }
        });

        return convertView;
    }
}
