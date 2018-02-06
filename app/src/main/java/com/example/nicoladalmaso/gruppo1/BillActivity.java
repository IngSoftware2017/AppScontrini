package com.example.nicoladalmaso.gruppo1;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.ing.software.ocr.OcrManager;
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
    CustomAdapter adapter;
    Camera mCamera;
    ListView listView;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_PHOTO_FOR_AVATAR = 2;
    static final int TICKET_MOD = 4;
    static final int MISSION_MOD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        PersonEntity thisPerson = DB.getPerson(Singleton.getInstance().getPersonID());


        missionID = Singleton.getInstance().getMissionID();
        thisMission = DB.getMission(missionID);
        //lazzarin
        Singleton.getInstance().setStartDate(thisMission.getStartDate());
        Singleton.getInstance().setEndDate(thisMission.getEndDate());

        ActionBar ab = getSupportActionBar();
        ab.setTitle(thisMission.getName());
        ab.setSubtitle("Missione di "+thisPerson.getName()+" "+thisPerson.getLastName());

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
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.action_export:
                //TODO: show from bottom export mission
                break;

            default:
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.export_menu, menu);
        return true;
    }

    /** Dal Maso
     * Se non sono presenti ticket mostra come aggiungerli
     */
    public void showGuide(){
        TapTargetView.showFor(this, TapTarget.forView(findViewById(R.id.fab), "Iniziamo!", "Aggiungi un nuovo scontrino, inizia subito scattando una foto")
            .targetCircleColor(R.color.white)
            .titleTextSize(20)
            .titleTextColor(R.color.white)
            .descriptionTextSize(10)
            .descriptionTextColor(R.color.white)
            .textColor(R.color.white)
            .icon(getResources().getDrawable(R.mipmap.ic_camera_white_24dp)),
            new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);      // This call is optional
                    takePhotoIntent();
                }
            }
        );
    }

    /** Dal Maso
     *  Manage all animations and catch onclick events about FloatingActionButtons
     */
    public void initializeComponents(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                1);
        listView = (ListView)findViewById(R.id.list1);
        printAllTickets();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhotoIntent();
            }
        });

        if(thisMission.isClosed()) {
            fab.setVisibility(View.INVISIBLE);
        }
        else{
            fab.setVisibility(View.VISIBLE);
        }

        if(DB.getAllTickets().size() == 0){
            showGuide();
        }
    }

    /** Dal Maso
     * Add new ticket to the list
     */
    public void addToList(){
        adapter = new CustomAdapter(this, R.layout.cardview, list, missionID, DB);
        listView.setAdapter(adapter);
    }


    /**Dal Maso
     * It opens the custom camera, take the photo, analize it and save it
     */
    private void takePhotoIntent() {
        if(checkCameraHardware(context)){
            mCamera = getCameraInstance();
            Intent cameraActivity = new Intent(context, com.example.nicoladalmaso.gruppo1.CameraActivity.class);
            startActivityForResult(cameraActivity, REQUEST_TAKE_PHOTO);
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
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

                case(REQUEST_TAKE_PHOTO):
                    printAllTickets();
                    break;


                default:
                    printAllTickets();
                    break;
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("CANCELLED", "OK");
            printAllTickets();
        }
    }

    /** Dal Maso
     *  Print all tickets, get it from DB
     */
    public void printAllTickets(){
        list.clear();
        List<TicketEntity> ticketList = DB.getTicketsForMission(missionID);
        Log.d("Tickets", ticketList.toString());
        TicketEntity t;
        int count = 0;
        for(int i = 0; i < ticketList.size(); i++){
            list.add(ticketList.get(i));
            Log.d("Ticket_ID", ""+ticketList.get(i).getID());
            count++;
        }
        addToList();
        //If there aren't tickets show message
        TextView noBills = (TextView)findViewById(R.id.noBills);
        String noBillsError=getResources().getString(R.string.noBills);
        if(!thisMission.isClosed())
            noBillsError+=getResources().getString(R.string.noBillsOpen);
        noBills.setText(noBillsError);
        if(count == 0){
            noBills.setVisibility(View.VISIBLE);
        }
        else{
            noBills.setVisibility(View.INVISIBLE);
        }
    }

    /** DAL MASO
     * Rotate the image
     * @param source photo bitmap
     * @param angle angle of rotation
     * @return rotated bitmap
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**PICCOLO
     * Method that is run when the activity is resumed.
     * it hides the button for adding tickets if the mission is closed, else it shows it.
     */
    public void onResume(){
        super.onResume();
        if(thisMission.isClosed()) {
            fab.setVisibility(View.INVISIBLE);
        }
        else{
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ocrManager.release();
    }
}
