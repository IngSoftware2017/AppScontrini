package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
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

public class MainActivity extends AppCompatActivity {
    public List<Missione> list = new LinkedList<Missione>();
    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Le tue missioni");
        setContentView(R.layout.activity_main);
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
       /* File newDir1 = new File(path + "/Missione 1");
        File newDir2 = new File(path + "/Missione 2");
        File newDir3 = new File(path + "/Missione 3");
        File newDir4 = new File(path + "/Missione 4");
        newDir1.mkdir();
        newDir2.mkdir();
        newDir3.mkdir();
        newDir4.mkdir();*/
        File f = new File(path);
        File[] files = f.listFiles();
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                Log.d("Dir", inFile.toString());
            }
        }
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_addMission);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent addMission = new Intent(v.getContext(), com.example.nicoladalmaso.gruppo1.AddNewMission.class);
                startActivity(addMission);
            }
        });
        printAllMissions();
    }

    /** Dal Maso
     * Aggiunge alla lista la nuova missione
     * @param title Tilolo missione
     * @param desc descrizione missione
     */
    public void addToList(String title, String desc){
        list.add(new Missione(title, desc));
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapter adapter = new MissionAdapter(this, R.layout.mission_card, list);
        listView.setAdapter(adapter);
    }

    /** Dal Maso
     * Legge tutte le missioni disponibili
     * @return ritorna array di missioni
     */
    private File[] readAllMissions(){
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        return files;
    }

    /**Lazzarin
     * clear the view after I've eliminated a mission(before to call printAllMissions)
     *
     */
    public void clearAllMissions(){
        ListView listView = (ListView)findViewById(R.id.listMission);
        MissionAdapter emptyAdapter = new MissionAdapter(this, R.layout.mission_card, list);
        emptyAdapter.clear();
        emptyAdapter.notifyDataSetChanged();
        listView.setAdapter(emptyAdapter);}

    /** Dal Maso
     *  Stampa tutte le immagini
     */
    public void printAllMissions(){
        File[] files = readAllMissions();
        for (int i = files.length-1; i>=0; i--)
        {
            if(files[i].isDirectory())

                addToList(files[i].getName(),"descrizione");
        }
    }
}