package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ListView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List <Scontrino> list = new LinkedList<Scontrino>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();
    }

    //Gestione delle animazioni e visualizzazione delle foto salvate precedentemente
    public void initializeComponents(){
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
        //Camera button
        fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        //Gallery button
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
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

    //Aggiunge una card alla lista
    //Accetta come input 2 string contenenti il titolo e la descrizione dello scontrino
    public void addToList(String title, String desc, Bitmap img){
        list.add(new Scontrino(title, desc, img));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list);
        listView.setAdapter(adapter);
    }


    //----------------------------------------//
    //------- Funzione che scatta foto -------//
    //----------------------------------------//
        static final int REQUEST_TAKE_PHOTO = 1;

        private void dispatchTakePictureIntent() {
         Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
         /*   if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
           */

        }


    //----------------------------------------//
    //----------------------------------------//
    //----------------------------------------//


    //----------------------------------------//
    //-------Selezione foto da galleria-------//
    //----------------------------------------//
        public static final int PICK_PHOTO_FOR_AVATAR = 2;

        public void pickImageFromGallery() {
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
                    case (REQUEST_TAKE_PHOTO):

                       /* File description = new File(Environment.getExternalStorageDirectory(),
                                "photo"+String.valueOf(System.currentTimeMillis())+".jpg");
                        Uri  namePhoto= Uri.fromFile(description);
                        data.putExtra(MediaStore.EXTRA_OUTPUT,namePhoto);
                        CropImage.activity(namePhoto).start(this);
                      /* Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        savePickedFile(imageBitmap);
                        printLastImage();
                        */

                        break;

                    //Foto presa da galleria
                    case (PICK_PHOTO_FOR_AVATAR):
                        photoURI = data.getData();
                        //Invoco la libreria che si occupa del resize
                        CropImage.activity(photoURI)
                                .start(this);
                        break;

                    //Gestisco il risultato del Resize
                    case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                        Log.d("Alla", "okoko");
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        Uri resultUri = result.getUri();
                        try {
                            Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
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


