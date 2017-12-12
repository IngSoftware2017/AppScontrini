package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.software.ocr.DataAnalyzer;
import com.ing.software.ocr.OnTicketReadyListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import database.DataManager;
import database.Ticket;
public class BillActivity extends AppCompatActivity {
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List<Ticket> list = new LinkedList<Ticket>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    static final int REQUEST_TAKE_PHOTO = 1;
    public static final int PICK_PHOTO_FOR_AVATAR = 2;
    String tempPhotoPath;
    Integer pos;
    Context context;
    String root;
    public DataManager DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        Intent intent = getIntent();
        String missionName = intent.getExtras().getString("missionName");
        pos = intent.getExtras().getInt("missionID");
        Log.d("MissionID", ""+pos);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        setTitle(missionName);
        initializeComponents();
    }

    //Dal Maso (adding menu delete option)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deletemission_menu, menu);
        return true;
    }

    /** Dal Maso
     * Edit by Lazzarin
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_deleteMission:
                deleteMission();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Dal Maso
     *  Gestione delle animazioni e visualizzazione delle foto salvate precedentemente
     */
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
                takePhotoIntent();
            }
        });
        //Gallery button
        fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
    }

    /** Dal Maso
     *  Animazioni per il Floating Action Button (FAB)
     */
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

    /** Dal Maso
     * Aggiunge una card alla lista
     * @param fileUri
     * @param amount
     * @param shop
     * @param date
     * @param title
     * @param missionID
     */
    public void addToList(Uri fileUri, BigDecimal amount, String shop, Date date, String title, int missionID){
        list.add(new Ticket(fileUri, amount, shop, date, title, missionID));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, pos, DB);
        listView.setAdapter(adapter);
    }

    /** Dal Maso (Using Lazzarin code)
     * Delete the mission from the bills viewer (inside the mission)
     */
    public void deleteMission(){
        //Lazzarin
        Log.d("tagMission", "" + pos);
        AlertDialog.Builder toast = new AlertDialog.Builder(BillActivity.this);

        toast.setMessage(text.deleteMissionToast)
                .setTitle("Cancellazione");

        toast.setPositiveButton(text.buttonDelete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent startMissionView = new Intent(context, com.example.nicoladalmaso.gruppo1.MainActivity.class);
                startMissionView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                for(int i = 0; i < list.size(); i++){
                    if(list.get(i).getMissionID() == pos){
                        DB.deleteTicket(list.get(i).getID());
                    }
                }
                DB.deleteMission(pos);
                context.startActivity(startMissionView);
            }
        });

        toast.setNegativeButton(text.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing to do
            }
        });
        
        AlertDialog alert = toast.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setTextColor(Color.parseColor("#2196F3"));
    }

    /**Lazzarin
     * It Opens the camera,takes the photo and puts as Extra Uri created by createImageFile method.
     * @Framing Camera, directory modified by createImageFile
     */
    private void takePhotoIntent() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhoto.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {}
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhoto, REQUEST_TAKE_PHOTO);
            }
        }
    }


    /**Lazzarin
     * It creates a temporary file where to save the photo on.
     * @Framing Directory Pictures
     * @Return the temporary file
     *
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = new File(root);
        File image = File.createTempFile(
                "temp",  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        tempPhotoPath = image.getAbsolutePath();
        return image;
    }


    /** Dal Maso
     *  Cancella i file temporanei utilizzati per il salvataggio delle foto da fotocamera
     */
    public void deleteTempFiles(){
        File[] files = readAllImages();
        String filename = "";
        for (int i = 0; i < files.length; i++)
        {
            filename = files[i].getName();
            Log.d("Sub", filename.substring(0,4));
            if(filename.substring(0,4).equals("temp")){
                files[i].delete();
            }
        }
    }


    /** Dal Maso
     *  Selezione foto da galleria
     */
    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }


    /** Dal Maso
     * Cattura risultato degli intent
     * @param requestCode ritorna il numero di azione compiuta
     * @param resultCode indica se l'operazione è andata a buon fine
     * @param data Risultato dell'operazione
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                /**lazzarin
                 * Saves definitely the photo without losing quality, deletes the temporary file and shows
                 * the new photo.
                 * @Framing Add the photo on the directory using savePickedFile()
                 */
                case(REQUEST_TAKE_PHOTO):
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmapPhoto = BitmapFactory.decodeFile(tempPhotoPath,bmOptions);
                    savePickedFile(bitmapPhoto);
                    deleteTempFiles();
                    waitDB();
                    clearAllImages();
                    printAllImages();
                    break;

                //Dal Maso
                //Foto presa da galleria
                case (PICK_PHOTO_FOR_AVATAR):
                    photoURI = data.getData();
                    try {
                        Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                        savePickedFile(btm);
                        waitDB();
                        clearAllImages();
                        printAllImages();
                        Log.d("Foto da galleria", "OK");
                    }catch (Exception e){
                        Log.d("Foto da galleria", "ERROR");
                    }
                    break;
                //Dal Maso
                //Gestisco il risultato del Resize
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                    Log.d("Crop", "OK");
                    waitDB();
                    clearAllImages();
                    printAllImages();
                    break;
            }
        }
    }


    public void waitDB(){
        try {
            Log.i("Waiting db", "Going to sleep");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /** Dal Maso
     * Salva il bitmap passato nell'apposita cartella
     * @param imageToSave bitmap da salvare come jpeg
     */
    private void savePickedFile(Bitmap imageToSave) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String fname = imageFileName+".jpg";
        File file = new File(root, fname);
        final Uri uri=Uri.fromFile(file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            //PICCOLO
            //using the ocr, extract the information from che picture and then add them to the database
            DataAnalyzer ocr = new DataAnalyzer();
            while (ocr.initialize(getApplicationContext())==1){
                //resource occupied
            }
            ocr.getTicket(imageToSave,new OnTicketReadyListener(){
                /**
                 * Get a Ticket. In the argument "ticket", fields corresponding to unextracted information are null.
                 * ID and fileURI fields are uninitialized.
                 * @param ticket new Ticket. Never null.
                 */
                @Override
                public void onTicketReady(com.ing.software.common.Ticket ticket) {
                    //TODO:CHECK THE DATA EXTRACTED FROM THE IMAGE
                    Log.d("OCRTicket", ticket.toString());
                    DB.addTicket(new Ticket(uri, ticket.amount, null, ticket.date, ticket.title, pos));
                }
            });
            ocr.release();

            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //PICCOLO_OLD
            // aggiungo il file al db
            //DatabaseManager helper = DatabaseManager.getInstance(getApplicationContext());
            //helper.addPhoto(root+fname); DB ALTERNATIVO
            //DbManager db = new DbManager(getApplicationContext());
            //db.addRecord(root+fname,"","","");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** NOT USED
     * Lazzarin
     * @param  imageToCrop is the photo we want to resize. It has to be a Bitmap object.
     * @return an Uri object taken to the file allocated in "documents",so it isn't show on the gallery
     *
     */
    private Uri savePhotoForCrop (Bitmap imageToCrop) {
        File allocation = temporaryFile();
        try {
            FileOutputStream out = new FileOutputStream(allocation);
            imageToCrop.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri uri=Uri.fromFile(allocation);
        return uri;

    }

    /** NOT USED
     * Lazzarin
     * @return temporary allocation with a File object.
     */
    private File temporaryFile()
    {
        File myDir = new File(root);
        String imageFileName = "photoToCrop.jpg";
        File file = new File(myDir, imageFileName);
        if (file.exists())
            file.delete();
        return file;
    }


    /**PICCOLO
     * Metodo che "ripulisce" lo schermo dalle immagini
     */
    public void clearAllImages(){
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, pos, DB);
        adapter.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }//clearAllImages


    /** Dal Maso
     * Legge tutte le immagini
     * @return ritorna tutti i file letti nella cartella
     */
    private File[] readAllImages(){
        String path = root;
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files=null;
        if(directory.listFiles()==null) {
            Log.d("Files", "hai trovato l'errore");
        }
         else
         files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        return files;
    }


    /** Dal Maso
     *  Stampa tutte le immagini
     */
    public void printAllImages(){
        List<Ticket> ticketList = DB.getAllTickets();
        Log.d("Tickets", ticketList.toString());
        Ticket t;
        int count = 0;
        for(int i = 0; i < ticketList.size(); i++){
            t = ticketList.get(i);
            if(t.getMissionID() == (pos)){
                count++;
                addToList(t.getFileUri(), t.getAmount(), t.getShop(), t.getDate(), t.getTitle(), t.getID());
            }
        }
        TextView noBills = (TextView)findViewById(R.id.noBills);
        if(count == 0){
            noBills.setVisibility(View.VISIBLE);
        }
        else{
            noBills.setVisibility(View.INVISIBLE);
        }
    }


    /** Dal Maso (NOT USED)
     *  Stampa l'ultima foto
     */
    private void printLastImage(){
        File[] files = readAllImages();
        Bitmap myBitmap = BitmapFactory.decodeFile(files[files.length-1].getAbsolutePath());
        //addToList(files[files.length-1].getName(), "Descrizione della foto", myBitmap);
    }


    /** NOT USED
     * Dal Maso
     * Stampa il bitmap passato (Solo per testing)
     * @param myBitmap bitmap da stampare
     */
    private void printThisBitmap(Bitmap myBitmap){
        //addToList("Print this bitmap", "Descrizione della foto", myBitmap);
    }


    /** NOT USED
     * PICCOLO_Edit by Dal Maso
     * Metodo che cancella l'i-esimo file in una directory
     * @param toDelete l'indice del file da cancellare
     * @param path percorso del file da cancellare
     * @return se l'operazione è andata a buon fine
     */
    public boolean deleteFile(int toDelete, String path){
        File directory = new File(path);
        File[] files = directory.listFiles();
        return files[toDelete].delete();
    }//deleteFile


    /**PICCOLO_Edit by Dal Maso
     * Metodo che cancella permette all'utente di ridimensionare la foto
     * @param toCrop l'indice della foto di cui fire il resize
     * @param path percorso della foto
     */
    public void cropFile(int toCrop, String path){
        Log.d("Crop","Success");
        boolean result = false;
        File directory = new File(path);
        File[] files = directory.listFiles();
        CropImage.activity(Uri.fromFile(files[toCrop])).start(this);
    }//cropFile

    /** NOT USED
     * VERSIONE DATABASE
     *PICCOLO
     * @param filename il id del file da cancellare a
     */
    /*
    private void deleteFileAndRow(String filename){
        DbManager db = new DbManager(getApplicationContext());
        //cancello il file associato solo se la query va a buon fine
        if(db.delete(filename)){
            File file = new File(filename);
            boolean deleted = file.delete();
        }//if
    }//deletePickedFile */
}
