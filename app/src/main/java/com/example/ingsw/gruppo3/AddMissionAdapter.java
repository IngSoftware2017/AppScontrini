package com.example.ingsw.gruppo3;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import database.MissionEntity;
import database.PersonEntity;

/**
 * Created by Federico Taschin on 05/12/2017.
 */

public class AddMissionAdapter extends ArrayAdapter<PersonEntity> {



public AddMissionAdapter(Context context, int textViewResourceId, List<PersonEntity> persons){
        super(context,textViewResourceId,persons);
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

        if(position == 0) {
            AddPersonHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.add_person_row_item, null);
                holder = new AddPersonHolder();
                convertView.setTag(holder);
            } else {
                holder = (AddPersonHolder) convertView.getTag();
            }
            PersonEntity personEntity = getItem(position);
        }else{
            PersonViewerHolder personViewerHolder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.person_row_custom,null);
                personViewerHolder = new PersonViewerHolder();
                personViewerHolder.personSurnameTextView = convertView.findViewById(R.id.personRowCustomId);
                convertView.setTag(personViewerHolder);
            }else{
                personViewerHolder = (PersonViewerHolder)convertView.getTag();
            }
            PersonEntity personEntity = getItem(position);
            personViewerHolder.personSurnameTextView.setText(personEntity.getLastName());
        }

        return convertView;
        }

@Override
public int getViewTypeCount() {
        return 2;
        }

@Override
public int getItemViewType(int position) {
        if(position == 0)
        return 1;
        return 0;
        }

static class PersonViewerHolder {
    private TextView personSurnameTextView;
}
static class AddPersonHolder{
    private TextView textView;
    private FloatingActionButton addPersonButton;
}
}

