package com.ing.software.ticketapp;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.software.ocr.ImageProcessor;
import com.ing.software.ocr.OcrManager;
import com.ing.software.ocr.OcrUtils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;

import static com.ing.software.ticketapp.StatusVars.*;

public class BillActivity extends AppCompatActivity  implements OcrResultReceiver.Receiver {

    final OcrResultReceiver mReceiver = new OcrResultReceiver(new Handler());
    private static OcrManager ocrManager;
    public FloatingActionButton fab, fab1, fab2;
    public Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public List<TicketEntity> list = new LinkedList<TicketEntity>();
    public Uri photoURI;
    public boolean isFabOpen = false;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_PHOTO_FOR_AVATAR = 2;
    static final int TICKET_MOD = 4;
    static final int MISSION_MOD = 5;
    String tempPhotoPath;
    Integer missionID;
    Context context;
    String root;
    public DataManager DB;
    MissionEntity thisMission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        Intent intent = getIntent();

        missionID = intent.getExtras().getInt("missionID");
        thisMission = DB.getMission(missionID);
        setTitle(thisMission.getName());
        context = this.getApplicationContext();
        initializeComponents();
        mReceiver.setReceiver(this);
        ocrManager = new OcrManager();
        while (ocrManager.initialize(this) != 0) {
            try {
                Toast.makeText(this, getString(R.string.downloading_library), Toast.LENGTH_SHORT).show();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ocrManager.release();
    }

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
     * Edit by Lazzarin
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.action_deleteMission:
                deleteMission();
                break;
            case (R.id.action_editMission):
                //TODO: modifica la missione
                Intent editMission = new Intent(context, EditMission.class);
                editMission.putExtra("missionID", thisMission.getID());
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
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, missionID, DB);
        listView.setAdapter(adapter);
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
                        AUTHORITY,
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
    public void deleteTempFiles() {
        File[] files = readAllImages();
        String filename = "";
        for (int i = 0; i < files.length; i++)
        {
            filename = files[i].getName();
            if (filename.substring(0, 4).equals("temp")) {
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
                /**lazzarin
                 * Saves definitely the photo without losing quality, deletes the temporary file and shows
                 * the new photo.
                 * @Framing Add the photo on the directory using savePickedFile()
                 */
                case(REQUEST_TAKE_PHOTO):
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmapPhoto = BitmapFactory.decodeFile(tempPhotoPath,bmOptions);
                    savePickedFile(bitmapPhoto);
                    if(bitmapPhoto!=null) {
                        bitmapPhoto.recycle();
                        bitmapPhoto=null;
                    }
                    deleteTempFiles();
                    //waitDB();
                    clearAllImages();
                    printAllTickets();
                    break;

                //Dal Maso
                //Gallery photo
                case (PICK_PHOTO_FOR_AVATAR):
                    photoURI = data.getData();
                    try {
                        Bitmap btm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                        savePickedFile(btm);
                        //waitDB();
                        clearAllImages();
                        printAllTickets();
                    }catch (Exception e){
                        Log.d("Foto da galleria", "ERROR");
                    }
                    break;
                //Dal Maso
                //Resize management
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                    //waitDB();
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
        } else if (resultCode == REDO_OCR) {
            long ticketID = Long.parseLong(data.getStringExtra("ticketID"));
            TicketEntity ticket = DB.getTicket(ticketID);
            Log.d("TICKETID_REDO_OCR", "ID received in bill activity is: " + ticketID);
            Uri bitmapUri = ticket.getFileUri();
            String fname = getFileName(bitmapUri);
            //DB.deleteTicket(ticketID);
            Log.d("###################", "#####################################");
            Log.d("Image_file_name", "is: " + fname);
            Intent intent = new Intent(BillActivity.this, OcrService.class);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra("image", fname);
            intent.putExtra("root", root);
            intent.putExtra(TICKET_ID, String.valueOf(ticketID));
            startService(intent);
        }
    }

    /** Dal Maso
     * Thread sleep for 1 second for right tickets real-time vision
     */
    /*
    public void waitDB(){
        try {
            Log.i("Waiting db", "Going to sleep");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    */

    /** Dal Maso
     * Save the bitmap passed
     * @param imageToSave bitmap to save
     */
    private void savePickedFile(Bitmap imageToSave) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
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
            FileOutputStream out = new FileOutputStream(file);

            //TODO: HardCode example, implement ocr here

            //DB.addTicket(new TicketEntity(uri, BigDecimal.valueOf(100).movePointLeft(2), null, Calendar.getInstance().getTime(), fname, missionID));
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            FileOutputStream outOriginal = new FileOutputStream(originalPhoto);
            imageToSave.compress(Bitmap.CompressFormat.JPEG,90,outOriginal);
            outOriginal.flush();
            outOriginal.close();
            Intent intent = new Intent(BillActivity.this, OcrService.class);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra("image", fname);
            intent.putExtra("root", root);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**PICCOLO
     * Method that clears the screen from the images
     */
    public void clearAllImages(){
        list = new LinkedList<>();
        ListView listView = (ListView)findViewById(R.id.list1);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.cardview, list, missionID, DB);
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


    /** Dal Maso
     *  Print all tickets, get it from DB
     */
    public void printAllTickets(){
        List<TicketEntity> ticketList = DB.getTicketsForMission(missionID);
        Log.d("Tickets", ticketList.toString());
        int count = 0;
        for(int i = 0; i < ticketList.size(); i++){
            Log.d("Ticket_ID", ""+ticketList.get(i).getID());
            addToList(ticketList.get(i));
            count++;
        }
        //If there aren't tickets show message
        TextView noBills = (TextView)findViewById(R.id.noBills);
        String noBillsError=getResources().getString(R.string.noBills);
        if(!thisMission.isRepay())
            noBillsError+=getResources().getString(R.string.noBillsOpen);
        noBills.setText(noBillsError);
        if(count == 0){
            noBills.setVisibility(View.VISIBLE);
        }
        else{
            noBills.setVisibility(View.INVISIBLE);
        }
    }

    void printSingleTicket(TicketEntity t) {
        if (list.size() == 0) {
            TextView noBills = (TextView)findViewById(R.id.noBills);
            noBills.setVisibility(View.INVISIBLE);
        }
        addToList(t);
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case STATUS_RUNNING:
                //Toast.makeText(this, "Starting img ", Toast.LENGTH_SHORT).show();
                //show new photo, but set amount and date to null
                if (resultData.getString(IMAGE_RECEIVED) != null) {
                    Uri uri = Uri.fromFile(new File(resultData.getString(ROOT_RECEIVED), resultData.getString(IMAGE_RECEIVED)));
                    Log.d("TICKETID_REDO_OCR", "ID received in service (start) is: " + resultData.getString(TICKET_ID));
                    if (resultData.getString(TICKET_ID) == null) {
                        //photo is not present, add an image, will be remove once clearAllImages will be called
                        printSingleTicket(new TicketEntity(uri, null, null, null, null, missionID));
                    }
                }
                break;
            case STATUS_FINISHED:
                //Toast.makeText(this, "Amount is: " + resultData.getString(AMOUNT_RECEIVED) + "\nDate is: "
                //        + resultData.getString(DATE_RECEIVED), Toast.LENGTH_LONG).show();
                if (resultData.getString(IMAGE_RECEIVED) != null) {
                    Uri uri = Uri.fromFile(new File(resultData.getString(ROOT_RECEIVED), resultData.getString(IMAGE_RECEIVED)));
                    BigDecimal amount = null;
                    try {
                        amount = new BigDecimal(resultData.getString(AMOUNT_RECEIVED)).setScale(2, RoundingMode.HALF_UP);
                    } catch (NumberFormatException e) {
                        //No valid amount
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    Date date = null;
                    try {
                        date = dateFormat.parse(resultData.getString(DATE_RECEIVED));
                    } catch (ParseException e) {
                        //Nada
                    }
                    //if (date == null)
                    //    date = Calendar.getInstance().getTime();
                    Log.d("TICKETID_REDO_OCR", "ID received in service (finished) is: " + resultData.getString(TICKET_ID));
                    if (resultData.getString(TICKET_ID) != null) {
                        try {
                            long ticketID = Long.parseLong(resultData.getString(TICKET_ID));
                            TicketEntity ticket = DB.getTicket(ticketID);
                            if (ticket != null) {
                                ticket.setAmount(amount);
                                ticket.setDate(date);
                                ticket.setFileUri(uri);
                            }
                            DB.updateTicket(ticket);
                        } catch (NumberFormatException e) {
                            //Nada
                        }
                    } else
                        DB.addTicket(new TicketEntity(uri, amount, null, date, null, missionID));
                    clearAllImages();
                    printAllTickets();
                }
                break;
            case STATUS_ERROR:
                /* Handle the error */
                String error = resultData.getString(ERROR_RECEIVED);
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Service to manage requests to analyze tickets
     * When ticket is ready, send message to the receiver.
     */
    public static class OcrService extends IntentService {

        public OcrService() {
            super("OcrService");
        }

        public OcrService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(final Intent workIntent) {
            OcrUtils.log(1, "OcrService", "Entering service");
            final ResultReceiver receiver = workIntent.getParcelableExtra("receiver");
            final Bundle bundle = new Bundle();
            String root = workIntent.getStringExtra("root");
            String name = workIntent.getStringExtra("image");
            String ticketID = workIntent.getStringExtra(TICKET_ID);
            bundle.putString(ROOT_RECEIVED, root);
            bundle.putString(IMAGE_RECEIVED, name);
            bundle.putString(TICKET_ID, ticketID);
            File file = new File(root, name);
            Bitmap testBmp = getBitmapFromFile(file);
            receiver.send(STATUS_RUNNING, bundle);
            ocrManager.initialize(this);
            ImageProcessor preproc = new ImageProcessor(testBmp);
            preproc.findTicket(false, err -> {
                    ocrManager.getTicket(preproc, result -> {
                    OcrUtils.log(1, "OcrHandler", "Detection complete");
                    if (result.amount != null) {
                        OcrUtils.log(1, "OcrHandler", "Amount: " + result.amount);
                        bundle.putString(AMOUNT_RECEIVED, result.amount.toString());
                    } else {
                        OcrUtils.log(1, "OcrHandler", "No amount found");
                        bundle.putString(AMOUNT_RECEIVED, NOT_FOUND);
                    }
                    if (result.date != null) {
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                        String formattedDate = df.format(result.date);
                        OcrUtils.log(1, "OcrHandler", "Date: " + result.date.toString());
                        OcrUtils.log(1, "OcrHandler", "Formatted Date: " + formattedDate);
                        bundle.putString(DATE_RECEIVED, formattedDate);
                    } else {
                        OcrUtils.log(1, "OcrHandler", "No date found");
                        bundle.putString(DATE_RECEIVED, NOT_FOUND);
                    }
                        receiver.send(STATUS_FINISHED, bundle);
                });
            });
            this.stopSelf();
        }

        /**
         * Decode bitmap from file
         *
         * @param file not null and must be an image
         * @return bitmap from file
         */
        private Bitmap getBitmapFromFile(File file) {
            FileInputStream fis = null;
            try {
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return BitmapFactory.decodeStream(fis);
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
