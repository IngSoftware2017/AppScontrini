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
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    AddPersonEditText addPersonaEditText;
    int pos = 0; // The index position of the clicked object inside the Array

    /**
     * Constructor of the class
     *
     * @param context it contain the context of the class
     * @param textViewResourceId it's the ID of the textEdit in which the adapter will update the info
     * @param objects it contain the object where improve the changes
     */
    public PersonAdapter(Context context, int textViewResourceId, List<PersonEntity> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }

    /**
     * It sets the card view of the Person List
     *
     * @param position The position of the item within the PersonAdapter's data set
     * @param convertView The CardView object
     * @param parent The parent were the CardView is attached
     *
     * @return The CardView with the all Persons
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.persona_row_item, null);
        CardView card = convertView.findViewById(R.id.personaCardView);
        TextView personNameView = convertView.findViewById(R.id.personNameView);
        final PersonEntity personEntity= getItem(position);
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
                // Extraction of the persons selected and than assign it to the Mission
                PersonEntity personEntity= getItem(position);

                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.activity_add_mission, null);

                //addPersonaEditText = v.findViewById(R.id.addPersonaEditText);
                //addPersonaEditText.setPersonEntity(personEntity);
            }
        });

        return convertView;
    }
}
