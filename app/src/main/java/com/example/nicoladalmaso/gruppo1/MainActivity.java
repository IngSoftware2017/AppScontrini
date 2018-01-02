package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import database.Constants;
import database.DAO;
import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;


public class MainActivity extends AppCompatActivity {
    public DataManager DB;
    public List<PersonEntity> listPeople = new LinkedList<PersonEntity>();
    static final int person_added = 1;

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.titlePeople));
        setContentView(R.layout.activity_main);
        initialize();
        printAllPeople();
    }

    /** Dal Maso
     * Catch intent results
     * @param requestCode action number
     * @param resultCode intent result code
     * @param data intent data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result", ""+requestCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (person_added):
                    clearAllPeople();
                    printAllPeople();
                    break;
                default:
                    clearAllPeople();
                    printAllPeople();
                    break;
            }
        }
    }

    private void initialize(){
        DB = new DataManager(this.getApplicationContext());
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addPerson);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addPerson = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewPerson.class);
                startActivityForResult(addPerson, person_added);
            }
        });
    }

    /** PICCOLO
     * Adds in the database the new mission
     * @param person the person to be added
     */
    public void addToList(PersonEntity person){
        listPeople.add(person);
        ListView listView = (ListView)findViewById(R.id.listPeople);
        PeopleAdapter adapter = new PeopleAdapter(this, R.layout.person_card, listPeople);
        listView.setAdapter(adapter);
    }

    /**Lazzarin
     * clear the view after I've eliminated a mission(before to call printAllMissions)
     */
    public void clearAllPeople()
    {
        ListView listView = (ListView)findViewById(R.id.listPeople);
        PeopleAdapter emptyAdapter = new PeopleAdapter(this, R.layout.mission_card, listPeople);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     */
    public void printAllPeople(){
        List<PersonEntity> people = DB.getAllPerson();
        TextView noPeople = (TextView)findViewById(R.id.noPeople);
        if(people.size() == 0){
            noPeople.setVisibility(View.VISIBLE);
        }
        else{
            noPeople.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < people.size(); i++)
        {
            Log.d("Persone", ""+people.get(i).getName());
            addToList(people.get(i));
        }
    }
}