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
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
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
    ListView listView;
    PeopleAdapter adapter;
    View myView;
    TextView noPeople;

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.titlePeople));
        setContentView(R.layout.activity_main);
        initialize();
        printAllPeople();
    }

    @Override
    public void onResume() {
        super.onResume();
        myView.setVisibility(View.INVISIBLE);
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
                    printAllPeople();
                    break;
                default:
                    printAllPeople();
                    break;
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("CANCELLED", "OK");
            printAllPeople();
        }
    }

    /** Dal Maso
     * Se non sono presenti persone mostra come aggiungerne una
     */
    private void startFabGuide(){
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.fab_addPerson),
                        "Benvenuto in TicketManager!",
                        "1) Clicca qui per aggiungere una nuova persona\n2) Una volta creata esegui uno swipe verso sinistra sulla casella se vuoi modificarla")
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
                    AppUtilities.circularReveal(myView);
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
                AppUtilities.circularReveal(myView);
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
     */
    public void addToList(){
        adapter = new PeopleAdapter(this, R.layout.person_card, listPeople);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * get all missions from the DB and print
     */
    public void printAllPeople(){
        listPeople.clear();
        List<PersonEntity> people = DB.getAllPerson();

        if(people.size() == 0){
            noPeople.setVisibility(View.VISIBLE);
        }
        else{
            noPeople.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < people.size(); i++)
        {
            listPeople.add(people.get(i));
            Log.d("Persone", ""+people.get(i).getName());
        }
        addToList();
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

}