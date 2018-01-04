package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;

public class BillViewer extends AppCompatActivity {
    public FloatingActionButton fabEdit, fabDelete, fabCrop, fabConfirmEdit;
    public DataManager DB;
    int ticketId;
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

        fabCrop=(FloatingActionButton)findViewById(R.id.fabCrop);
        fabDelete=(FloatingActionButton)findViewById(R.id.fabDelete);
        fabCrop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cropPhoto(ticketId);
           }//onClick
        });
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTicket(ticketId);
            }//onClick
        });
    }

    /** Dal Maso
     * Initialize components
     */
    public void initialize(){
        //Get data from parent view
        Intent intent = getIntent();
        ticketId = (int) intent.getExtras().getLong("ID");
        thisTicket = DB.getTicket(ticketId);
        ticketPath = thisTicket.getFileUri().toString().substring(7);
        ticketTitle = thisTicket.getTitle();
        ticketDate = thisTicket.getDate().toString();
        ticketShop = thisTicket.getShop();
        ticketAmount = thisTicket.getAmount().toString();

        //Title
        setTitle(ticketTitle);
        TextView billLastMod = (TextView)findViewById(R.id.billDate);
        billLastMod.setText(ticketDate);

        //ImageName
        TextView billName = (TextView)findViewById(R.id.billName);
        billName.setText(ticketTitle);

        //Total price
        TextView billPrice = (TextView)findViewById(R.id.billTotal);
        billPrice.setText(ticketAmount+" €");

        //Shop
        TextView billShop = (TextView)findViewById(R.id.billShop);
        billShop.setText(ticketShop);

        //Full image view
        ImageView imgView = (ImageView)findViewById(R.id.billImage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(ticketPath,bmOptions);
        imgView.setImageBitmap(bitmap);
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editticket_menu, menu);
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

            case R.id.action_editTicket:
                //Open Edit Ticket Activity
                Intent editTicket = new Intent(context, com.example.nicoladalmaso.gruppo1.EditTicket.class);
                editTicket.putExtra("ticketID", thisTicket.getID());
                startActivityForResult(editTicket, TICKET_MOD);
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
                }
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
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(toCropUri.toString().substring(7),bmOptions);
        imgView.setImageBitmap(bitmap);
    }//cropPhoto
}

