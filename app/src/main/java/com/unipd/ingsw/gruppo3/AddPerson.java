package com.unipd.ingsw.gruppo3;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.EditText;

import database.DataManager;
import database.PersonEntity;

/**
 * Created by Step on 22/12/2017.
 */

public class AddPerson extends AppCompatActivity implements View.OnClickListener {

    //components
    EditText name;
    EditText lastName;
    EditText academicTitle;

    FloatingActionButton save;

    //db person
    PersonEntity person = new PersonEntity();

    /**
     * Associate all text views for persons object with their listener
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_add_new_person);

        name = findViewById(R.id.nameText);
        name.setOnClickListener(this);

        lastName = findViewById(R.id.lastNameText);
        lastName.setOnClickListener(this);

        academicTitle = findViewById(R.id.academicTitleText);
        academicTitle.setOnClickListener(this);

        // Button SAVE
        save = findViewById(R.id.saveButton);
        save.setOnClickListener(this);
    }

    /**
     * Manage all clicks on this activity
     * @param view
     */
    @Override
    public void onClick(View view){
        if(view.getId() == save.getId()){
            //TODO controllo errori di inserimento
            person.setName(name.getText().toString());
            person.setLastName(lastName.getText().toString());
            person.setAcademicTitle(academicTitle.getText().toString());

            DataManager.getInstance(this).addPerson(person);

            finish();
        }
    }
}
