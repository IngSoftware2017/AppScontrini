package com.unipd.ingsw.gruppo3;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.TimeKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.software.common.Ticket;
import com.ing.software.ocr.OnTicketReadyListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;

public class BillActivityGruppo1 extends AppCompatActivity implements OnTicketReadyListener{
    private final String DEBUG_TAG = "BAG1_DEBUG";

    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List<TicketEntity> list = new LinkedList<TicketEntity>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    static final int REQUEST_TAKE_PHOTO = 1;
    public static final int PICK_PHOTO_FOR_AVATAR = 2;
    String tempPhotoPath;
    Integer pos;
    Context context;
    MissionEntity missionEntity;
    ArrayList<Ticket> requestedTickets = new ArrayList<Ticket>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivityGruppo1.mainActivity.subscribe(this);
        setContentView(R.layout.activity_bill);
        Intent intent = getIntent();
         missionEntity = (MissionEntity) intent.getSerializableExtra(IntentCodes.MISSION_OBJECT);
        //pos = intent.getExtras().getInt("missionId");
        context = this.getApplicationContext();
        setTitle(missionEntity.getName());
        list = DataManager.getInstance(this).getTicketsForMission(missionEntity.getID());
        initializeComponents();
        Log.d("fin qui","corretto");
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
     * Cattura degli eventi nella toolbar
     * @param item oggetto nella toolbar catturato
     * @return flag di successo
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

    /**
     *
     * @param ticketEntity
     */
    public void addToList(TicketEntity ticketEntity){
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapterGruppo1 adapter = new CustomAdapterGruppo1(this, R.layout.cardview, list);
        listView.setAdapter(adapter);
    }

    /*public void addToMissionGrid(String title, String desc, Bitmap img){
        list.add(new Scontrino(title, desc, img));
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapterGruppo1 adapter = new CustomAdapterGruppo1(this, R.layout.cardview, list);
        listView.setAdapter(adapter);
    }*/

    /** Dal Maso (Using Lazzarin code)
     * Delete the mission from the bills viewer (inside the mission)
     */
    public void deleteMission(){
        //Lazzarin
        Log.d("tagMission", "" + pos);
        AlertDialog.Builder toast = new AlertDialog.Builder(BillActivityGruppo1.this);

        toast.setMessage("Sei sicuro di voler eliminare la missione?\nTutti gli scontrini verranno eliminati")
                .setTitle("Cancellazione");

        toast.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());
                File[] files = directory.listFiles();
                Intent startMissionView = new Intent(context, MainActivityGruppo1.class);
                File[] bill = files[pos].listFiles();
                Log.d("number of elements", bill.length+"");
                int count = bill.length;
                while(count > 0){
                    //remove internal file
                    if(bill[count-1].delete()){
                        Log.d("eliminated file number",count + "");
                        count--;
                    }
                }
                Log.d("flag",count+"qui ci arrivo");
                files[pos].delete();
                context.startActivity(startMissionView);
            }
        });

        toast.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
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
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(DEBUG_TAG,e.getMessage());
            }
            if (photoFile != null) {
                try {
                    photoURI = FileProvider.getUriForFile(this, "com.unipd.ingsw.gruppo3.fileprovider", photoFile);
                    Log.d(DEBUG_TAG,"CREATO A "+photoURI.getPath());
                }catch(Exception e){
                    Log.d(DEBUG_TAG,"ECCEZIONE URI");
                    Log.d(DEBUG_TAG,e.getMessage());
                }
                takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePhoto, REQUEST_TAKE_PHOTO);
            }else{
                Toast.makeText(this, "null", Toast.LENGTH_SHORT);
            }

        }else{
            Log.d(DEBUG_TAG,"AAAAAAAAAAAAAA");
        }
    }


    /**Lazzarin
     * It creates a temporary file where to save the photo on.
     * @Framing Directory Pictures
     * @Return the temporary file
     *
     */
    private File createImageFile() throws IOException {
        /// Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        if(image.exists()){
            Log.d("AAAAAAAAAA","ESISTE!!!!");
        }
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
                    clearAllImages();
                    printAllImages();
                    break;
            }
        }
    }

    //
    //

    /** Dal Maso
     * Salva il bitmap passato nell'apposita cartella
     * @param imageToSave bitmap da salvare come jpeg
     */
    private void savePickedFile(Bitmap imageToSave) {
        File myDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File dir = new File(myDir, "photos");
        boolean made;
        if(made = dir.mkdirs()){
            Log.d(DEBUG_TAG,"MKDIRS "+made);
        }else{
            Log.d(DEBUG_TAG,"NOT MADE");
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        String fname = imageFileName+".jpg";
        File file = new File(dir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            //PICCOLO
            // aggiungo il file al db
            //DatabaseManager helper = DatabaseManager.getInstance(getApplicationContext());
            //helper.addPhoto(root+fname); DB ALTERNATIVO
            //DbManager db = new DbManager(getApplicationContext());
            //db.addRecord(root+fname,"","","");
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d(DEBUG_TAG,"FINAL WRITE EXCEPTION");
            Log.d(DEBUG_TAG,e.getMessage());
            e.printStackTrace();
        }
        Uri uriFile = Uri.fromFile(file);
        Log.d(DEBUG_TAG,"Uri created at "+uriFile.getPath());
        TicketEntity ticketToSave = new TicketEntity();
        ticketToSave.setMissionID(missionEntity.getID());
        ticketToSave.setFileUri(uriFile);
        DataManager.getInstance(this).addTicket(ticketToSave);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmapPhoto = BitmapFactory.decodeFile(ticketToSave.getFileUri().getPath(),bmOptions);
        Ticket ticket = Wrapper.toTicket(ticketToSave);
        ticket.bitmap = bitmapPhoto;
        requestedTickets.add(ticket);
        MainActivityGruppo1.mainActivity.requestTicket(ticket);
        list.add(ticketToSave);
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
        String root = Variables.getInstance().getCurrentMissionDir();
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
        CustomAdapterGruppo1 adapter = new CustomAdapterGruppo1(this, R.layout.cardview, list);
        adapter.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }//clearAllImages


    /** Dal Maso
     * Legge tutte le immagini
     * @return ritorna tutti i file letti nella cartella
     */
    private File[] readAllImages(){
        String path = Variables.getInstance().getCurrentMissionDir();
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
        list = DataManager.getInstance(this).getTicketsForMission(missionEntity.getID());
        for(TicketEntity ticketEntity : list){
            Log.d(DEBUG_TAG, "Adding to list ticket number "+ticketEntity.getID()+"with amount = "+ticketEntity.getAmount());
            addToList(ticketEntity);
        }
        TextView noBills = (TextView)findViewById(R.id.noBills);
        if(list.size() == 0){
            noBills.setVisibility(View.VISIBLE);
        }
        else{
            noBills.setVisibility(View.INVISIBLE);
        }
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

    @Override
    public void onTicketReady(Ticket ticket) {
        for(TicketEntity ticketEntity : list){
            if(ticketEntity.getID() == ticket.ID)
                if(ticket.amount!=null) {
                ticketEntity.setAmount(ticket.amount);
                DataManager.getInstance(this).updateTicket(ticketEntity);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearAllImages();
                        printAllImages();
                    }
                });
            }
        }
    }

    @Override
    public boolean isRequested(Ticket ticket) {
        boolean isRequested = requestedTickets.contains(ticket);
        if(isRequested){
            requestedTickets.remove(ticket);
        }
        return isRequested;
    }

    /** NOT USED
     * VERSIONE DATABASE
     *PICCOLO
     *   il id del file da cancellare a
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
