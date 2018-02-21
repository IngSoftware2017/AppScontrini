package com.example.nicoladalmaso.gruppo1;

import android.animation.Animator;
import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Typeface;
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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    ListView listView;
    PeopleAdapter adapter;
    View myView;
    TextView noPeople;

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTitle(getString(R.string.titlePeople));
        setContentView(R.layout.activity_main);
        initialize();
        printAllPeople();
    }

    @Override
    public void onResume() {
        super.onResume();
        myView.setVisibility(View.INVISIBLE);
    }

    /**
     * Dal Maso adapted by Elardo
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main_activity_menu, menu);
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
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
                    //clearAllPeople();
                    printAllPeople();
                    break;
                default:
                    //clearAllPeople();
                    printAllPeople();
                    break;
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            printAllPeople();
        }
    }

    /** Dal Maso
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = new Intent();
        switch (item.getItemId()) {
            //Open settings panel
            case R.id.action_OCRsettings:
                int cx = myView.getWidth();
                AppUtilities.circularReveal(myView, cx, 0);
                Intent settings = new Intent(getApplicationContext(), com.example.nicoladalmaso.gruppo1.ApplicationSettings.class);
                startActivity(settings);
                break;
        }
        return true;
    }

    /** Dal Maso
     * Se non sono presenti persone mostra come aggiungerne una
     */
    private void startFabGuide(){
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.fab_addPerson),
                        getResources().getString(R.string.personAddTitle),
                        getResources().getString(R.string.personAddDesc))
                        .targetCircleColor(R.color.white)
                        .titleTextSize(21)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(13)
                        .descriptionTextColor(R.color.white)
                        .textColor(R.color.white)
                        .icon(getResources().getDrawable(R.mipmap.ic_add_white_24dp)),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView v) {
                        super.onTargetClick(v);      // This call is optional
                        int cx = myView.getWidth();
                        int cy = myView.getHeight();
                        AppUtilities.circularReveal(myView, cx, cy);
                        Intent addPerson = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewPerson.class);
                        startActivityForResult(addPerson, person_added);
                    }
                }
        );
    }

    /** Dal Maso
     * Initialize the activity
     */
    private void initialize(){
        myView = findViewById(R.id.circularAnimation);
        noPeople = (TextView)findViewById(R.id.noPeople);
        listView = (ListView)findViewById(R.id.listPeople);
        DB = new DataManager(this.getApplicationContext());
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addPerson);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int cx = myView.getWidth();
                int cy = myView.getHeight();
                AppUtilities.circularReveal(myView, cx, cy);
                Intent addPerson = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewPerson.class);
                startActivityForResult(addPerson, person_added);
            }
        });
        if(DB.getAllPerson().size() == 0){
            startFabGuide();
        }

    }

    /** PICCOLO
     * Adds in the database the new mission
     * @param person the person to be added
     */
    public void addToList(PersonEntity person){
        listPeople.add(person);
        ListView listView = (ListView)findViewById(R.id.listPeople);
        adapter = new PeopleAdapter(this, R.layout.person_card, listPeople);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     */
    public void printAllPeople(){
        listPeople.clear();
        List<PersonEntity> people = getAllPersonOrderedByNumMission();
        TextView noPeople = (TextView)findViewById(R.id.noPeople);

        if(people.size() == 0)
            noPeople.setVisibility(View.VISIBLE);
        else
            noPeople.setVisibility(View.INVISIBLE);

        for (int i = 0; i < people.size(); i++)
            addToList(people.get(i));
    }

    /**
     * @author Federico Taschin
     * @return List of PersonEntity not null, ordered by the number of active missions
     */
    public List<PersonEntity> getAllPersonOrderedByNumMission(){
        List<PersonEntity> persons = DB.getAllPerson();
        PersonNumMissions[] tempArray = new PersonNumMissions[persons.size()];
        int lastIndex = 0;
        int index;
        for(PersonEntity personEntity : persons){
            int numMissions = activeMissionsNumber(personEntity.getID());
            PersonNumMissions personNumMissions = new PersonNumMissions(personEntity,numMissions);
            index = lastIndex;
            while (index>0 && tempArray[index-1].numMissions<numMissions){
                index--;
            } //now tempArray[index] is the last element with a lower value in numMissions
            insert(tempArray, lastIndex, index, personNumMissions);
            lastIndex++;
        }
        persons.clear();
        for(PersonNumMissions personNumMissions : tempArray){
            persons.add(personNumMissions.personEntity);
        }
        return persons;
    }

    /**
     * @author Federico TAschin
     * @param personId the id of the PersonEntity
     * @return the number of active missions for the given PersonEntity
     */
    private int activeMissionsNumber(long personId){
        List<MissionEntity> missions = DB.getMissionsForPerson(personId);
        int cont = 0;
        for(MissionEntity missionEntity : missions){
            if(!missionEntity.isClosed()){
                cont++;
            }
        }
        return cont;
    }

    /**
     * @author Federico Taschin
     * Insert a value into the array at a given position by traslating the portion of the array of 1 position right
     * @param array the input array (not null), size >0
     * @param lastIndex first free position of the array (between 0 and array.length-2)
     * @param from the position from which to traslate the values (between 0 and array.length-2)
     * @param newValue the object to be inserted
     */
    private void insert(PersonNumMissions[] array, int lastIndex, int from, PersonNumMissions newValue){
        //last index is the first free position of the array
        //from is the element from which to traslate the values
        for(int i = lastIndex; i>from; i--){
            array[i] = array[i-1];
        }
        array[from] = newValue;
    }

    /** Dal Maso
     * Delete one listview cell
     * @param v view to animate after deleting
     * @param index item position
     */
    public void deleteCell(final View v, final int index) {
        Animation.AnimationListener al = new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                listPeople.remove(index);
                adapter.notifyDataSetChanged();
                if(listPeople.size() == 0){
                    noPeople.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationStart(Animation animation) {}
        };
        AppUtilities.collapse(v, al);
    }

    class PersonNumMissions {
        PersonEntity personEntity;
        int numMissions;
        public PersonNumMissions(PersonEntity personEntity, int numMissions) {
            this.personEntity = personEntity;
            this.numMissions = numMissions;
        }
    }
}