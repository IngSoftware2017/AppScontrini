package com.ing.software.ticketapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import database.DataManager;
import database.TicketEntity;

import static com.ing.software.ticketapp.StatusVars.REDO_OCR;


public class BillViewer extends AppCompatActivity {
    public FloatingActionButton fabEdit, fabEditor, fabCrop, fabConfirmEdit, fabOcr;
    public DataManager DB;
    long ticketId;
    Context context;
    final int TICKET_MOD = 1;
    TicketEntity thisTicket;
    String ticketTitle = "", ticketDate = "", ticketAmount = "", ticketShop = "", ticketPath = "";

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_bill_viewer);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        initialize();

        //fabCrop=(FloatingActionButton)findViewById(R.id.fabCrop);
        fabEditor =(FloatingActionButton)findViewById(R.id.fabEdit);
        fabOcr = findViewById(R.id.fabOcr);
        /*
        fabCrop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cropPhoto(ticketId);
           }//onClick
        });
        */
        fabEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open Edit Ticket Activity
                Intent editTicket = new Intent(context, EditTicket.class);
                editTicket.putExtra("ticketID", thisTicket.getID());
                startActivityForResult(editTicket, TICKET_MOD);
            }//onClick
        });
        fabOcr.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("ticketID", String.valueOf(ticketId));
            Log.d("TICKETID_REDO_OCR", "ID is: " + ticketId);
            setResult(REDO_OCR, intent);
            finish();
        });
    }

    public void initialize(){
        //Get data from parent view
        Intent intent = getIntent();
        ticketId = intent.getExtras().getLong("ID");
        thisTicket = DB.getTicket(ticketId);
        ticketPath = thisTicket.getFileUri().toString().substring(7);
        ticketTitle = thisTicket.getTitle();
        if (thisTicket.getDate() != null) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            ticketDate = df.format(thisTicket.getDate());
        } else
            ticketDate = getString(R.string.no_date);
        ticketShop = thisTicket.getShop();
        if (thisTicket.getAmount() != null)
            ticketAmount = thisTicket.getAmount().setScale(2, RoundingMode.HALF_UP).toString();
        else
            ticketAmount = getString(R.string.no_amount);

        //Title
        setTitle(ticketTitle);
        TextView billLastMod = (TextView)findViewById(R.id.billDate);
        billLastMod.setText(ticketDate);

        //ImageName
        TextView billName = (TextView)findViewById(R.id.billName);
        billName.setText(ticketTitle);

        //Total price
        TextView billPrice = (TextView)findViewById(R.id.billTotal);
        billPrice.setText(ticketAmount);

        //Shop
        TextView billShop = (TextView)findViewById(R.id.billShop);
        billShop.setText(ticketShop);

        //Full image view
        ImageView imgView = (ImageView)findViewById(R.id.billImage);
        //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //Bitmap bitmap = BitmapFactory.decodeFile(ticketPath,bmOptions);
        //imgView.setImageBitmap(bitmap);
        Glide
                .with(context)
                .load(ticketPath)
                .thumbnail(0.1f)
                .into(imgView);

        imgView.setOnClickListener(v -> {
            Intent fullImgIntent = new Intent(this, FullImageActivity.class);
            fullImgIntent.putExtra("ID", ticketId);
            startActivity(fullImgIntent);
        });
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bill_viewer_menu, menu);
        /*
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(false); //remove back button, or redo ocr doesn't work
            */
        return true;
    }

    /** Dal Maso
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.action_deleteBill:
                deleteTicket(ticketId);
                break;

            default:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
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
                case(TICKET_MOD):
                    initialize();
                    break;
                case (CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE):
                    initialize();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        setResult(RESULT_OK, mIntent);
        super.onBackPressed();
    }


    /**PICCOLO, DAL MASO
     * Method that deletes a ticket from the db
     * @param id the id of the TicketEntity in the db
     */
    private void deleteTicket(long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.deleteTicketToast))
                .setTitle(this.getString(R.string.deleteTitle));
        // Add the buttons
        builder.setPositiveButton(this.getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                TicketEntity thisTicket = DB.getTicket(ticketId);
                String toDelete = "";
                //Get the ticket to delete
                toDelete = thisTicket.getFileUri().toString().substring(7);
                final File ticketDelete = new File(toDelete);
                final File ticketDeleteOriginal = new File (toDelete+"orig");
                Log.d("TicketID", ""+ticketId);
                if(DB.deleteTicket(ticketId) && ticketDelete.delete() && ticketDeleteOriginal.delete()){
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } //todo log error
            }
        });
        builder.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing
            }
        });
        AlertDialog alert = builder.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setTextColor(Color.parseColor("#2196F3"));
    }//deleteTicket

    /**PICCOLO
     * Method that lets the user crop and/or rotate the original photo
     * @param id the id of the TicketEntity in the db
     */
    private void cropPhoto(long id) {
        TicketEntity ticket = DB.getTicket((int) id);
        Uri toCropUri = ticket.getFileUri();
        File originalFile = new File(toCropUri.toString().substring(7)+"orig");
        Uri originalUri=Uri.fromFile(originalFile);
        CropImage.activity(originalUri)
                .setOutputUri(toCropUri).start(this);
        ticket.setFileUri(toCropUri);

        ImageView imgView = (ImageView)findViewById(R.id.billImage);
        //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //Bitmap bitmap = BitmapFactory.decodeFile(toCropUri.toString().substring(7),bmOptions);
        //imgView.setImageBitmap(bitmap);
        Glide
                .with(context)
                .load(toCropUri.toString().substring(7))
                .thumbnail(0.1f)
                .into(imgView);
    }//cropPhoto
}

