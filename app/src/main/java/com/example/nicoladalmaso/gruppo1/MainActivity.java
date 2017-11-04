package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static android.R.attr.bitmap;
import static com.example.nicoladalmaso.gruppo1.R.id.container;
import static com.example.nicoladalmaso.gruppo1.R.styleable.View;

public class MainActivity extends AppCompatActivity {
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List <Scontrino> list = new LinkedList<Scontrino>();

    public boolean isFabOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        printAllImages();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                animateFAB();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImage();
            }
        });

        //Esempio aggiunta record a db

        /* DbManager db = new DbManager(this);
        db.addRecord("ID della foto", getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(), "Descrizione della foto", "Data della foto");
        Cursor  cursor = db.query(); */

    }

    //Aggiunge una card alla lista
    //Accetta come input 2 string contenenti il titolo e la descrizione dello scontrino
    public void addToList(String title, String desc, Bitmap img){
        list.add(new Scontrino(title, desc, img));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list);
        listView.setAdapter(adapter);
    }

    //Animazioni per il Floating Action Button (FAB)
    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false; 
            Log.d("Raj", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }


    //----------------------------------------//
    //------- Funzione che scatta foto -------//
    //----------------------------------------//
        static final int REQUEST_TAKE_PHOTO = 1;
        //Lancio intent fotocamera
        private void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
        //Creazione file per ospitare la foto scattata
        String mCurrentPhotoPath;
        private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }
    //----------------------------------------//
    //----------------------------------------//
    //----------------------------------------//




    //----------------------------------------//
    //-------Selezione foto da galleria-------//
    //----------------------------------------//
        public static final int PICK_PHOTO_FOR_AVATAR = 2;

        public void pickImage() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
        }
    //----------------------------------------//
    //----------------------------------------//
    //----------------------------------------//




    //----------------------------------------//
    //-----Cattura risultato degli intent-----//
    //----------------------------------------//
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    //Display an error
                    return;
                }
                switch (requestCode) {

                    //Foto scattata da noi
                    case (1):
                        printLastImage();
                        //Inserimento nel db
                        //....
                        break;

                    //Foto presa da galleria
                    case (2):
                        Uri uri = data.getData();
                        try {
                            Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            savePickedFile(btm);
                            printLastImage();
                        }catch (Exception e){
                            //Fai qualcosa
                        }
                        break;
                }
            }
        }
    //----------------------------------------//
    //----------------------------------------//
    //----------------------------------------//



    //----------------------------------------//
    //------Salva foto presa da galleria------//
    //----------------------------------------//
        private void savePickedFile(Bitmap imageToSave) {
            String root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
            File myDir = new File(root);
            myDir.mkdirs();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            String fname = imageFileName+".jpg";
            File file = new File(myDir, fname);
            if (file.exists())
                file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    //----------------------------------------//
    //----------------------------------------//
    //----------------------------------------//


    //----------------------------------------//
    //-------Stampa delle foto salvate--------//
    //----------------------------------------//
        //Legge tutte le immagini
        private File[] readAllImages(){
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();

            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            Log.d("Files", "Size: "+ files.length);
            return files;
        }
        //Stampa tutte le immagini
        private void printAllImages(){
            File[] files = readAllImages();

            for (int i = 0; i < files.length; i++)
            {
                Bitmap myBitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());
                addToList(files[i].getName(), "Descrizione della foto", myBitmap);
            }

        }
        //Stampa l'ultima foto
        private void printLastImage(){
            File[] files = readAllImages();
            Bitmap myBitmap = BitmapFactory.decodeFile(files[files.length-1].getAbsolutePath());
            addToList(files[files.length-1].getName(), "Descrizione della foto", myBitmap);
        }
        //Stampa il bitmap passato (Solo per testing)
        private void printThisBitmap(Bitmap myBitmap){
            addToList("Print this bitmap", "Descrizione della foto", myBitmap);
        }
    //----------------------------------------//
    //----------------------------------------//
    //----------------------------------------//





}


