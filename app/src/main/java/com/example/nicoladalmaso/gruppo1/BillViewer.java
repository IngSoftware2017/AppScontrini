package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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

        //Get data from parent view
        Intent intent = getIntent();
        ticketId = (int) intent.getExtras().getLong("ID");
        String imgPath = intent.getExtras().getString("imagePath");
        String imgName = intent.getExtras().getString("imageName");
        String imgLastMod = intent.getExtras().getString("imgDate");
        String imgPrice = intent.getExtras().getString("imgPrice");

        //Title
        setTitle(imgName);
        TextView billLastMod = (TextView)findViewById(R.id.billDate);
        billLastMod.setText(imgLastMod);

        //ImageName
        TextView billName = (TextView)findViewById(R.id.billName);
        billName.setText(imgName);

        //Total price
        TextView billPrice = (TextView)findViewById(R.id.billTotal);
        billPrice.setText(imgPrice);

        //Full image view
        ImageView imgView = (ImageView)findViewById(R.id.billImage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,bmOptions);
        imgView.setImageBitmap(bitmap);

        fabCrop=(FloatingActionButton)findViewById(R.id.fabCrop);
        fabDelete=(FloatingActionButton)findViewById(R.id.fabDelete);

        //Edit Ticket Info button
        /**fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTicketInfo(id);
            }//onClick
        });*/
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
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addmission_menu, menu);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**PICCOLO
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
                Log.d("TicketID", ""+ticketId);
                if(DB.deleteTicket(ticketId) && ticketDelete.delete()){
                    Log.d("ELIMINATO", "OK");
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
     * Method that lets the user crop and/or rotate the photo
     * @param id the id of the TicketEntity in the db
     */
    private void cropPhoto(long id) {
        DB=new DataManager(getApplicationContext());
        TicketEntity ticket = DB.getTicket((int) id);
        Uri toCropUri = ticket.getFileUri();
        CropImage.activity(toCropUri)
                .setOutputUri(toCropUri).start(this);
        ticket.setFileUri(toCropUri);
        DB.deleteTicket((int)id);
        DB.addTicket(ticket);
       //TODO: implement the method using the origial file instead
    }//cropPhoto

    //Dal Maso, manage back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

