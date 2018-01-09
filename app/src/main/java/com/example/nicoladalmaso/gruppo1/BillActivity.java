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
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

import com.ing.software.ocr.ImageProcessor;
import com.ing.software.ocr.OcrManager;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

public class BillActivity extends AppCompatActivity {
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List<TicketEntity> list = new LinkedList<TicketEntity>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    String tempPhotoPath;
    Integer missionID;
    MissionEntity thisMission;
    Context context;
    String root;
    public DataManager DB;
    OcrManager ocrManager;
    HashMap<Integer,Bitmap> bitmaps = new HashMap<>();
    int screenWIdth;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_PHOTO_FOR_AVATAR = 2;
    static final int TICKET_MOD = 4;
    static final int MISSION_MOD = 5;

    /** Modified by Federico Taschin
     *  Creates the activity that displays the tickets of the selected mission. Istantiates the OCR module.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        Intent intent = getIntent();
        missionID = intent.getExtras().getInt(IntentCodes.INTENT_MISSION_ID);
        thisMission = DB.getMission(missionID);
        PersonEntity person = DB.getPerson(thisMission.getPersonID());
        setTitle(person.getName()+" "+person.getLastName()+": "+thisMission.getName());

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWIdth = metrics.widthPixels;

        ocrManager = new OcrManager();
        while (ocrManager.initialize(this) != 0) { // 'this' is the context
            try {
                //On first run vision library will be downloaded
                Toast.makeText(this, "Downloading library...", Toast.LENGTH_LONG).show();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        initializeComponents();
    }

    /** Dal Maso
     * Setting toolbar delete button and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mission_menu, menu);
        return true;
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
            case (R.id.action_deleteMission):
                deleteMission();
                break;
            case (R.id.action_editMission):
                //TODO: modifica la missione
                Intent editMission = new Intent(context, com.example.nicoladalmaso.gruppo1.EditMission.class);
                editMission.putExtra(IntentCodes.INTENT_MISSION_ID, thisMission.getID());
                startActivityForResult(editMission, MISSION_MOD);
                break;
            default:
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso
     *  Manage all animations and catch onclick events about FloatingActionButtons
     */
    public void initializeComponents(){
        printAllTickets();
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
        if(thisMission.isRepay()) {
            fab.setVisibility(View.INVISIBLE);
        }
        else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    /** Dal Maso
     *  Animations for Floating Action Button (FAB)
     */
    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {

            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }

    /** Dal Maso
     * Add new ticket to the list
     */
    public void addToList(TicketEntity t){
        list.add(t);
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, missionID, screenWIdth);
        adapter.setBitmaps(bitmaps);
        listView.setAdapter(adapter);
        Log.d("DEBUGTICKET","addToList(): "+t.getAmount());
    }

    /** Cereated by FEDERICO TASCHIN
     *  Refresh the listView by reading the tickets from the database and recreating the adapter
     *  Images aren't readed if they're already in the memory
     */
    public void refreshList(){
        list = DB.getTicketForMissionOrderedByInsertionDate(missionID);
        Log.d("TICKETDEBUG","LIST SIZE: "+list.size());
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, missionID, screenWIdth);
        //I decided to replace my bitmap caching management with that of Picasso library
        //setBitmaps();
        adapter.setBitmaps(bitmaps);
        listView.setAdapter(adapter);
    }

    /**
     * !!!!! REPLACED WITH PICASSO LIBRARY !!!!!!!
     * Created by FEDERICO TASCHIN
     *  Scales the bitmaps and adds all those that aren't in memory already to the HashMap.
     */
    public void setBitmaps(){
       for(int i = 0; i<list.size(); i++){
           TicketEntity ticketEntity = list.get(i);
           if(!bitmaps.containsKey(new Integer((int)ticketEntity.getID()))){

               BitmapFactory.Options options = new BitmapFactory.Options();
               options.inPreferredConfig = Bitmap.Config.ARGB_8888;
               Bitmap bitmap = BitmapFactory.decodeFile(ticketEntity.getFileUri().getPath(), options);

               DisplayMetrics displayMetrics = new DisplayMetrics();
               this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
               double width = displayMetrics.widthPixels/2;
               int nh = (int) (bitmap.getHeight() * (width/ bitmap.getWidth()));
               Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int)width, nh, true);
               bitmaps.put((int)ticketEntity.getID(),scaled);
           }
       }
    }

    /** Dal Maso (Using Lazzarin code)
     * Delete the mission from the bill viewer (inside the mission)
     */
    public void deleteMission(){
        //Lazzarin
        AlertDialog.Builder toast = new AlertDialog.Builder(BillActivity.this);
        //Dialog
        toast.setMessage(context.getString(R.string.deleteMissionToast))
                .setTitle(context.getString(R.string.deleteTitle));
        //Positive button
        toast.setPositiveButton(context.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<TicketEntity> list = DB.getTicketsForMission(missionID);
                for(int i = 0; i < list.size(); i++){
                    DB.deleteTicket((int) list.get(i).getID());
                }
                DB.deleteMission(missionID);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        //Negative button
        toast.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing to do
            }
        });
        //Show toast
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
            } catch (IOException e)
            {
             Log.d("IOException","error using createImageFile method");
            }
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
     *  Delete all temp files used for saving camera's images
     */
    public void deleteTempFiles(){
        File[] files = readAllImages();
        String filename = "";
        for (int i = 0; i < files.length; i++)
        {
            filename = files[i].getName();
            if(filename.substring(0,4).equals("temp")){
                files[i].delete();
            }
        }
    }


    /** Dal Maso
     *  Pick up photo from gallery
     */
    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }


    /** Dal Maso modified by FEDERICO TASCHIN
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
                    //clearAllImages();
                    long time = System.currentTimeMillis();
                    printAllTickets();
                    Log.d("TICKETDEBUG","TIME TAKEN: "+((System.currentTimeMillis()-time)/1000));
                    break;

                //Dal Maso
                //Gallery photo
                case (PICK_PHOTO_FOR_AVATAR):
                    photoURI = data.getData();
                    try {
                        Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                        savePickedFile(btm);
                        //clearAllImages();
                        Log.d("DEBUGTICKET","onActivityResult{CASE PICK_PHOTO_FOR_AVATAR}");
                        printAllTickets();
                    }catch (Exception e){
                        Log.d("Foto da galleria", "ERROR");
                    }
                    break;

                //Dal Maso
                //Resize management
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                    clearAllImages();
                    printAllTickets();
                    break;

                case (TICKET_MOD):
                    clearAllImages();
                    printAllTickets();
                    break;

                case (MISSION_MOD):
                    thisMission = DB.getMission(missionID);
                    setTitle(thisMission.getName());
                    clearAllImages();
                    printAllTickets();
                    break;

                default:
                    clearAllImages();
                    printAllTickets();
                    break;
            }
        }
    }


    /** Dal Maso, modified by FEDERICO TASCHIN
     * Saves the given bitmap, sets its path in the TicketEntity object and inserts it in the database.
     * Starts the OCR analysis
     * @param imageToSave bitmap to save
     */
    private void savePickedFile(Bitmap imageToSave) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String fname = imageFileName+".jpg";
        File file = new File(root, fname);
        File originalPhoto = new File(root,fname+"orig");
        final Uri uri=Uri.fromFile(file);
        if (file.exists())
            file.delete();
        if(originalPhoto.exists())
            originalPhoto.delete();
        try {
            long time = System.currentTimeMillis();
            FileOutputStream out = new FileOutputStream(file);
            TicketEntity ticket = new TicketEntity();
            ticket.setDate(Calendar.getInstance().getTime());
            ticket.setFileUri(uri);
            ticket.setShop("Pam Padova");
            ticket.setTitle("Scontrino ");
            ticket.setMissionID(missionID);
            ticket.setInsertionDate(Calendar.getInstance().getTime());
            DB.addTicket(ticket);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            FileOutputStream outOriginal = new FileOutputStream(originalPhoto);
            imageToSave.compress(Bitmap.CompressFormat.JPEG,90,outOriginal);
            outOriginal.flush();
            outOriginal.close();
            long time1 = System.currentTimeMillis();
            startOcrAnalysis(imageToSave, ticket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Federico Taschin
     * Starts the OCR analysis of the bitmap. When the resuly is ready, it updates the database and the activity is refreshed
     * @param bitmap not null, image to be analyzed
     * @param ticketEntity the object related to the image
     */
    private void startOcrAnalysis(Bitmap bitmap, TicketEntity ticketEntity){
        ImageProcessor processor = new ImageProcessor(bitmap);
        ocrManager.getTicket(processor, result -> {
            Log.d("DEBUGOCR","RESULT IS READY");
            ticketEntity.setAmount(result.amount);
            DB.updateTicket(ticketEntity);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("DEBUGTICKET0","LIST REFRESHED");
                    refreshList();
                }
            });
        });
    }



    /**PICCOLO
     * Method that clears the screen from the images
     */
    public void clearAllImages(){
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, missionID, screenWIdth);
        adapter.clear();
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }//clearAllImages


    /** Dal Maso
     * read all images
     * @return all read files in the folder
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


    /** Dal Maso, Modified by Federico TAschin
     *  Calls the refreshList() method. If the list is empty, it displays a message
     */
    public void printAllTickets(){
        refreshList();

        //If there aren't tickets show message
        TextView noBills = (TextView)findViewById(R.id.noBills);
        String noBillsError=getResources().getString(R.string.noBills);
        if(!thisMission.isRepay())
            noBillsError+=getResources().getString(R.string.noBillsOpen);
        noBills.setText(noBillsError);
        if(list.isEmpty()){
            noBills.setVisibility(View.VISIBLE);
        }
        else{
            noBills.setVisibility(View.INVISIBLE);
        }
    }

    /**PICCOLO_Edit by Dal Maso
     * Method that lets the user crop the photo
     * @param toCrop photo's index
     * @param path path of the photo
     */
    public void cropFile(int toCrop, String path){
        Log.d("Crop","Success");
        File directory = new File(path);
        File[] files = directory.listFiles();
        CropImage.activity(Uri.fromFile(files[toCrop])).start(this);
    }//cropFile

    /**PICCOLO
     * Method that is run when the activity is resumed.
     * it hides the button for adding tickets if the mission is closed, else it shows it.
     */
    public void onResume(){
        super.onResume();
        if(thisMission.isRepay()) {
            fab.setVisibility(View.INVISIBLE);
        }
        else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy(){
        ocrManager.release();
        super.onDestroy();
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        printAllTickets();
    }
}
